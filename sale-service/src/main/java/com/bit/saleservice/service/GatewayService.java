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

/**
 * This class is responsible for handling communication with the product-service via the gateway.
 * It provides methods for fetching product details and returning products.
 *
 * @author Emirhan Tuygun
 */
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

    /**
     * This method initializes the GATEWAY_URL by combining the GATEWAY_HOST and GATEWAY_PORT.
     * It logs the trace, info, and trace messages at the respective levels.
     */
    @PostConstruct
    protected void initGatewayUrl() {
        log.trace("Entering initGatewayUrl method in GatewayService");

        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
        log.info("Initialized GATEWAY_URL: {}", GATEWAY_URL);

        log.trace("Exiting initGatewayUrl method in GatewayService");
    }

    /**
     * This method is responsible for fetching a product from the product-service via the gateway.
     * It constructs the URL using the GATEWAY_URL and GET_PRODUCT_ENDPOINT, sends a GET request with the product ID,
     * and processes the response.
     *
     * @param id The ID of the product to fetch.
     * @return The fetched product details.
     * @throws HeaderProcessingException If there is an error processing the HTTP headers.
     * @throws ProductNotFoundException If the product with the given ID is not found.
     * @throws ProductServiceException If there is an error fetching the product from the product-service.
     */
    public ProductResponse getProduct(Long id) throws HeaderProcessingException {
        log.trace("Entering getProduct method in GatewayService with id: {}", id);

        try {
            String getUrl = GATEWAY_URL + GET_PRODUCT_ENDPOINT;
            log.debug("Constructed getUrl: {}", getUrl);

            // Getting HTTP headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Making the request
            ResponseEntity<ProductResponse> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    ProductResponse.class,
                    id
            );

            // Checking the status code
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

    /**
     * This method is responsible for returning products to the product-service via the gateway.
     * It constructs the URL using the GATEWAY_URL and RETURN_PRODUCTS_ENDPOINT, sends a POST request with the product stock return request,
     * and processes the response.
     *
     * @param request The product stock return request containing the details of the products to be returned.
     * @throws HeaderProcessingException If there is an error processing the HTTP headers.
     * @throws ProductReturnException If there is an error returning the products to the product-service.
     */
    protected void returnProducts(ProductStockReturnRequest request) throws HeaderProcessingException, ProductReturnException {
        log.trace("Entering returnProducts method in GatewayService with request: {}", request);

        try {
            String returnUrl = GATEWAY_URL + RETURN_PRODUCTS_ENDPOINT;
            log.debug("Constructed returnUrl: {}", returnUrl);

            // Getting HTTP headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<ProductStockReturnRequest> requestEntity = new HttpEntity<>(request, headers);

            // Making the request
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    returnUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Checking the status code
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

    /**
     * This method retrieves HTTP headers for the REST API calls.
     * It retrieves the authorization token from the current HTTP request and sets it in the headers.
     *
     * @return The HTTP headers with the authorization token set.
     * @throws HeaderProcessingException If there is an error processing the HTTP headers.
     */
    @ExcludeFromGeneratedCoverage
    protected HttpHeaders getHttpHeaders() throws HeaderProcessingException {
        log.trace("Entering getHttpHeaders method in GatewayService");

        // Creating new headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Retrieving authorization token from request attributes and set it in the headers
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
