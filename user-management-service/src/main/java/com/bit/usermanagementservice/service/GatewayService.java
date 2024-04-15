package com.bit.usermanagementservice.service;

import com.bit.usermanagementservice.dto.AuthUserRequest;
import com.bit.usermanagementservice.dto.UserRequest;
import com.bit.usermanagementservice.exception.AuthException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GatewayService {

    private final String GATEWAY_URL = "http://localhost:8080/";
    private final String CREATE_ENDPOINT = "auth/create";
    private final String UPDATE_ENDPOINT = "auth/update/{id}";
    private final String RESTORE_ENDPOINT = "auth/restore/{id}";
    private final String DELETE_ENDPOINT = "auth/delete/{id}";
    private final String DELETE_PERMANENTLY_ENDPOINT = "auth/delete/permanent/{id}";


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    protected void createUser(AuthUserRequest authUserRequest) {
        try {
            String createUrl = GATEWAY_URL + CREATE_ENDPOINT;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(authUserRequest);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    createUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!(responseEntity.getStatusCode() == HttpStatus.CREATED)) {
                throw new AuthException("User not created in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            throw new AuthException("HTTP error: " + statusCode + "\n Response: " + responseBody);

        } catch (RestClientException e) {
            throw new AuthException("REST client error: " + e.getMessage());

        } catch (JsonProcessingException e) {
            throw new AuthException("JSON processing error: " + e.getMessage());
        }
    }

    protected void updateUser(Long id, AuthUserRequest authUserRequest) {
        try {
            String updateUrl = GATEWAY_URL + UPDATE_ENDPOINT;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AuthUserRequest> requestEntity = new HttpEntity<>(authUserRequest, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new RuntimeException("User update failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            throw new AuthException("HTTP error: " + statusCode + "\n Response: " + responseBody);

        } catch (RestClientException e) {
            throw new AuthException("REST client error: " + e.getMessage());
        }
    }

    protected void restoreUser(Long id) {
        try {
            String restoreUrl = GATEWAY_URL + RESTORE_ENDPOINT;

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    restoreUrl,
                    HttpMethod.PUT,
                    null,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new RuntimeException("User restore failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            throw new AuthException("HTTP error: " + statusCode + "\n Response: " + responseBody);

        } catch (RestClientException e) {
            throw new AuthException("REST client error: " + e.getMessage());
        }
    }

    protected void deleteUser(Long id) {
        try {
            String deleteUrl = GATEWAY_URL + DELETE_ENDPOINT;

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    null,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new RuntimeException("User soft-delete failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            throw new AuthException("HTTP error: " + statusCode + "\n Response: " + responseBody);

        } catch (RestClientException e) {
            throw new AuthException("REST client error: " + e.getMessage());
        }
    }

    protected void deleteUserPermanently(Long id) {
        try {
            String deleteUrl = GATEWAY_URL + DELETE_PERMANENTLY_ENDPOINT;

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    null,
                    String.class,
                    id
            );

            if (!(responseEntity.getStatusCode().is2xxSuccessful())) {
                throw new RuntimeException("User permanent-delete failed in auth-service!");
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            String responseBody = e.getResponseBodyAsString();
            throw new AuthException("HTTP error: " + statusCode + "\n Response: " + responseBody);

        } catch (RestClientException e) {
            throw new AuthException("REST client error: " + e.getMessage());
        }
    }
}
