package com.bit.saleservice.service;

import com.bit.saleservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.saleservice.dto.*;
import com.bit.saleservice.entity.*;
import com.bit.saleservice.exception.*;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import com.bit.saleservice.wrapper.PageWrapper;
import com.bit.saleservice.wrapper.ProductStockReduceRequest;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey}")
    private String ROUTING_KEY;

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CampaignProcessService campaignProcessService;
    private final GatewayService gatewayService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public SaleResponse getSale(Long id) {
        log.trace("Entering getSale method in SaleServiceImpl class with id: {}", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Sale not found with id: {}", id);
                    return new SaleNotFoundException("Sale not found with id " + id);
                });
        log.info("Sale retrieved successfully with id: {}", id);

        log.trace("Exiting getSale method in SaleServiceImpl class");
        return mapToSaleResponse(sale);
    }

    @Override
    public List<SaleResponse> getAllSales() {
        log.trace("Entering getAllSales method in SaleServiceImpl class");

        List<Sale> sales = saleRepository.findAll();
        log.info("Retrieved all sales successfully, count: {}", sales.size());

        log.trace("Exiting getAllSales method in SaleServiceImpl class");
        return sales.stream().map(this::mapToSaleResponse).toList();
    }

    @Override
    public List<SaleResponse> getDeletedSales() {
        log.trace("Entering getDeletedSales method in SaleServiceImpl class");

        List<Sale> sales = saleRepository.findSoftDeletedSales();
        log.info("Retrieved all deleted sales successfully, count: {}", sales.size());

        log.trace("Exiting getDeletedSales method in SaleServiceImpl class");
        return sales.stream().map(this::mapToSaleResponse).toList();
    }

    @Override
    public PageWrapper<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                                  String cashier, String paymentMethod,
                                                                  BigDecimal minTotal, BigDecimal maxTotal,
                                                                  String startDate, String endDate) {
        log.trace("Entering getAllSalesFilteredAndSorted method in SaleServiceImpl class");

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Sale> salesPage = saleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicates(cashier, paymentMethod, minTotal, maxTotal, startDate, endDate, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        Page<SaleResponse> saleResponsePage = salesPage.map(this::mapToSaleResponse);
        log.info("Retrieved filtered and sorted sales successfully, total elements: {}", saleResponsePage.getTotalElements());

        PageWrapper<SaleResponse> saleResponsePageWrapper = new PageWrapper<>();
        saleResponsePageWrapper.setContent(saleResponsePage.getContent());
        saleResponsePageWrapper.setPageNumber(page);
        saleResponsePageWrapper.setPageSize(size);
        saleResponsePageWrapper.setTotalElements(saleResponsePage.getTotalElements());

        log.trace("Exiting getAllSalesFilteredAndSorted method in SaleServiceImpl class");
        return saleResponsePageWrapper;
    }


    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering createSale method in SaleServiceImpl class");

        log.debug("SaleRequest: {}", saleRequest);
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
        log.info("Sale created successfully with id: {}", sale.getId());

        log.trace("Exiting createSale method in SaleServiceImpl class");
        return mapToSaleResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering updateSale method in SaleServiceImpl class with id: {}", id);

        log.debug("SaleRequest: {}", saleRequest);
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Sale doesn't exist with id: {}", id);
                    return new SaleNotFoundException("Sale doesn't exist with id " + id);
                });

        Payment paymentMethod = getPaymentMethod(saleRequest.getPaymentMethod());
        if (paymentMethod != existingSale.getPaymentMethod()) {
            log.warn("Payment method update not allowed for sale id: {}", id);
            throw new PaymentMethodUpdateNotAllowedException("Payment method update not allowed");
        }

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
            log.info("Sale updated successfully with id: {}", existingSale.getId());

            log.trace("Exiting updateSale method in SaleServiceImpl class");
            return mapToSaleResponse(existingSale);

        } catch (Exception e) {
            log.error("Error updating sale with id: {}", id, e);
            reduceStocks(oldProducts);
            throw e;
        }
    }

    @Override
    public void cancelSale(Long id) {
        log.trace("Entering cancelSale method in SaleServiceImpl class with id: {}", id);

        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Sale doesn't exist with id: {}", id);
                    return new SaleNotFoundException("Sale doesn't exist with id " + id);
                });

        List<Product> oldProducts = existingSale.getProducts();
        returnProducts(oldProducts);

        existingSale.setCancelled(true);
        saleRepository.save(existingSale);
        log.info("Sale cancelled successfully with id: {}", id);

        log.trace("Exiting cancelSale method in SaleServiceImpl class");
    }


    @Override
    public SaleResponse restoreSale(Long id) {
        log.trace("Entering restoreSale method in SaleServiceImpl class with id: {}", id);

        if (!saleRepository.existsByIdAndDeletedTrue(id)) {
            log.error("Sale with id {} is not soft-deleted and cannot be restored", id);
            throw new SaleNotSoftDeletedException("Sale with id " + id + " is not soft-deleted and cannot be restored.");
        }

        productRepository.restoreProductsBySaleId(id);
        saleRepository.restoreSale(id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Sale not found after attempting to restore with id: {}", id);
                    return new SaleNotFoundException("Sale could not restored and not found with id " + id);
                });
        log.info("Sale restored successfully with id: {}", id);

        log.trace("Exiting restoreSale method in SaleServiceImpl class");
        return mapToSaleResponse(sale);
    }

    @Override
    public void deleteSale(Long id) {
        log.trace("Entering deleteSale method in SaleServiceImpl class with id: {}", id);

        if (!saleRepository.existsById(id)) {
            log.error("Sale doesn't exist with id: {}", id);
            throw new SaleNotFoundException("Sale doesn't exist with id " + id);
        }

        saleRepository.deleteById(id);
        productRepository.deleteAllBySaleId(id);

        log.info("Sale soft-deleted successfully with id: {}", id);
        log.trace("Exiting deleteSale method in SaleServiceImpl class");
    }

    @Override
    public void deleteSalePermanently(Long id) {
        log.trace("Entering deleteSalePermanently method in SaleServiceImpl class with id: {}", id);

        if (!saleRepository.existsById(id)) {
            log.error("Sale doesn't exist with id: {}", id);
            throw new SaleNotFoundException("Sale doesn't exist with id " + id);
        }

        saleRepository.deleteCampaignsForSale(id);
        saleRepository.deleteProductsForSale(id);
        saleRepository.deletePermanently(id);
        log.info("Sale permanently deleted successfully with id: {}", id);

        log.trace("Exiting deleteSalePermanently method in SaleServiceImpl class");
    }

    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates(String cashier, String paymentMethod,
                                          BigDecimal minTotal, BigDecimal maxTotal,
                                          String startDate, String endDate,
                                          Root<Sale> root, CriteriaBuilder criteriaBuilder) {
        log.trace("Entering getPredicates method with parameters: cashier={}, paymentMethod={}, minTotal={}, maxTotal={}, startDate={}, endDate={}",
                cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(cashier)) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("cashier")), "%" + cashier.toLowerCase() + "%"));
            log.debug("Added predicate for cashier: {}", cashier);
        }
        if (StringUtils.isNotBlank(paymentMethod)) {
            try {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), Payment.valueOf(paymentMethod)));
                log.debug("Added predicate for paymentMethod: {}", paymentMethod);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid payment method value: {}", paymentMethod, e);
                throw new InvalidPaymentMethodException("Invalid payment method value: " + paymentMethod, e);
            }
        }
        if (minTotal != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalWithCampaign"), minTotal));
            log.debug("Added predicate for minTotal: {}", minTotal);
        }
        if (maxTotal != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalWithCampaign"), maxTotal));
            log.debug("Added predicate for maxTotal: {}", maxTotal);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (StringUtils.isNotBlank(startDate)) {
            try {
                Date date = dateFormat.parse(startDate);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), date));
                log.debug("Added predicate for startDate: {}", startDate);
            } catch (ParseException e) {
                log.error("Error parsing startDate: {}", startDate, e);
                throw new ParsingException("Error parsing startDate: ", e);
            }
        }
        if (StringUtils.isNotBlank(endDate)) {
            try {
                Date date = dateFormat.parse(endDate);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), date));
                log.debug("Added predicate for endDate: {}", endDate);
            } catch (ParseException e) {
                log.error("Error parsing endDate: {}", endDate, e);
                throw new ParsingException("Error parsing endDate: ", e);
            }
        }

        log.trace("Exiting getPredicates method with {} predicates", predicates.size());
        return predicates;
    }

    @ExcludeFromGeneratedCoverage
    private List<Product> getProducts(List<SaleProductRequest> saleProductRequests) throws HeaderProcessingException {
        log.trace("Entering getProducts method in SaleServiceImpl class");

        List<Product> products = new ArrayList<>();

        for (var productRequest : saleProductRequests) {
            try {
                ProductResponse productResponse = gatewayService.getProduct(productRequest.getId());

                if (productResponse != null) {
                    boolean areEnoughProductsInStock = Boolean.TRUE.equals(productResponse.getStockQuantity() >= productRequest.getQuantity());

                    if (areEnoughProductsInStock) {
                        BigDecimal totalPrice = productResponse.getPrice().multiply(BigDecimal.valueOf(productRequest.getQuantity()));
                        products.add(Product.builder()
                                .productId(productResponse.getId())
                                .name(productResponse.getName())
                                .barcodeNumber(productResponse.getBarcodeNumber())
                                .price(productResponse.getPrice())
                                .quantity(productRequest.getQuantity())
                                .totalPrice(totalPrice)
                                .build());
                    } else {
                        log.error("Not enough stock for product with id: " + productRequest.getId());
                        throw new ProductOutOfStockException("Not enough stock for product with id: " + productRequest.getId());
                    }
                }

            } catch (HttpClientErrorException ex) {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new ProductNotFoundException("Product not found with id: " + productRequest.getId());
                } else {
                    log.error("Error calling Product service: " + ex.getMessage());
                    throw new ProductServiceException("Error calling Product service: " + ex.getMessage());
                }
            }
        }

        log.trace("Exiting getProducts method in SaleServiceImpl class");
        return products;
    }

    private BigDecimal getTotal(List<Product> products) {
        log.trace("Entering getTotal method in SaleServiceImpl class");
        log.trace("Exiting getTotal method in SaleServiceImpl class");
        return products.stream()
                .map(Product::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected CampaignProcessResult processCampaigns(List<Long> campaignIds, List<Product> products, BigDecimal total) {
        log.trace("Entering processCampaigns method in SaleServiceImpl class");

        CampaignProcessRequest campaignProcessRequest = CampaignProcessRequest.builder()
                .campaignIds(campaignIds)
                .products(products)
                .total(total).build();
        CampaignProcessResponse campaignProcessResponse = campaignProcessService.processCampaigns(campaignProcessRequest);

        List<Campaign> campaigns = campaignProcessService.getCampaigns(campaignIds);
        products = campaignProcessResponse.getProducts();
        BigDecimal totalWithCampaign = campaignProcessResponse.getTotal();

        log.trace("Exiting processCampaigns method in SaleServiceImpl class");
        return new CampaignProcessResult(campaigns, products, totalWithCampaign);
    }

    private List<SaleProductResponse> mapToProductResponse(List<Product> products) {
        log.trace("Entering mapToProductResponse method in SaleServiceImpl class");
        log.trace("Exiting mapToProductResponse method in SaleServiceImpl class");
        return products.stream()
                .map(product -> SaleProductResponse.builder()
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
        log.trace("Entering getCampaignNames method in SaleServiceImpl class");

        List<String> campaignNames = null;
        if (sale.getCampaigns() != null && !sale.getCampaigns().isEmpty()) {
            campaignNames = sale.getCampaigns().stream().map(Campaign::getName).toList();
        }

        log.trace("Exiting getCampaignNames method in SaleServiceImpl class");
        return campaignNames;
    }

    protected BigDecimal processCashPayment(BigDecimal cash, BigDecimal totalWithCampaign) {
        log.trace("Entering processCashPayment method in SaleServiceImpl class");

        if (cash == null) {
            log.error("Cash not provided");
            throw new CashNotProvidedException("Cash not provided");
        }

        if (cash.compareTo(totalWithCampaign) < 0) {
            log.error("Cash is not enough to cover the sale amount.");
            throw new InsufficientCashException("Insufficient cash for payment");
        }

        log.trace("Exiting processCashPayment method in SaleServiceImpl class");
        return cash.subtract(totalWithCampaign);
    }

    protected BigDecimal processMixedPayment(MixedPayment mixedPayment, BigDecimal totalWithCampaign) {
        log.trace("Entering processMixedPayment method in SaleServiceImpl class");

        if (mixedPayment == null) {
            log.error("Mixed payment not found");
            throw new MixedPaymentNotFoundException("Mixed payment not found");
        }
        BigDecimal cashAmount = mixedPayment.getCashAmount();
        BigDecimal creditCardAmount = mixedPayment.getCreditCardAmount();

        if (cashAmount == null || creditCardAmount == null) {
            log.error("Invalid mixed payment");
            throw new InvalidMixedPaymentException("Invalid mixed payment");
        }

        BigDecimal amountPaid = cashAmount.add(creditCardAmount);

        if (amountPaid.compareTo(totalWithCampaign) < 0) {
            log.error("The total payment is not enough to cover the sale amount.");
            throw new InsufficientMixedPaymentException("The total payment is not enough to cover the sale amount.");
        }

        BigDecimal amountToBePaidByCash = totalWithCampaign.subtract(creditCardAmount);

        log.trace("Exiting processMixedPayment method in SaleServiceImpl class");
        return cashAmount.subtract(amountToBePaidByCash);
    }

    @ExcludeFromGeneratedCoverage
    private Payment getPaymentMethod(String paymentMethod) {
        log.trace("Entering getPaymentMethod method in SaleServiceImpl class");

        try {
            return Payment.valueOf(paymentMethod.toUpperCase());

        } catch (IllegalArgumentException e) {
            log.error("Invalid payment method: {}", paymentMethod);
            throw new InvalidPaymentMethodException("Invalid payment method: " + paymentMethod);

        } finally {
            log.trace("Exiting getPaymentMethod method in SaleServiceImpl class");
        }
    }

    protected void reduceStocks(List<Product> products) {
        log.trace("Entering reduceStocks method in SaleServiceImpl class with products: {}", products);

        products.forEach(product -> {
            ProductStockReduceRequest productStockReduceRequest = new ProductStockReduceRequest(product.getProductId(), product.getQuantity());
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, productStockReduceRequest);
                log.debug("Stock reduce request sent for product with id: {}", product.getId());

            } catch (Exception e) {
                log.error("Failed to send reduce message to RabbitMQ", e);
                throw new RabbitMQException("Failed to send reduce message to RabbitMQ", e);
            }
        });
        log.info("Stocks reduced successfully for products: {}", products);

        log.trace("Exiting reduceStocks method in SaleServiceImpl class");
    }

    private void returnProducts(List<Product> products) {
        log.trace("Entering returnProducts method in SaleServiceImpl class");

        products.forEach(product -> {
            ProductStockReturnRequest productStockReturnRequest = new ProductStockReturnRequest(product.getProductId(), product.getQuantity());
            try {
                gatewayService.returnProducts(productStockReturnRequest);
            } catch (HeaderProcessingException | ProductReturnException e) {
                log.error("Failed to return product", e);
                throw new RuntimeException(e);
            }
        });
        log.info("Products returned successfully");

        log.trace("Exiting returnProducts method in SaleServiceImpl class");
    }

    private SaleResponse mapToSaleResponse(Sale sale) {
        log.trace("Entering mapToSaleResponse method in SaleServiceImpl class");

        List<SaleProductResponse> saleProductResponses = mapToProductResponse(sale.getProducts());
        List<String> campaignNames = getCampaignNames(sale);

        log.trace("Exiting mapToSaleResponse method in SaleServiceImpl class");
        return SaleResponse.builder()
                .id(sale.getId())
                .cashier(sale.getCashier())
                .date(sale.getDate())
                .paymentMethod(sale.getPaymentMethod().toString())
                .campaignNames(campaignNames)
                .products(saleProductResponses)
                .cash(sale.getCash())
                .change(sale.getChange())
                .total(sale.getTotal())
                .totalWithCampaign(sale.getTotalWithCampaign())
                .mixedPayment(sale.getMixedPayment()).build();
    }

}
