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
import org.springframework.amqp.AmqpException;
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

/**
 * Service class for managing sales.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.routingKey}")
    private String ROUTING_KEY;

    private final static String NOT_FOUND_ERROR_MESSAGE = "Sale not found with id: ";
    private final static String NOT_EXIST_ERROR_MESSAGE = "Sale does not exist with id: ";

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
                    log.error(NOT_FOUND_ERROR_MESSAGE + id);
                    return new SaleNotFoundException(NOT_FOUND_ERROR_MESSAGE + id);
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
                                                                  String startDate, String endDate, Boolean isCancelled) {
        log.trace("Entering getAllSalesFilteredAndSorted method in SaleServiceImpl class");

        // Creating the pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);

        // Getting the sale page object
        Page<Sale> salesPage = saleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicates(cashier, paymentMethod, minTotal, maxTotal, startDate, endDate, isCancelled, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        Page<SaleResponse> saleResponsePage = salesPage.map(this::mapToSaleResponse);
        log.info("Retrieved filtered and sorted sales successfully, total elements: {}", saleResponsePage.getTotalElements());

        // Creating a PageWrapper object
        PageWrapper<SaleResponse> saleResponsePageWrapper = new PageWrapper<>();
        saleResponsePageWrapper.setContent(saleResponsePage.getContent());
        saleResponsePageWrapper.setPageNumber(page);
        saleResponsePageWrapper.setPageSize(size);
        saleResponsePageWrapper.setTotalElements(saleResponsePage.getTotalElements());
        log.debug("SaleResponsePageWrapper created with the size: " + size);

        log.trace("Exiting getAllSalesFilteredAndSorted method in SaleServiceImpl class");
        return saleResponsePageWrapper;
    }


    @Override
    @Transactional
    public SaleResponse createSale(SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering createSale method in SaleServiceImpl class");

        // Defining the variables
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

        // Checking the campaign ids
        if (campaignIds != null && !campaignIds.isEmpty()) {
            log.debug("Sale has campaigns");
            CampaignProcessResult campaignProcessResult = processCampaigns(campaignIds, products, total);

            products = campaignProcessResult.getProducts();
            totalWithCampaign = campaignProcessResult.getTotalWithCampaign();
            campaigns = campaignProcessResult.getCampaigns();
        }

        // Processing the payment
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
        log.debug("Payment processed");

        // Creating the sale
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
        log.debug("Sale saved");

        // Sending a reduce message to the RabbitMQ
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

        // Finding the existing sale
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(NOT_EXIST_ERROR_MESSAGE + id);
                    return new SaleNotFoundException(NOT_EXIST_ERROR_MESSAGE + id);
                });

        // Checking whether the payment method is the same as the existing sale's payment method
        Payment paymentMethod = getPaymentMethod(saleRequest.getPaymentMethod());
        if (paymentMethod != existingSale.getPaymentMethod()) {
            log.warn("Payment method update not allowed for sale id: {}", id);
            throw new PaymentMethodUpdateNotAllowedException("Payment method update not allowed");
        }
        log.debug("Payment method is the same as the existing sale");

        List<Product> oldProducts = existingSale.getProducts();
        returnProducts(oldProducts);

        try {
            // Defining the variables
            List<Product> products = getProducts(saleRequest.getProducts());
            BigDecimal total = getTotal(products);
            BigDecimal totalWithCampaign = null;
            List<Long> campaignIds = saleRequest.getCampaignIds();
            List<Campaign> campaigns = null;
            BigDecimal cash = null;
            BigDecimal change;
            MixedPayment mixedPayment = null;

            // Checking the campaign ids
            if (campaignIds != null && !campaignIds.isEmpty()) {
                log.debug("Sale has campaigns");
                CampaignProcessResult campaignProcessResult = processCampaigns(campaignIds, products, total);

                products = campaignProcessResult.getProducts();
                totalWithCampaign = campaignProcessResult.getTotalWithCampaign();
                campaigns = campaignProcessResult.getCampaigns();
            }

            // Processing the payment
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
            log.debug("Payment processed");

            // Setting the new values
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
            log.debug("Sale saved");

            // Sending a reduce message to the RabbitMQ
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

        // Finding the existing sale
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(NOT_EXIST_ERROR_MESSAGE + id);
                    return new SaleNotFoundException(NOT_EXIST_ERROR_MESSAGE + id);
                });
        log.debug("Existing sale found");

        // Returning the products
        List<Product> oldProducts = existingSale.getProducts();
        returnProducts(oldProducts);
        log.debug("Products returned");

        existingSale.setCancelled(true);
        saleRepository.save(existingSale);
        log.info("Sale cancelled successfully with id: {}", id);

        log.trace("Exiting cancelSale method in SaleServiceImpl class");
    }


    @Override
    public SaleResponse restoreSale(Long id) {
        log.trace("Entering restoreSale method in SaleServiceImpl class with id: {}", id);

        // Checking whether the sale is soft-deleted
        if (!saleRepository.existsByIdAndDeletedTrue(id)) {
            log.error("Sale with id {} is not soft-deleted and cannot be restored", id);
            throw new SaleNotSoftDeletedException("Sale with id " + id + " is not soft-deleted and cannot be restored.");
        }
        log.debug("Sale is soft-deleted");

        productRepository.restoreProductsBySaleId(id);
        saleRepository.restoreSale(id);

        // Finding the sale
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

        // Checking whether the sale exists
        if (!saleRepository.existsById(id)) {
            log.error(NOT_EXIST_ERROR_MESSAGE + id);
            throw new SaleNotFoundException(NOT_EXIST_ERROR_MESSAGE + id);
        }
        log.debug("Sale exists with id: {}", id);

        saleRepository.deleteById(id);
        productRepository.deleteAllBySaleId(id);

        log.info("Sale soft-deleted successfully with id: {}", id);
        log.trace("Exiting deleteSale method in SaleServiceImpl class");
    }

    @Override
    public void deleteSalePermanently(Long id) {
        log.trace("Entering deleteSalePermanently method in SaleServiceImpl class with id: {}", id);

        // Checking whether the sale exists
        if (!saleRepository.existsById(id)) {
            log.error(NOT_EXIST_ERROR_MESSAGE + id);
            throw new SaleNotFoundException(NOT_EXIST_ERROR_MESSAGE + id);
        }
        log.debug("Sale exists with id: {}", id);

        saleRepository.deleteCampaignsForSale(id);
        saleRepository.deleteProductsForSale(id);
        saleRepository.deletePermanently(id);
        log.info("Sale permanently deleted successfully with id: {}", id);

        log.trace("Exiting deleteSalePermanently method in SaleServiceImpl class");
    }

    /**
     * This method generates a list of predicates for filtering sales based on the given parameters.
     *
     * @param cashier         The cashier name to filter sales by.
     * @param paymentMethod   The payment method to filter sales by.
     * @param minTotal        The minimum total amount to filter sales by.
     * @param maxTotal        The maximum total amount to filter sales by.
     * @param startDate       The start date to filter sales by.
     * @param endDate         The end date to filter sales by.
     * @param isCancelled     The boolean on whether it is cancelled to filter by.
     * @param root            The root of the CriteriaQuery.
     * @param criteriaBuilder The CriteriaBuilder for creating predicates.
     * @return A list of predicates for filtering sales.
     */
    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates(String cashier, String paymentMethod,
                                          BigDecimal minTotal, BigDecimal maxTotal,
                                          String startDate, String endDate,
                                          Boolean isCancelled,
                                          Root<Sale> root, CriteriaBuilder criteriaBuilder) {
        log.trace("Entering getPredicates method with parameters: cashier={}, paymentMethod={}, minTotal={}, maxTotal={}, startDate={}, endDate={}, isCancelled={}",
                cashier, paymentMethod, minTotal, maxTotal, startDate, endDate, isCancelled);

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

        // Adding total query parameters
        if (minTotal != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalWithCampaign"), minTotal));
            log.debug("Added predicate for minTotal: {}", minTotal);
        }
        if (maxTotal != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalWithCampaign"), maxTotal));
            log.debug("Added predicate for maxTotal: {}", maxTotal);
        }

        // Adding date query paramters
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

        // Adding cancelled query parameter
        if (isCancelled != null) {
            predicates.add(criteriaBuilder.equal(root.get("cancelled"), isCancelled));
            log.debug("Added predicate for isCancelled: {}", isCancelled);
        }

        log.trace("Exiting getPredicates method with {} predicates", predicates.size());
        return predicates;
    }

    /**
     * This method retrieves a list of products for a sale based on the given product requests.
     * It calls the Product service to fetch the details of each product and checks if there is enough stock.
     * If there is enough stock, it creates a Product object and adds it to the list.
     * If there is not enough stock, it throws a ProductOutOfStockException.
     * If the Product service returns a 404 Not Found status, it throws a ProductNotFoundException.
     * If any other error occurs during the process, it throws a ProductServiceException.
     *
     * @param saleProductRequests The list of product requests for the sale.
     * @return A list of Product objects representing the products for the sale.
     * @throws HeaderProcessingException  If there is an error processing the header.
     * @throws ProductOutOfStockException If there is not enough stock for a product.
     * @throws ProductNotFoundException   If the Product service returns a 404 Not Found status.
     * @throws ProductServiceException    If any other error occurs during the process.
     */
    @ExcludeFromGeneratedCoverage
    private List<Product> getProducts(List<SaleProductRequest> saleProductRequests) throws HeaderProcessingException {
        log.trace("Entering getProducts method in SaleServiceImpl class");

        List<Product> products = new ArrayList<>();

        for (var productRequest : saleProductRequests) {
            try {
                // Calling the getProduct method in Gateway Service
                ProductResponse productResponse = gatewayService.getProduct(productRequest.getId());

                if (productResponse != null) {

                    // Checking whether there are enough stock for the product
                    boolean areEnoughProductsInStock = Boolean.TRUE.equals(productResponse.getStockQuantity() >= productRequest.getQuantity());

                    if (areEnoughProductsInStock) {
                        log.debug("There are enough products for the product " + productRequest.getId() + " in stock");
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

    /**
     * This method calculates the total price of all products in the given list.
     *
     * @param products The list of products for which the total price needs to be calculated.
     * @return The total price of all products in the given list.
     */
    private BigDecimal getTotal(List<Product> products) {
        log.trace("Entering getTotal method in SaleServiceImpl class");
        log.trace("Exiting getTotal method in SaleServiceImpl class");
        return products.stream()
                .map(Product::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * This method processes the campaigns for a sale. It calls the Campaign service to apply the campaigns
     * to the products and calculates the total price after applying the campaigns.
     *
     * @param campaignIds The list of campaign IDs to apply to the products.
     * @param products    The list of products for which the campaigns need to be applied.
     * @param total       The total price of the products before applying the campaigns.
     * @return A CampaignProcessResult object containing the list of applied campaigns, the updated list of products,
     * and the total price after applying the campaigns.
     */
    protected CampaignProcessResult processCampaigns(List<Long> campaignIds, List<Product> products, BigDecimal total) {
        log.trace("Entering processCampaigns method in SaleServiceImpl class");

        // Creating a CampaignProcessRequest object
        CampaignProcessRequest campaignProcessRequest = CampaignProcessRequest.builder()
                .campaignIds(campaignIds)
                .products(products)
                .total(total).build();
        CampaignProcessResponse campaignProcessResponse = campaignProcessService.processCampaigns(campaignProcessRequest);
        log.debug("Got campaignProcessResponse");

        // Setting results from the campaignProcessResponse object
        List<Campaign> campaigns = campaignProcessService.getCampaigns(campaignIds);
        products = campaignProcessResponse.getProducts();
        BigDecimal totalWithCampaign = campaignProcessResponse.getTotal();

        log.trace("Exiting processCampaigns method in SaleServiceImpl class");
        return new CampaignProcessResult(campaigns, products, totalWithCampaign);
    }

    /**
     * This method maps a list of Product objects to a list of SaleProductResponse objects.
     * Each SaleProductResponse object represents a product in a sale, and contains the necessary information.
     *
     * @param products The list of Product objects to be mapped.
     * @return A list of SaleProductResponse objects representing the products in a sale.
     */
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

    /**
     * This method retrieves the names of the campaigns applied to a sale.
     *
     * @param sale The sale object for which the campaign names need to be retrieved.
     * @return A list of strings representing the names of the campaigns applied to the sale.
     * If no campaigns are applied, it returns null.
     */
    private List<String> getCampaignNames(Sale sale) {
        log.trace("Entering getCampaignNames method in SaleServiceImpl class");

        List<String> campaignNames = null;
        if (sale.getCampaigns() != null && !sale.getCampaigns().isEmpty()) {
            campaignNames = sale.getCampaigns().stream().map(Campaign::getName).toList();
        }

        log.trace("Exiting getCampaignNames method in SaleServiceImpl class");
        return campaignNames;
    }

    /**
     * This method processes the cash payment for a sale.
     * It checks if the cash amount is provided and if it is enough to cover the total amount of the sale.
     * If the cash amount is not provided, it throws a CashNotProvidedException.
     * If the cash amount is not enough, it throws an InsufficientCashException.
     * If the cash amount is sufficient, it calculates and returns the change amount.
     *
     * @param cash              The cash amount provided by the customer.
     * @param totalWithCampaign The total amount of the sale after applying the campaigns.
     * @return The change amount after processing the cash payment.
     * @throws CashNotProvidedException  If the cash amount is not provided.
     * @throws InsufficientCashException If the cash amount is not enough to cover the sale amount.
     */
    protected BigDecimal processCashPayment(BigDecimal cash, BigDecimal totalWithCampaign) {
        log.trace("Entering processCashPayment method in SaleServiceImpl class");

        // Check whether cash is provided
        if (cash == null) {
            log.error("Cash not provided");
            throw new CashNotProvidedException("Cash not provided");
        }
        log.debug("Cash is provided");

        // Check whether cash is enough to cover the sale amount
        if (cash.compareTo(totalWithCampaign) < 0) {
            log.error("Cash is not enough to cover the sale amount.");
            throw new InsufficientCashException("Insufficient cash for payment");
        }
        log.debug("Cash is enough to cover the sale amount");

        log.trace("Exiting processCashPayment method in SaleServiceImpl class");
        return cash.subtract(totalWithCampaign);
    }

    /**
     * This method processes the mixed payment for a sale.
     * It checks if the mixed payment object is provided and if the cash and credit card amounts are valid.
     * If the mixed payment object is not provided or if the cash or credit card amounts are not valid, it throws an exception.
     * It calculates the total amount paid by the customer and checks if it is enough to cover the sale amount.
     * If the total amount paid is not enough, it throws an exception.
     * Finally, it calculates and returns the change amount to be given to the customer.
     *
     * @param mixedPayment      The mixed payment object provided by the customer.
     * @param totalWithCampaign The total amount of the sale after applying the campaigns.
     * @return The change amount to be given to the customer after processing the mixed payment.
     * @throws MixedPaymentNotFoundException     If the mixed payment object is not provided.
     * @throws InvalidMixedPaymentException      If the cash or credit card amounts in the mixed payment object are not valid.
     * @throws InsufficientMixedPaymentException If the total amount paid by the customer is not enough to cover the sale amount.
     */
    protected BigDecimal processMixedPayment(MixedPayment mixedPayment, BigDecimal totalWithCampaign) {
        log.trace("Entering processMixedPayment method in SaleServiceImpl class");

        // Check whether mixed payment is provided
        if (mixedPayment == null) {
            log.error("Mixed payment not found");
            throw new MixedPaymentNotFoundException("Mixed payment not found");
        }
        log.debug("Mixed payment is provided");

        BigDecimal cashAmount = mixedPayment.getCashAmount();
        BigDecimal creditCardAmount = mixedPayment.getCreditCardAmount();

        // Check whether cash and credit card amounts are provided
        if (cashAmount == null || creditCardAmount == null) {
            log.error("Invalid mixed payment");
            throw new InvalidMixedPaymentException("Invalid mixed payment");
        }
        log.debug("Cash and credit card amounts are provided");

        BigDecimal amountPaid = cashAmount.add(creditCardAmount);

        // Check whether the total amount paid is enough to cover the sale amount
        if (amountPaid.compareTo(totalWithCampaign) < 0) {
            log.error("The total payment is not enough to cover the sale amount.");
            throw new InsufficientMixedPaymentException("The total payment is not enough to cover the sale amount.");
        }
        log.debug("The total payment is enough to cover the sale amount");

        BigDecimal amountToBePaidByCash = totalWithCampaign.subtract(creditCardAmount);

        log.trace("Exiting processMixedPayment method in SaleServiceImpl class");
        return cashAmount.subtract(amountToBePaidByCash);
    }

    /**
     * This method retrieves the payment method for a sale.
     * It converts the given payment method string to an enum value and returns it.
     * If the given payment method string is not a valid enum value, it throws an InvalidPaymentMethodException.
     *
     * @param paymentMethod The payment method string to be converted to an enum value.
     * @return The payment method enum value corresponding to the given payment method string.
     * @throws InvalidPaymentMethodException If the given payment method string is not a valid enum value.
     */
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

    /**
     * This method reduces the stock quantity of the products in the given list.
     * It sends a message to RabbitMQ to reduce the stock for each product.
     * If an exception occurs during the process, it logs the error and throws a custom exception.
     *
     * @param products The list of products for which the stock needs to be reduced.
     */
    protected void reduceStocks(List<Product> products) {
        log.trace("Entering reduceStocks method in SaleServiceImpl class with products: {}", products);

        products.forEach(product -> {
            ProductStockReduceRequest productStockReduceRequest = new ProductStockReduceRequest(product.getProductId(), product.getQuantity());
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, productStockReduceRequest);
                log.debug("Stock reduce request sent for product with id: {}", product.getId());

            } catch (AmqpException e) {
                log.error("Failed to send reduce message to RabbitMQ", e);
                throw new RabbitMQException("Failed to send reduce message to RabbitMQ", e);
            }
        });
        log.info("Stocks reduced successfully for products: {}", products);

        log.trace("Exiting reduceStocks method in SaleServiceImpl class");
    }

    /**
     * This method returns the products to the inventory after a sale.
     * It sends a request to the Product service to return the products.
     * If any exception occurs during the process, it logs the error and throws a RuntimeException.
     *
     * @param products The list of products to be returned to the inventory.
     */
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

    /**
     * This method maps a Sale object to a SaleResponse object.
     * It extracts the necessary information from the Sale object and creates a SaleResponse object.
     * It includes the sale ID, cashier, date, payment method, names of applied campaigns, list of products,
     * cash amount, change amount, total amount, total amount after applying campaigns, and mixed payment details.
     *
     * @param sale The Sale object to be mapped to a SaleResponse object.
     * @return A SaleResponse object representing the sale.
     */
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
