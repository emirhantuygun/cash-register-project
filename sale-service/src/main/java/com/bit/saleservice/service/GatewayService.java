package com.bit.saleservice.service;

import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.ProductServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final String GATEWAY_URL = "http://localhost:8080/";
    private final String GET_ENDPOINT = "products/{id}";

    private final RestTemplate restTemplate;

    protected ProductServiceResponse getProduct(Long id){
        try {
            String getUrl = GATEWAY_URL + GET_ENDPOINT;

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
            throw new ProductServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new ProductServiceException("REST client error: " + e.getMessage());
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
