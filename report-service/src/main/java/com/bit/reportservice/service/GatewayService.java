package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.SaleServiceException;
import com.bit.reportservice.wrapper.PageWrapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;
    private String GATEWAY_URL;
    private final String GET_SALE_ENDPOINT = "sales/{id}";
    private final String GET_ALL_SALES_ENDPOINT = "sales";
    private final String GET_DELETED_SALES_ENDPOINT = "sales/deleted";
    private final String GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT = "sales/filteredAndSorted";

    private final RestTemplate restTemplate;

    @PostConstruct
    private void initGatewayUrl() {
        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
    }

    protected SaleResponse getSale(Long id) {
        try {
            String getUrl = GATEWAY_URL + GET_SALE_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<SaleResponse> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    SaleResponse.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new SaleServiceException("Sale fetch failed in sale-service!");
            }

            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new SaleServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new SaleServiceException("REST client error: " + e.getMessage());
        }
    }

    protected List<SaleResponse> getAllSales() {
        try {
            String getUrl = GATEWAY_URL + GET_ALL_SALES_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<List<SaleResponse>> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new SaleServiceException("Sale fetch failed in sale-service!");
            }

            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new SaleServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new SaleServiceException("REST client error: " + e.getMessage());
        }
    }

    protected List<SaleResponse> getDeletedSales() {
        try {
            String getUrl = GATEWAY_URL + GET_DELETED_SALES_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<List<SaleResponse>> responseEntity = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<SaleResponse>>() {
                    }
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new SaleServiceException("Deleted sale fetch failed in sale-service!");
            }

            return responseEntity.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new SaleServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new SaleServiceException("REST client error: " + e.getMessage());
        }
    }

    protected Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction, String cashier, String paymentMethod, BigDecimal minPrice, BigDecimal maxPrice, String startDate, String endDate) {
        try {
            String getUrl = GATEWAY_URL + GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getUrl)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .queryParam("sortBy", sortBy)
                    .queryParam("direction", direction)
                    .queryParam("cashier", cashier)
                    .queryParam("paymentMethod", paymentMethod)
                    .queryParam("minPrice", minPrice)
                    .queryParam("maxPrice", maxPrice)
                    .queryParam("startDate", startDate)
                    .queryParam("endDate", endDate);

            ResponseEntity<PageWrapper<SaleResponse>> responseEntity = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new SaleServiceException("Sales fetch failed in sale-service!");
            }

            PageWrapper<SaleResponse> pageWrapper = responseEntity.getBody();
            if (pageWrapper != null) {
                return new PageImpl<>(pageWrapper.getContent(), PageRequest.of(page, size), pageWrapper.getTotalElements());
            } else {
                throw new SaleServiceException("Sales fetch failed in sale-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new SaleServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new SaleServiceException("REST client error: " + e.getMessage());
        }
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
