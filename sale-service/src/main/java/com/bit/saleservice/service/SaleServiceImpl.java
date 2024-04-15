package com.bit.saleservice.service;

import com.bit.saleservice.dto.*;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Payment;
import com.bit.saleservice.entity.Product;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.InvalidPaymentMethodException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final String GATEWAY_URL = "http://localhost:8080/";
    private final String GET_ENDPOINT = "products/";

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CampaignProcessService campaignProcessService;
    private final RestTemplate restTemplate;

    public SaleResponse createSale(SaleRequest saleRequest) {

        List<Product> products = getProducts(saleRequest.getProducts());
        BigDecimal total = products.stream()
                .map(Product::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Payment payment;
        try {
            payment = Payment.valueOf(saleRequest.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentMethodException("Invalid payment method: " + saleRequest.getPaymentMethod());
        }

        List<Long> campaignIds = saleRequest.getCampaigns();
        List<Campaign> campaigns = null;
        if (campaignIds != null && !campaignIds.isEmpty()) {
            CampaignProcessRequest campaignProcessRequest = CampaignProcessRequest.builder()
                    .campaigns(saleRequest.getCampaigns())
                    .products(products)
                    .total(total).build();

            CampaignProcessResponse campaignProcessResponse = campaignProcessService.processCampaigns(campaignProcessRequest);
            products = campaignProcessResponse.getProducts();
            total = campaignProcessResponse.getTotal();

            campaigns = campaignProcessService.getCampaigns(campaignIds);
        }

        Sale sale = Sale.builder()
                .cashier(saleRequest.getCashier())
                .date(new Date())
                .paymentMethod(payment)
                .campaigns(campaigns)
                .products(products)
                .total(total).build();

        saleRepository.save(sale);
        saveAllProducts(products, sale);

        List<ProductResponse> productResponses = mapToProductResponse(products, sale.getId());

        return mapToSaleResponse(sale, productResponses);
    }

    private List<Product> getProducts (List<ProductRequest> productRequests) {

        List<Product> products = new ArrayList<>();

        for (var productRequest : productRequests) {
            try {
                ProductServiceResponse productServiceResponse = restTemplate
                        .getForObject(GATEWAY_URL + GET_ENDPOINT + productRequest.getId(), ProductServiceResponse.class);

                if (productServiceResponse != null) {
                    BigDecimal totalPrice = productServiceResponse.getPrice().multiply(BigDecimal.valueOf(productRequest.getQuantity()));
                    products.add(Product.builder()
                            .name(productServiceResponse.getName())
                            .barcodeNumber(productServiceResponse.getBarcodeNumber())
                            .price(productServiceResponse.getPrice())
                            .quantity(productRequest.getQuantity())
                            .totalPrice(totalPrice)
                            .build());
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
    private List<ProductResponse> mapToProductResponse(List<Product> products, Long id) {
        return products.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .barcodeNumber(product.getBarcodeNumber())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .totalPrice(product.getTotalPrice())
                        .saleId(id)
                        .build())
                .collect(Collectors.toList());
    }
    private SaleResponse mapToSaleResponse(Sale sale, List<ProductResponse> productResponses) {

        List<String> campaignNames = List.of();
        if (sale.getCampaigns() != null && !sale.getCampaigns().isEmpty())
            campaignNames = sale.getCampaigns().stream().map(Campaign::getName).toList();

        return SaleResponse.builder()
                .id(sale.getId())
                .cashier(sale.getCashier())
                .date(sale.getDate())
                .paymentMethod(sale.getPaymentMethod())
                .campaigns(campaignNames)
                .products(productResponses)
                .total(sale.getTotal()).build();
    }
    private void saveAllProducts(List<Product> products, Sale sale){
        List<Product> updatedProducts = new ArrayList<>();
        products.forEach(product -> {
            product.setSale(sale);
            updatedProducts.add(product);
        });
        productRepository.saveAll(updatedProducts);
    }
}
