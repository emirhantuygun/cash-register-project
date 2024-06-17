package com.bit.apigateway;

import com.bit.apigateway.config.RouteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.*;

public class RouteValidatorTest {

    private RouteValidator routeValidator;

    @BeforeEach
    public void setUp() {
        routeValidator = new RouteValidator();
    }

    @Test
    public void testIsOpenEndpointWithOpenEndpoint() {
        ServerHttpRequest request = MockServerHttpRequest.get("/auth").build();
        assertFalse(routeValidator.isOpenEndpoint.test(request));
    }

    @Test
    public void testIsOpenEndpointWithNonOpenEndpoint() {
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();
        assertTrue(routeValidator.isOpenEndpoint.test(request));
    }

    @Test
    public void testIsRoleBasedAuthorizationNeededWithNoRoleBasedAuthorizationEndpoint() {
        ServerHttpRequest request = MockServerHttpRequest.get("/products").build();
        assertFalse(routeValidator.isRoleBasedAuthorizationNeeded.test(request));
    }

    @Test
    public void testIsRoleBasedAuthorizationNeededWithNonNoRoleBasedAuthorizationEndpoint() {
        ServerHttpRequest request = MockServerHttpRequest.get("/some-other-endpoint").build();
        assertTrue(routeValidator.isRoleBasedAuthorizationNeeded.test(request));
    }
}
