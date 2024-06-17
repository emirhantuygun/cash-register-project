package com.bit.apigateway;

import com.bit.apigateway.config.AuthGatewayFilterFactory;
import com.bit.apigateway.config.RouteValidator;
import com.bit.apigateway.exception.*;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthGatewayFilterFactoryTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    private AuthGatewayFilterFactory.Config config;

    @BeforeEach
    public void setUp() {
        config = new AuthGatewayFilterFactory.Config();
        Map<String, List<String>> roleMapping = new HashMap<>();
        roleMapping.put("/admin", List.of("ADMIN"));
        config.setRoleMapping(roleMapping);
    }

    @Test
    public void testApplyWithValidTokenAndRolesShouldNotThrowException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(request.getPath().toString()).thenReturn("/admin");
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.getRoles(any(Claims.class))).thenReturn(List.of("ADMIN"));

        assertDoesNotThrow(() -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithMissingAuthorizationHeaderShouldThrowMissingAuthorizationHeaderException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);

        assertThrows(MissingAuthorizationHeaderException.class, () -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithInvalidTokenShouldThrowInvalidTokenException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(request.getHeaders().getFirst("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithLoggedOutTokenShouldThrowLoggedOutTokenException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(request.getHeaders().getFirst("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.isLoggedOut(anyString())).thenReturn(true);

        assertThrows(LoggedOutTokenException.class, () -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithInsufficientRolesShouldThrowInsufficientRolesException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(request.getHeaders().getFirst("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.getRoles(any(Claims.class))).thenReturn(List.of("USER"));
        when(request.getPath().toString()).thenReturn("/admin");

        assertThrows(InsufficientRolesException.class, () -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithMissingRolesShouldThrowMissingRolesException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(exchange.getRequest()).thenReturn(request);
        when(request.getHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(request.getHeaders().getFirst("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.getRoles(any(Claims.class))).thenReturn(null);
        when(request.getPath().toString()).thenReturn("/admin");

        assertThrows(MissingRolesException.class, () -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }

    @Test
    public void testApplyWithOpenEndpointShouldNotThrowException() {
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(false);
        when(exchange.getRequest()).thenReturn(request);

        assertDoesNotThrow(() -> authGatewayFilterFactory.apply(config).filter(exchange, chain));
    }
}
