package com.bit.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.*;

class RouteValidatorTest {

    private RouteValidator routeValidator;

    @BeforeEach
    void setUp() {
        routeValidator = new RouteValidator();
    }

    @Test
    void testIsOpenEndpointWithOpenEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/auth").build();

        // Assert
        assertFalse(routeValidator.isSecured.test(request));
    }

    @Test
    void testIsOpenEndpointWithNonOpenEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();

        // Assert
        assertTrue(routeValidator.isSecured.test(request));
    }

    @Test
    void testIsRoleBasedAuthorizationNeededWithNoRoleBasedAuthorizationEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/products").build();

        // Assert
        assertFalse(routeValidator.isRoleBasedAuthorizationNeeded.test(request));
    }

    @Test
    void testIsRoleBasedAuthorizationNeededWithNonNoRoleBasedAuthorizationEndpoint() {
        // Act
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();

        // Assert
        assertTrue(routeValidator.isRoleBasedAuthorizationNeeded.test(request));
    }
}
