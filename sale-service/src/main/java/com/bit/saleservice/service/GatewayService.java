package com.bit.saleservice.service;

import com.bit.saleservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.saleservice.dto.ProductResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductReturnException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;

    @Value("${endpoint.product-service.get-product}")
    private String GET_PRODUCT_ENDPOINT;

    @Value("${endpoint.product-service.return-products}")
    private String RETURN_PRODUCTS_ENDPOINT;

    private String GATEWAY_URL;
    private final RestTemplate restTemplate;

    @PostConstruct
    protected void initGatewayUrl() {
        log.trace("Entering initGatewayUrl method in GatewayService");

        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
        log.info("Initialized GATEWAY_URL: {}", GATEWAY_URL);

        log.trace("Exiting initGatewayUrl method in GatewayService");
    }

    public ProductResponse getProduct(Long id) throws HeaderProcessingException {
        log.trace("Entering getProduct method in GatewayService with id: {}", id);

        try {
            String getUrl = GATEWAY_URL + GET_PRODUCT_ENDPOINT;
            log.debug("Constructed getUrl: {}", getUrl);

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<ProductResponse> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    ProductResponse.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.warn("Product fetch failed with status code: {}", responseEntity.getStatusCode());
                throw new ProductServiceException("Product fetch failed in product-service!");
            }
            log.info("Successfully fetched product with id: {}", id);

            log.trace("Exiting getProduct method in GatewayService with id: {}", id);
            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();

            if (statusCode == HttpStatus.NOT_FOUND) {
                log.error("404 Not Found error occurred while fetching product with id: {}", id, e);
                throw new ProductNotFoundException("Product not found with id: " + id);
            }

            log.error("HTTP error occurred while fetching product with id: {}, status code: {}", id, statusCode, e);
            throw new ProductServiceException("HTTP error: " + statusCode.value() + ". Product Service is temporarily unavailable. Please try again later.");

        } catch (RestClientException e) {
            log.error("REST client error occurred while fetching product with id: {}", id, e);
            throw new ProductServiceException("REST client error: " + e.getMessage());
        }
    }

    protected void returnProducts(ProductStockReturnRequest request) throws HeaderProcessingException, ProductReturnException {
        log.trace("Entering returnProducts method in GatewayService with request: {}", request);

        try {
            String returnUrl = GATEWAY_URL + RETURN_PRODUCTS_ENDPOINT;
            log.debug("Constructed returnUrl: {}", returnUrl);

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<ProductStockReturnRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    returnUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.warn("Product return failed with status code: {}", responseEntity.getStatusCode());
                throw new ProductServiceException("Product return failed in product-service!");
            }

            log.info("Successfully returned products for request: {}", request);
            log.trace("Exiting returnProducts method in GatewayService with request: {}", request);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode == HttpStatus.NOT_FOUND) {
                log.error("404 Not Found error occurred while returning products with id: {}", request.getId(), e);
                throw new ProductNotFoundException("Product not found with id: " + request.getId());
            }

            log.error("HTTP error occurred while returning products for request: {}, status code: {}", request, statusCode, e);
            throw new ProductServiceException("HTTP error: " + statusCode.value() + ". Product Service is temporarily unavailable. Please try again later.");

        } catch (RestClientException e) {
            log.error("REST client error occurred while returning products for request: {}", request, e);
            throw new ProductReturnException("REST client error: " + e.getMessage());
        }
    }

    @ExcludeFromGeneratedCoverage
    protected HttpHeaders getHttpHeaders() throws HeaderProcessingException {
        log.trace("Entering getHttpHeaders method in GatewayService");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpServletRequest = attributes.getRequest();
                String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                headers.set(HttpHeaders.AUTHORIZATION, token);
                log.debug("Retrieved authorization token from request attributes");

            } else {
                log.warn("No request attributes found while processing HTTP headers");
                throw new HeaderProcessingException("No request attributes found");
            }
        } catch (Exception e) {
            log.error("Error occurred while processing HTTP headers", e);
            throw new HeaderProcessingException("Failed to process HTTP headers", e);
        }

        log.trace("Exiting getHttpHeaders method in GatewayService");
        return headers;
    }
}
