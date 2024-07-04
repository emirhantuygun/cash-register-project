package com.bit.reportservice.service;

import com.bit.reportservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.SaleServiceException;
import com.bit.reportservice.wrapper.PageWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import java.math.BigDecimal;
import java.util.List;

/**
 * This class is responsible for making HTTP requests to the sale-service using the Spring RestTemplate.
 * It handles various operations related to sales, such as fetching a single sale, all sales, deleted sales,
 * and sales filtered and sorted.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${endpoint.sale-service.get-sale}")
    private String GET_SALE_ENDPOINT;

    @Value("${endpoint.sale-service.get-all-sales}")
    private String GET_ALL_SALES_ENDPOINT;

    @Value("${endpoint.sale-service.get-deleted-sales}")
    private String GET_DELETED_SALES_ENDPOINT;

    @Value("${endpoint.sale-service.get-all-sales-filtered-and-sorted}")
    private String GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT;

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;
    private String GATEWAY_URL;
    private final RestTemplate restTemplate;

    /**
     * This method initializes the GATEWAY_URL by combining the GATEWAY_HOST and GATEWAY_PORT.
     * It is called automatically after the GatewayService object is created.
     */
    @PostConstruct
    protected void initGatewayUrl() {
        log.trace("Entering initGatewayUrl method in GatewayService");
        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
        log.trace("Exiting initGatewayUrl method in GatewayService");
    }

    /**
     * This method is responsible for making a GET request to the sale-service to fetch a single sale by its ID.
     *
     * @param id The ID of the sale to be fetched.
     * @return The SaleResponse object representing the fetched sale.
     * @throws HeaderProcessingException If there is an issue processing the HTTP headers.
     * @throws SaleServiceException If there is an error fetching the sale from the sale-service.
     */
    protected SaleResponse getSale(Long id) throws HeaderProcessingException {
        log.trace("Entering getSale method in GatewayService");
        try {
            String getUrl = GATEWAY_URL + GET_SALE_ENDPOINT;
            log.debug("Sending GET request to: {}", getUrl);

            // Getting Http headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Making the request
            ResponseEntity<SaleResponse> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    SaleResponse.class,
                    id
            );

            // Checking the status code
            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.error("Sale fetch failed in sale-service!");
                throw new SaleServiceException("Sale fetch failed in sale-service!");
            }
            log.debug("Received successful response for getSale: {}", responseEntity.getBody());

            log.trace("Exiting getSale method in GatewayService");
            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error: {} for sale service", e.getStatusCode(), e);
            throw new SaleServiceException("HTTP error: " + e.getStatusCode() + " for sale service");

        } catch (RestClientException e) {
            log.error("REST client error: {} for sale service", e.getMessage(), e);
            throw new SaleServiceException("REST client error: " + e.getMessage() + " for sale service");
        }
    }

    /**
     * This method is responsible for making a GET request to the sale-service to fetch all sales.
     *
     * @return A list of SaleResponse objects representing all fetched sales.
     * @throws HeaderProcessingException If there is an issue processing the HTTP headers.
     * @throws SaleServiceException If there is an error fetching the sales from the sale-service.
     */
    protected List<SaleResponse> getAllSales() throws HeaderProcessingException {
        log.trace("Entering getAllSales method in GatewayService");

        try {
            String getUrl = GATEWAY_URL + GET_ALL_SALES_ENDPOINT;
            log.debug("Sending GET request to: {}", getUrl);

            // Getting Http headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Making the request
            ResponseEntity<List<SaleResponse>> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            // Checking the status code
            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.error("Sales fetch failed in sale-service!");
                throw new SaleServiceException("Sale fetch failed in sale-service!");
            }
            log.debug("Received successful response for getAllSales: {}", responseEntity.getBody());

            log.trace("Exiting getAllSales method in GatewayService");
            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error: {} for sale service", e.getStatusCode(), e);
            throw new SaleServiceException("HTTP error: " + e.getStatusCode() + " for sale service");

        } catch (RestClientException e) {
            log.error("REST client error: {} for sale service", e.getMessage(), e);
            throw new SaleServiceException("REST client error: " + e.getMessage() + " for sale service");
        }
    }

    /**
     * This method is responsible for making a GET request to the sale-service to fetch all deleted sales.
     *
     * @return A list of SaleResponse objects representing all fetched deleted sales.
     * @throws HeaderProcessingException If there is an issue processing the HTTP headers.
     * @throws SaleServiceException If there is an error fetching the deleted sales from the sale-service.
     */
    protected List<SaleResponse> getDeletedSales() throws HeaderProcessingException {
        log.trace("Entering getDeletedSales method in GatewayService");
        try {
            String getUrl = GATEWAY_URL + GET_DELETED_SALES_ENDPOINT;
            log.debug("Sending GET request to: {}", getUrl);

            // Getting Http headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Making the request
            ResponseEntity<List<SaleResponse>> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            // Checking the status code
            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.error("Deleted sales fetch failed in sale-service!");
                throw new SaleServiceException("Deleted sale fetch failed in sale-service!");
            }
            log.debug("Received successful response for getDeletedSales: {}", responseEntity.getBody());

            log.trace("Exiting getDeletedSales method in GatewayService");
            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error: {} for sale service", e.getStatusCode(), e);
            throw new SaleServiceException("HTTP error: " + e.getStatusCode() + " for sale service");

        } catch (RestClientException e) {
            log.error("REST client error: {} for sale service", e.getMessage(), e);
            throw new SaleServiceException("REST client error: " + e.getMessage() + " for sale service");
        }
    }

    /**
     * This method is responsible for making a GET request to the sale-service to fetch all sales,
     * filtered and sorted based on the provided parameters.
     *
     * @param page The page number for pagination.
     * @param size The number of sales per page.
     * @param sortBy The field to sort the sales by.
     * @param direction The sorting direction (asc or desc).
     * @param cashier The cashier's name to filter sales by.
     * @param paymentMethod The payment method to filter sales by.
     * @param minTotal The minimum total amount to filter sales by.
     * @param maxTotal The maximum total amount to filter sales by.
     * @param startDate The start date to filter sales by.
     * @param endDate The end date to filter sales by.
     * @return A Page object containing SaleResponse objects representing the fetched sales.
     * @throws HeaderProcessingException If there is an issue processing the HTTP headers.
     * @throws SaleServiceException If there is an error fetching the sales from the sale-service.
     */
    protected Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction, String cashier,
                                                              String paymentMethod, BigDecimal minTotal, BigDecimal maxTotal,
                                                              String startDate, String endDate, Boolean isCancelled) throws HeaderProcessingException {
        log.trace("Entering getAllSalesFilteredAndSorted method in GatewayService");

        try {
            String getUrl = GATEWAY_URL + GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT;
            log.debug("Sending GET request to: {}", getUrl);

            // Getting Http headers
            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Building the query parameters using UriComponentsBuilder
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getUrl)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("sortBy", sortBy)
                    .queryParam("direction", direction)
                    .queryParam("cashier", cashier)
                    .queryParam("paymentMethod", paymentMethod)
                    .queryParam("minTotal", minTotal)
                    .queryParam("maxTotal", maxTotal)
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate)
                    .queryParam("isCancelled", isCancelled);

            // Making the request
            ResponseEntity<PageWrapper<SaleResponse>> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );

            // Checking the status code
            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                log.error("Sales fetch failed in sale-service!");
                throw new SaleServiceException("Sales fetch failed in sale-service!");
            }

            PageWrapper<SaleResponse> pageWrapper = responseEntity.getBody();
            if (pageWrapper != null) {
                log.debug("Received successful response for getAllSalesFilteredAndSorted: {}", pageWrapper.getContent());

                log.trace("Exiting getAllSalesFilteredAndSorted method in GatewayService");
                return new PageImpl<>(pageWrapper.getContent(), PageRequest.of(page, size), pageWrapper.getTotalElements());
            } else {
                log.error("Sales fetch failed in sale-service!");
                throw new SaleServiceException("Sales fetch failed in sale-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error: {} for sale service", e.getStatusCode(), e);
            throw new SaleServiceException("HTTP error: " + e.getStatusCode() + " for sale service");

        } catch (RestClientException e) {
            log.error("REST client error: {} for sale service", e.getMessage(), e);
            throw new SaleServiceException("REST client error: " + e.getMessage() + " for sale service");
        }
    }

    /**
     * This method retrieves HTTP headers for making requests to the sale-service.
     * It retrieves the authorization token from the current HTTP request and sets it in the headers.
     *
     * @return HttpHeaders object containing the authorization token.
     * @throws HeaderProcessingException If there is an issue processing the HTTP headers.
     */
    @ExcludeFromGeneratedCoverage
    protected HttpHeaders getHttpHeaders() throws HeaderProcessingException {
        log.trace("Entering getAllSalesFilteredAndSorted method in GatewayService");

        // Creating an empty HttpHeaders object to store the headers.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Extracting the authorization token from the current HTTP request and setting it in the headers.
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpServletRequest = attributes.getRequest();
                String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                headers.set(HttpHeaders.AUTHORIZATION, token);

            } else {
                log.error("No request attributes found");
                throw new HeaderProcessingException("No request attributes found");
            }
        } catch (Exception e) {
            log.error("Failed to process HTTP headers", e);
            throw new HeaderProcessingException("Failed to process HTTP headers", e);
        }

        log.trace("Exiting getAllSalesFilteredAndSorted method in GatewayService");
        return headers;
    }
}
