package com.bit.apigateway;

import com.bit.apigateway.config.AuthGatewayFilterFactory;
import com.bit.apigateway.config.RouteValidator;
import com.bit.apigateway.exception.InsufficientRolesException;
import com.bit.apigateway.exception.InvalidTokenException;
import com.bit.apigateway.exception.LoggedOutTokenException;
import com.bit.apigateway.exception.MissingAuthorizationHeaderException;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthGatewayFilterFactoryTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RouteValidator routeValidator;

    @InjectMocks
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(routeValidator.isOpenEndpoint).thenReturn(mock(Predicate.class));
        when(routeValidator.isRoleBasedAuthorizationNeeded).thenReturn(mock(Predicate.class));
    }

    @Test
    public void testFilterOpenEndpointWithValidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/open-endpoint")
                .header("Authorization", "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.isLoggedOut(anyString())).thenReturn(false);

        GatewayFilter filter = authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config());
        filter.filter(exchange, exchange1 -> Mono.empty()).subscribe();

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getClaimsAndValidate(anyString());
        verify(jwtUtils).isLoggedOut(anyString());
    }

    @Test
    public void testFilterOpenEndpointWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/open-endpoint")
                .header("Authorization", "Bearer invalid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(null);

        GatewayFilter filter = authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config());

        assertThrows(InvalidTokenException.class, () -> filter.filter(exchange, exchange1 -> Mono.empty()).block());

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getClaimsAndValidate(anyString());
    }

    @Test
    public void testFilterOpenEndpointWithLoggedOutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/open-endpoint")
                .header("Authorization", "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(mock(Claims.class));
        when(jwtUtils.isLoggedOut(anyString())).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config());

        assertThrows(LoggedOutTokenException.class, () -> filter.filter(exchange, exchange1 -> Mono.empty()).block());

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getClaimsAndValidate(anyString());
        verify(jwtUtils).isLoggedOut(anyString());
    }

    @Test
    public void testFilterMissingAuthorizationHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/open-endpoint").build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config());

        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, exchange1 -> Mono.empty()).block());

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
    }

    @Test
    public void testFilterRoleBasedAuthorizationNeeded() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/role-endpoint")
                .header("Authorization", "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        Claims claims = mock(Claims.class);
        List<String> roles = Collections.singletonList("USER");

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(claims);
        when(jwtUtils.isLoggedOut(anyString())).thenReturn(false);
        when(routeValidator.isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getRoles(any(Claims.class))).thenReturn(roles);

        AuthGatewayFilterFactory.Config config = new AuthGatewayFilterFactory.Config();
        config.setRoleMapping(Collections.singletonMap("/role-endpoint", Collections.singletonList("USER")));

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        filter.filter(exchange, exchange1 -> Mono.empty()).subscribe();

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getClaimsAndValidate(anyString());
        verify(jwtUtils).isLoggedOut(anyString());
        verify(routeValidator.isRoleBasedAuthorizationNeeded).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getRoles(any(Claims.class));
    }

    @Test
    public void testFilterInsufficientRoles() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/role-endpoint")
                .header("Authorization", "Bearer valid-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        Claims claims = mock(Claims.class);
        List<String> roles = Collections.singletonList("USER");

        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getClaimsAndValidate(anyString())).thenReturn(claims);
        when(jwtUtils.isLoggedOut(anyString())).thenReturn(false);
        when(routeValidator.isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(jwtUtils.getRoles(any(Claims.class))).thenReturn(roles);

        AuthGatewayFilterFactory.Config config = new AuthGatewayFilterFactory.Config();
        config.setRoleMapping(Collections.singletonMap("/role-endpoint", Collections.singletonList("ADMIN")));

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        assertThrows(InsufficientRolesException.class, () -> filter.filter(exchange, exchange1 -> Mono.empty()).block());

        verify(routeValidator.isOpenEndpoint).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getClaimsAndValidate(anyString());
        verify(jwtUtils).isLoggedOut(anyString());
        verify(routeValidator.isRoleBasedAuthorizationNeeded).test(any(ServerHttpRequest.class));
        verify(jwtUtils).getRoles(any(Claims.class));
    }
}
