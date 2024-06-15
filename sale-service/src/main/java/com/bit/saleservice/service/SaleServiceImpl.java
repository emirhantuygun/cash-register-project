package com.bit.saleservice.service;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.*;
import com.bit.saleservice.entity.*;
import com.bit.saleservice.exception.*;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import com.bit.saleservice.wrapper.ProductStockCheckRequest;
import com.bit.saleservice.wrapper.ProductStockReduceRequest;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey}")
    private String ROUTING_KEY;
    private static final Logger logger = LogManager.getLogger(SaleServiceApplication.class);
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CampaignProcessService campaignProcessService;
    private final GatewayService gatewayService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public SaleResponse getSale(Long id) {
        logger.info("Fetching sale with ID: {}", id);
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Sale not found with id " + id));

        logger.info("Retrieved sale: {}", sale);
        return mapToSaleResponse(sale);
    }

    @Override
    public List<SaleResponse> getAllSales() {
        logger.info("Fetching all sales");
        List<Sale> sales = saleRepository.findAll();

        logger.info("Retrieved {} sales", sales.size());
        return sales.stream().map(this::mapToSaleResponse).toList();
    }

    @Override
    public List<SaleResponse> getDeletedSales() {
        logger.info("Fetching all deleted sales");
        List<Sale> sales = saleRepository.findSoftDeletedSales();

        logger.info("Retrieved {} deleted sales", sales.size());
        return sales.stream().map(this::mapToSaleResponse).toList();
    }

    @Override
    public Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                           String cashier, String paymentMethod,
                                                           BigDecimal minTotal, BigDecimal maxTotal,
                                                           String startDate, String endDate) {
        logger.info("Fetching all sales with filters and sorting");
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Sale> salesPage = saleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(cashier)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("cashier")), "%" + cashier.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(paymentMethod)) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), Payment.valueOf(paymentMethod)));
            }
            if (minTotal != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalWithCampaign"), minTotal));
            }
            if (maxTotal != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalWithCampaign"), maxTotal));
            }
            if (StringUtils.isNotBlank(startDate)) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(startDate);
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), date));
                } catch (ParseException e) {
                    logger.error("Failed to parse start date: {}", startDate);
                }
            }
            if (StringUtils.isNotBlank(endDate)) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(endDate);
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), date));
                } catch (ParseException e) {
                    logger.error("Failed to parse end date: {}", endDate);
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        logger.info("Retrieved {} sales", salesPage.getTotalElements());
        return salesPage.map(this::mapToSaleResponse);
    }


    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest saleRequest) throws HeaderProcessingException {

        List<Product> products = getProducts(saleRequest.getProducts());
        Payment paymentMethod = getPaymentMethod(saleRequest.getPaymentMethod());
        BigDecimal total = getTotal(products);
        BigDecimal totalWithCampaign = null;
        List<Long> campaignIds = saleRequest.getCampaignIds();
        List<Campaign> campaigns = null;
        BigDecimal cash = null;
        BigDecimal change;
        MixedPayment mixedPayment = null;

        if (campaignIds != null && !campaignIds.isEmpty()) {
            CampaignProcessResult campaignProcessResult = processCampaigns(campaignIds, products, total);
            products = campaignProcessResult.getProducts();
            totalWithCampaign = campaignProcessResult.getTotalWithCampaign();
            campaigns = campaignProcessResult.getCampaigns();
        }

        change = switch (paymentMethod) {
            case CASH -> {
                cash = saleRequest.getCash();
                yield processCashPayment(cash, totalWithCampaign);
            }
            case MIXED -> {
                mixedPayment = saleRequest.getMixedPayment();
                yield processMixedPayment(mixedPayment, totalWithCampaign);
            }
            default -> null;
        };

        Sale sale = Sale.builder()
                .cashier(saleRequest.getCashier())
                .date(new Date())
                .paymentMethod(paymentMethod)
                .campaigns(campaigns)
                .cash(cash)
                .change(change)
                .total(total)
                .totalWithCampaign(totalWithCampaign)
                .mixedPayment(mixedPayment)
                .build();

        saleRepository.save(sale);
        products.forEach(product -> product.setSale(sale));
        productRepository.saveAll(products);

        reduceStocks(products);

        sale.setProducts(products);
        return mapToSaleResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest saleRequest) {
        logger.info("Updating sale with ID {}: {}", id, saleRequest);
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Sale doesn't exist with id " + id));

        Payment paymentMethod = getPaymentMethod(saleRequest.getPaymentMethod());
        if (paymentMethod != existingSale.getPaymentMethod())
            throw new PaymentMethodUpdateNotAllowedException("Payment method update not allowed");

        List<Product> oldProducts = existingSale.getProducts();
        returnProducts(oldProducts);

        try {
            List<Product> products = getProducts(saleRequest.getProducts());
            BigDecimal total = getTotal(products);
            BigDecimal totalWithCampaign = null;
            List<Long> campaignIds = saleRequest.getCampaignIds();
            List<Campaign> campaigns = null;
            BigDecimal cash = null;
            BigDecimal change;
            MixedPayment mixedPayment = null;

            if (campaignIds != null && !campaignIds.isEmpty()) {
                CampaignProcessResult campaignProcessResult = processCampaigns(campaignIds, products, total);
                products = campaignProcessResult.getProducts();
                totalWithCampaign = campaignProcessResult.getTotalWithCampaign();
                campaigns = campaignProcessResult.getCampaigns();
            }

            change = switch (paymentMethod) {
                case CASH -> {
                    cash = saleRequest.getCash();
                    yield processCashPayment(cash, totalWithCampaign);
                }
                case MIXED -> {
                    mixedPayment = saleRequest.getMixedPayment();
                    yield processMixedPayment(mixedPayment, totalWithCampaign);
                }
                default -> null;
            };

            existingSale.setCashier(saleRequest.getCashier());
            existingSale.setDate(new Date());
            existingSale.setCampaigns(campaigns);
            existingSale.setCash(cash);
            existingSale.setChange(change);
            existingSale.setTotal(total);
            existingSale.setTotalWithCampaign(totalWithCampaign);
            existingSale.setMixedPayment(mixedPayment);

            saleRepository.save(existingSale);
            products.forEach(product -> product.setSale(existingSale));
            productRepository.saveAll(products);

            reduceStocks(products);

            existingSale.setProducts(products);
            return mapToSaleResponse(existingSale);

        } catch (Exception e) {
            reduceStocks(oldProducts);
            throw new SaleUpdateException("Failed to update the sale with ID " + id, e);
        }
    }

    @Override
    public void cancelSale(Long id){
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Sale doesn't exist with id " + id));

        List<Product> oldProducts = existingSale.getProducts();
        returnProducts(oldProducts);

        existingSale.setCancelled(true);
        saleRepository.save(existingSale);
    }


    @Override
    public SaleResponse restoreSale(Long id) {
        if (!saleRepository.existsById(id))
            throw new SaleNotFoundException("Sale not found with id " + id);

        if (!saleRepository.isSaleSoftDeleted(id)) {
            throw new SaleNotSoftDeletedException("Sale with id " + id + " is not soft-deleted and cannot be restored.");
        }
        productRepository.restoreProductsBySaleId(id);
        saleRepository.restoreSale(id);
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new SaleNotFoundException("Couldn't restore the sale with id " + id));
        return mapToSaleResponse(sale);
    }

    @Override
    public void deleteSale(Long id) {
        logger.info("Deleting sale with ID: {}", id);
        if (!saleRepository.existsById(id))
            throw new SaleNotFoundException("Sale not found with id " + id);

        saleRepository.deleteById(id);
        productRepository.deleteAllBySaleId(id);
        logger.info("Sale deleted!");
    }

    @Override
    public void deleteSalePermanently(Long id) {
        if (!saleRepository.existsById(id))
            throw new SaleNotFoundException("Sale not found with id " + id);

        saleRepository.deleteCampaignsForSale(id);
        saleRepository.deleteProductsForSale(id);
        saleRepository.deletePermanently(id);
    }


    private List<Product> getProducts(List<ProductRequest> productRequests) throws HeaderProcessingException {

        List<Product> products = new ArrayList<>();

        for (var productRequest : productRequests) {
            try {
                ProductServiceResponse productServiceResponse = gatewayService.getProduct(productRequest.getId());

                if (productServiceResponse != null) {
                    ProductStockCheckRequest productStockCheckRequest = new ProductStockCheckRequest(productRequest.getId(), productRequest.getQuantity());
                    boolean areEnoughProductsInStock = Boolean.TRUE.equals(gatewayService.checkEnoughProductsInStock(productStockCheckRequest).block());

                    if (areEnoughProductsInStock) {
                        BigDecimal totalPrice = productServiceResponse.getPrice().multiply(BigDecimal.valueOf(productRequest.getQuantity()));
                        products.add(Product.builder()
                                .productId(productServiceResponse.getId())
                                .name(productServiceResponse.getName())
                                .barcodeNumber(productServiceResponse.getBarcodeNumber())
                                .price(productServiceResponse.getPrice())
                                .quantity(productRequest.getQuantity())
                                .totalPrice(totalPrice)
                                .build());
                    } else {
                        throw new ProductOutOfStockException("Not enough stock for product with id: " + productRequest.getId());
                    }
                }

            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ProductNotFoundException("Product not found with id: " + productRequest.getId());
                } else {
                    throw new ProductServiceException("Error calling Product service: " + ex.getMessage());
                }
            }
        }
        return products;
    }

    private BigDecimal getTotal(List<Product> products) {
        return products.stream()
                .map(Product::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CampaignProcessResult processCampaigns(List<Long> campaignIds, List<Product> products, BigDecimal total) {
        CampaignProcessRequest campaignProcessRequest = CampaignProcessRequest.builder()
                .campaignIds(campaignIds)
                .products(products)
                .total(total).build();
        CampaignProcessResponse campaignProcessResponse = campaignProcessService.processCampaigns(campaignProcessRequest);

        List<Campaign> campaigns = campaignProcessService.getCampaigns(campaignIds);
        products = campaignProcessResponse.getProducts();
        BigDecimal totalWithCampaign = campaignProcessResponse.getTotal();


        return new CampaignProcessResult(campaigns, products, totalWithCampaign);
    }

    private List<ProductResponse> mapToProductResponse(List<Product> products) {
        return products.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .productId(product.getProductId())
                        .name(product.getName())
                        .barcodeNumber(product.getBarcodeNumber())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .totalPrice(product.getTotalPrice())
                        .saleId(product.getSale().getId())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> getCampaignNames(Sale sale) {
        List<String> campaignNames = List.of();
        if (sale.getCampaigns() != null && !sale.getCampaigns().isEmpty())
            campaignNames = sale.getCampaigns().stream().map(Campaign::getName).toList();
        return campaignNames;
    }

    private BigDecimal processCashPayment(BigDecimal cash, BigDecimal totalWithCampaign) {
        if (cash == null)
            throw new CashNotProvidedException("Cash not provided");

        if (cash.compareTo(totalWithCampaign) < 0) {
            throw new InsufficientCashException("Insufficient cash for payment");
        }
        return cash.subtract(totalWithCampaign);
    }

    private BigDecimal processMixedPayment(MixedPayment mixedPayment, BigDecimal totalWithCampaign) {
        if (mixedPayment == null) {
            throw new MixedPaymentNotFoundException("Mixed payment not found");
        }
        BigDecimal cashAmount = mixedPayment.getCashAmount();
        BigDecimal creditCardAmount = mixedPayment.getCreditCardAmount();

        if (cashAmount == null || creditCardAmount == null)
            throw new InvalidMixedPaymentException("Invalid mixed payment");

        BigDecimal amountPaid = cashAmount.add(creditCardAmount);

        if (amountPaid.compareTo(totalWithCampaign) < 0) {
            throw new InsufficientMixedPaymentException("The total payment is not enough to cover the sale amount.");
        }

        BigDecimal amountToBePaidByCash = totalWithCampaign.subtract(creditCardAmount);
        return cashAmount.subtract(amountToBePaidByCash);
    }

    private Payment getPaymentMethod(String paymentMethod) {
        try {
            return Payment.valueOf(paymentMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentMethodException("Invalid payment method: " + paymentMethod);
        }
    }

    private void reduceStocks(List<Product> products) {
        products.forEach(product -> {
            ProductStockReduceRequest productStockReduceRequest = new ProductStockReduceRequest(product.getProductId(), product.getQuantity());
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, productStockReduceRequest);
        });
    }

    private void returnProducts(List<Product> products) {
        products.forEach(product -> {
            try {
            ProductStockReturnRequest productStockReturnRequest = new ProductStockReturnRequest(product.getProductId(), product.getQuantity());
                gatewayService.returnProducts(productStockReturnRequest);
            } catch (HeaderProcessingException e) {
                throw new RuntimeException("Failed to return products");
            }
        });
    }

    private SaleResponse mapToSaleResponse(Sale sale) {
        List<ProductResponse> productResponses = mapToProductResponse(sale.getProducts());
        List<String> campaignNames = getCampaignNames(sale);

        return SaleResponse.builder()
                .id(sale.getId())
                .cashier(sale.getCashier())
                .date(sale.getDate())
                .paymentMethod(sale.getPaymentMethod().toString())
                .campaignNames(campaignNames)
                .products(productResponses)
                .cash(sale.getCash())
                .change(sale.getChange())
                .total(sale.getTotal())
                .totalWithCampaign(sale.getTotalWithCampaign())
                .mixedPayment(sale.getMixedPayment()).build();
    }

}
