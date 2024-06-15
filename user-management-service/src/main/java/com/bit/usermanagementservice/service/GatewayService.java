package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.AuthUserRequest;
import com.bit.usermanagementservice.exception.AuthServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
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

@Service
@RequiredArgsConstructor
public class GatewayService {

    @Value("${endpoint.auth-service.create}")
    private String CREATE_ENDPOINT;

    @Value("${endpoint.auth-service.update}")
    private String UPDATE_ENDPOINT;

    @Value("${endpoint.auth-service.restore}")
    private String RESTORE_ENDPOINT;

    @Value("${endpoint.auth-service.delete}")
    private String DELETE_ENDPOINT;

    @Value("${endpoint.auth-service.delete-permanently}")
    private String DELETE_PERMANENTLY_ENDPOINT;

    @Value("${gateway.host}")
    private String GATEWAY_HOST;

    @Value("${gateway.port}")
    private String GATEWAY_PORT;
    private String GATEWAY_URL;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    private void initGatewayUrl() {
        GATEWAY_URL = "http://" + GATEWAY_HOST + ":" + GATEWAY_PORT + "/";
    }

    protected void createUser(AuthUserRequest authUserRequest) {
        try {
            String createUrl = GATEWAY_URL + CREATE_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            String requestBody = objectMapper.writeValueAsString(authUserRequest);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    createUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!(responseEntity.getStatusCode() == HttpStatus.CREATED)) {
                throw new AuthServiceException("User not created in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new AuthServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new AuthServiceException("REST client error: " + e.getMessage());

        } catch (JsonProcessingException e) {
            throw new AuthServiceException("JSON processing error: " + e.getMessage());
        }
    }


    protected void updateUser(Long id, AuthUserRequest authUserRequest) {
        try {
            String updateUrl = GATEWAY_URL + UPDATE_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<AuthUserRequest> requestEntity = new HttpEntity<>(authUserRequest, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new AuthServiceException("User update failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new AuthServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new AuthServiceException("REST client error: " + e.getMessage());
        }
    }

    protected void restoreUser(Long id) {
        try {
            String restoreUrl = GATEWAY_URL + RESTORE_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    restoreUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new AuthServiceException("User restore failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new AuthServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new AuthServiceException("REST client error: " + e.getMessage());
        }
    }

    protected void deleteUser(Long id) {
        try {
            String deleteUrl = GATEWAY_URL + DELETE_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new AuthServiceException("User soft-delete failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new AuthServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new AuthServiceException("REST client error: " + e.getMessage());
        }
    }

    protected void deleteUserPermanently(Long id) {
        try {
            String deleteUrl = GATEWAY_URL + DELETE_PERMANENTLY_ENDPOINT;

            HttpHeaders headers = getHttpHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new AuthServiceException("User permanent-delete failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            throw new AuthServiceException("HTTP error: " + statusCode);

        } catch (RestClientException e) {
            throw new AuthServiceException("REST client error: " + e.getMessage());
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
