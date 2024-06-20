package com.bit.saleservice.service;

import com.bit.saleservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductReturnException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;

    @Value("${endpoint.product-service.get-product}")
    private String GET_PRODUCT_ENDPOINT;

    @Value("${endpoint.product-service.check-stock}")
    private String CHECK_STOCK_ENDPOINT;

    @Value("${endpoint.product-service.return-products}")
    private String RETURN_PRODUCTS_ENDPOINT;

    private String GATEWAY_URL;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @PostConstruct
    protected void initGatewayUrl() {
        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
    }


    protected ProductServiceResponse getProduct(Long id) throws HeaderProcessingException {
        try {
            String getUrl = GATEWAY_URL + GET_PRODUCT_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ProductServiceResponse> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    ProductServiceResponse.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new ProductServiceException("Product fetch failed in product-service!");
            }

            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException("Product not found with id: " + id);
            }

            throw new ProductServiceException("HTTP error: " + statusCode.value() + ". Product Service is temporarily unavailable. Please try again later.");
        } catch (RestClientException e) {
            throw new ProductServiceException("REST client error: " + e.getMessage());
        }
    }

//    protected Mono<Boolean> checkEnoughProductsInStock(ProductStockCheckRequest request) throws HeaderProcessingException {
//        HttpHeaders headers = getHttpHeaders();
//        return webClient.post()
//                .uri(GATEWAY_URL + CHECK_STOCK_ENDPOINT)
//                .headers(httpHeaders -> httpHeaders.addAll(headers))
//                .body(BodyInserters.fromValue(request))
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
//                        return Mono.error(new ProductNotFoundException("Product not found with id: " + request.getId()));
//                    }
//                    return Mono.error(new ClientErrorException("Client error occurred while checking product stock"));
//                })
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
//                        Mono.error(new ServerErrorException("Server error occurred while checking product stock"))
//                )
//                .bodyToMono(Boolean.class)
//                .doOnError(e -> {
//                    // Log the error or take appropriate action
//                    System.err.println("Error occurred while checking product stock: " + e.getMessage());
//                    throw new ProductStockCheckException("Error occurred while checking product stock", e);
//                })
//                .onErrorResume(e -> {
//                    // Handle the error and provide a default value
//                    return Mono.just(false);
//                });
//    }


    protected void returnProducts(ProductStockReturnRequest request) throws HeaderProcessingException, ProductReturnException {
        try {
            String returnUrl = GATEWAY_URL + RETURN_PRODUCTS_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<ProductStockReturnRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    returnUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new ProductServiceException("Product return failed in product-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException("Product not found with id: " + request.getId());
            }

            throw new ProductServiceException("HTTP error: " + statusCode.value() + ". Product Service is temporarily unavailable. Please try again later.");
        } catch (RestClientException e) {
            throw new ProductReturnException("REST client error: " + e.getMessage());
        }
    }

    @ExcludeFromGeneratedCoverage
    protected HttpHeaders getHttpHeaders() throws HeaderProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpServletRequest = attributes.getRequest();
                String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                headers.set(HttpHeaders.AUTHORIZATION, token);
            } else {
                throw new HeaderProcessingException("No request attributes found");
            }
        } catch (Exception e) {
            throw new HeaderProcessingException("Failed to process HTTP headers", e);
        }

        return headers;
    }
}
