package com.bit.apigateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.bit.apigateway.config.AuthGatewayFilterFactory;
import com.bit.apigateway.config.RouteValidator;
import com.bit.apigateway.exception.*;
import com.bit.apigateway.util.JwtUtils;
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

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

@ExtendWith(MockitoExtension.class)
public class AuthGatewayFilterFactoryTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RouteValidator routeValidator;

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private HttpHeaders headers;

    @Mock
    private URI uri;

    @InjectMocks
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    private AuthGatewayFilterFactory.Config config;

    @BeforeEach
    public void setup() {
        config = new AuthGatewayFilterFactory.Config()
                .setRoleMapping(Map.of(
                        "/products", List.of("USER"),
                        "/admin", List.of("ADMIN")
                ));

        routeValidator = new RouteValidator();
//        authGatewayFilterFactory = spy(authGatewayFilterFactory);
//        routeValidator.isOpenEndpoint = spy(routeValidator.isOpenEndpoint);
//        doReturn(true).when(authGatewayFilterFactory).(any(ServerHttpRequest.class));
//        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);

        uri = URI.create("http://localhost/products");
        String path = uri.getPath();
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getHeaders()).thenReturn(headers);
        when(request.getURI()).thenReturn(uri);
//        when(uri.getPath()).thenReturn(path);
    }

    @Test
    public void testApply_WhenEndpointIsOpenAndTokenIsValid_ShouldAllowRequest() {
        String token = "Bearer validToken";
        Claims claims = mock(Claims.class);
        when(headers.getFirst("Authorization")).thenReturn(token);
        when(jwtUtils.getClaimsAndValidate("validToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("validToken")).thenReturn(false);
        when(routeValidator.isOpenEndpoint.test(request)).thenReturn(true);
        when(routeValidator.isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(false);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        GatewayFilter filter = authGatewayFilterFactory.apply(config);
        Mono<Void> result = filter.filter(exchange, chain);

        verify(chain, times(1)).filter(exchange);
        assertNotNull(result);
    }

    @Test
    public void testApply_WhenEndpointIsOpenAndTokenIsInvalid_ShouldThrowInvalidTokenException() {
        String token = "Bearer invalidToken";
        when(headers.getFirst("Authorization")).thenReturn(token);
        when(jwtUtils.getClaimsAndValidate("invalidToken")).thenThrow(new RuntimeException());
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        assertThrows(InvalidTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void testApply_WhenTokenIsLoggedOut_ShouldThrowLoggedOutTokenException() {
        String token = "Bearer validToken";
        Claims claims = mock(Claims.class);
        when(headers.getFirst("Authorization")).thenReturn(token);
        when(jwtUtils.getClaimsAndValidate("validToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("validToken")).thenReturn(true);
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        assertThrows(LoggedOutTokenException.class, () -> filter.filter(exchange, chain));
    }

    @Test
    public void testApply_WhenNoRolesFoundInToken_ShouldThrowMissingRolesException() {
        String token = "Bearer validToken";
        Claims claims = mock(Claims.class);
        when(headers.getFirst("Authorization")).thenReturn(token);
        when(jwtUtils.getClaimsAndValidate("validToken")).thenReturn(claims);
        when(jwtUtils.isLoggedOut("validToken")).thenReturn(false);
        when(jwtUtils.getRoles(claims)).thenReturn(null);
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
        when(routeValidator.isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        assertThrows(MissingRolesException.class, () -> filter.filter(exchange, chain));
    }

//    @Test
//    public void testApply_WhenInsufficientRolesInToken_ShouldThrowInsufficientRolesException() {
//        String token = "Bearer validToken";
//        Claims claims = mock(Claims.class);
//        when(headers.getFirst("Authorization")).thenReturn(token);
//        when(jwtUtils.getClaimsAndValidate("validToken")).thenReturn(claims);
//        when(jwtUtils.isLoggedOut("validToken")).thenReturn(false);
//        when(jwtUtils.getRoles(claims)).thenReturn(List.of("USER"));
//        when(request.getPath()).thenReturn();
//        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);
//        when(routeValidator.isRoleBasedAuthorizationNeeded.test(any(ServerHttpRequest.class))).thenReturn(true);
//
//        GatewayFilter filter = authGatewayFilterFactory.apply(config);
//
//        assertThrows(InsufficientRolesException.class, () -> filter.filter(exchange, chain));
//    }

    @Test
    public void testApply_WhenAuthorizationHeaderMissing_ShouldThrowMissingAuthorizationHeaderException() {
        when(headers.getFirst("Authorization")).thenReturn(null);
        when(routeValidator.isOpenEndpoint.test(any(ServerHttpRequest.class))).thenReturn(true);

        GatewayFilter filter = authGatewayFilterFactory.apply(config);

        assertThrows(MissingAuthorizationHeaderException.class, () -> filter.filter(exchange, chain));
    }

//    private static class FakePath implements RequestPath {
//        private final String path;
//
//        public FakePath(String path) {
//            this.path = path;
//        }
//
//        @Override
//        public String value() {
//            return path;
//        }
//
//        @Override
//        public String contextPath() {
//            return null;
//        }
//
//        @Override
//        public String pathWithinApplication() {
//            return path;
//        }
//
//        @Override
//        public String toString() {
//            return path;
//        }
//
//        @Override
//        public Iterator<String> elements() {
//            return List.of(path.split("/")).iterator();
//        }
//
//        @Override
//        public List<String> segments() {
//            return List.of(path.split("/"));
//        }
//
//        @Override
//        public boolean startsWith(String prefix) {
//            return path.startsWith(prefix);
//        }
//
//        @Override
//        public boolean startsWith(ServerHttpRequest.Path other) {
//            return path.startsWith(other.toString());
//        }
//
//        @Override
//        public ServerHttpRequest.Path subPath(int index) {
//            return null;
//        }
//    }
}
