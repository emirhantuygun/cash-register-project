package com.bit.apigateway.config;

import com.bit.apigateway.exception.*;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private ServerHttpResponse response;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private Predicate<ServerHttpRequest> isSecured;

    @Mock
    private Predicate<ServerHttpRequest> isRoleBasedAuthorizationNeeded;

    @InjectMocks
    private AuthGatewayFilterFactory filterFactory;

    private AuthGatewayFilterFactory.Config config;

    @BeforeEach
    public void setUp() {
        config = new AuthGatewayFilterFactory.Config();
        config.setRoleMapping(Collections.singletonMap("/secure", Arrays.asList("ROLE_USER", "ROLE_ADMIN")));
        when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);

        // Mocking the predicates
        routeValidator.isSecured = isSecured;
        routeValidator.isRoleBasedAuthorizationNeeded = isRoleBasedAuthorizationNeeded;
    }

    @Test
    public void givenOpenEndpoint_whenApply_thenChainFilterIsCalled() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(false);
        GatewayFilter filter = filterFactory.apply(config);
        filter.filter(exchange, chain);
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    public void givenMissingAuthorizationHeader_whenApply_thenThrowsMissingAuthorizationHeaderException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenInvalidAuthorizationHeader_whenApply_thenThrowsMissingAuthorizationHeaderException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "InvalidToken");
        when(request.getHeaders()).thenReturn(headers);
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenInvalidToken_whenApply_thenThrowsInvalidTokenException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer InvalidToken");
        when(request.getHeaders()).thenReturn(headers);
        when(jwtUtils.getClaimsAndValidate("InvalidToken")).thenReturn(null);
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(InvalidTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenLoggedOutToken_whenApply_thenThrowsLoggedOutTokenException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer LoggedOutToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtils.getClaimsAndValidate("LoggedOutToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("LoggedOutToken")).thenReturn(true);
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(LoggedOutTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenMissingRoles_whenApply_thenThrowsMissingRolesException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtils.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("ValidToken")).thenReturn(false);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getRoles(claims)).thenReturn(Collections.emptyList());
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(MissingRolesException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenInsufficientRoles_whenApply_thenThrowsInsufficientRolesException() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtils.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("ValidToken")).thenReturn(false);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getRoles(claims)).thenReturn(Collections.singletonList("ROLE_CASHIER"));
        when(request.getPath()).thenReturn(mock(RequestPath.class));
        when(request.getPath().toString()).thenReturn("/secure");
        GatewayFilter filter = filterFactory.apply(config);
        assertThrows(InsufficientRolesException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void givenValidTokenAndRoles_whenApply_thenChainFilterIsCalled() {
        when(isSecured.test(any(ServerHttpRequest.class))).thenReturn(true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ValidToken");
        when(request.getHeaders()).thenReturn(headers);
        Claims claims = mock(Claims.class);
        when(jwtUtils.getClaimsAndValidate("ValidToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("ValidToken")).thenReturn(false);
        when(isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(false);
        GatewayFilter filter = filterFactory.apply(config);
        filter.filter(exchange, chain);
        verify(chain, times(1)).filter(exchange);
    }
}