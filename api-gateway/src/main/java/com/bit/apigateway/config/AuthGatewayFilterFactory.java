package com.bit.apigateway.config;

import com.bit.apigateway.ApiGatewayApplication;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private final JwtUtils jwtUtils;
    private final RouteValidator routeValidator;

    public AuthGatewayFilterFactory(JwtUtils jwtUtils, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isOpenEndpoint.test(request)) {

                String token = exchange.getRequest().getHeaders().getFirst("Authorization");

                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    Claims claims = jwtUtils.getClaimsAndValidate(token);

                    if (claims == null) {
                        return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    if (jwtUtils.isLoggedOut(token)) {
                        return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    if (routeValidator.isRoleBasedAuthorizationNeeded.test(request)) {
                        List<String> roles = jwtUtils.getRoles(claims);

                        if (roles == null || roles.isEmpty()) {
                            return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                        }

                        String fullPath = request.getPath().toString();
                        String basePath = fullPath.split("/")[1];
                        List<String> requiredRoles = config.getRoleMapping().get("/" + basePath);

                        if (roles.stream().noneMatch(requiredRoles::contains)) {
                            return completeResponse(exchange, HttpStatus.FORBIDDEN);
                        }
                    }

                    return chain.filter(exchange);

                } else {
                    return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                }
            } else {
                return chain.filter(exchange);
            }
        };
    }

    private Mono<Void> completeResponse(ServerWebExchange exchange, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    @Getter
    public static class Config {
        private Map<String, List<String>> roleMapping;

        public Config setRoleMapping(Map<String, List<String>> roleMapping) {
            this.roleMapping = roleMapping;
            return this;
        }
    }
}
