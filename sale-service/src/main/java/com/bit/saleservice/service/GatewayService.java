package com.bit.saleservice.service;

import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.ClientErrorException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.exception.ServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;

    @Value("${endpoint.product-service.get-product}")
    private String GET_PRODUCT_ENDPOINT;

    @Value("${endpoint.product-service.is-product-in-stock}")
    private String IS_PRODUCT_IN_STOCK_ENDPOINT;

    private String GATEWAY_URL;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @PostConstruct
    private void initGatewayUrl() {
        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
    }

    protected ProductServiceResponse getProduct(Long id){
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

            throw new ProductServiceException("HTTP error: " + statusCode.value());
        } catch (RestClientException e) {
            throw new ProductServiceException("REST client error: " + e.getMessage());
        }
    }

    protected Mono<Boolean> checkProductInStock(Long id) {
        return webClient.get()
                .uri(GATEWAY_URL + IS_PRODUCT_IN_STOCK_ENDPOINT, id)
                .headers(httpHeaders -> httpHeaders.addAll(getHttpHeaders()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new ProductNotFoundException("Product not found with id: " + id));
                    }
                    return Mono.error(new ClientErrorException("Client error occurred while checking product stock"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new ServerErrorException("Server error occurred while checking product stock"))
                )
                .bodyToMono(Boolean.class)
                .doOnError(e -> {
                    // Log the error or take appropriate action
                    System.err.println("Error occurred while checking product stock: " + e.getMessage());
                })
                .onErrorResume(e -> {
                    // Handle the error and provide a default value
                    return Mono.just(false);
                });
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest httpServletRequest = attributes.getRequest();
            String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            headers.set(HttpHeaders.AUTHORIZATION, token);
        }
        return headers;
    }
}
