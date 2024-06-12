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
    private static final Logger logger = LogManager.getLogger(ApiGatewayApplication.class);


    public AuthGatewayFilterFactory(JwtUtils jwtUtils, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        logger.info("inside gateway-filter method");

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isOpenEndpoint.test(request)) {
                logger.info("inside return (exchange, chain)");

                String token = exchange.getRequest().getHeaders().getFirst("Authorization");

                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    Claims claims = jwtUtils.getClaimsAndValidate(token);

                    if (claims == null) {
                        logger.info("Verification or validation failed!");
                        return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    if (jwtUtils.isLoggedOut(token)) {
                        logger.info("Token is logged out!");
                        return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                    }

                    if (routeValidator.isRoleBasedAuthorizationNeeded.test(request)) {
                        List<String> roles = jwtUtils.getRoles(claims);

                        if (roles == null || roles.isEmpty()) {
                            logger.info("Token has no role!");
                            return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                        }

                        String fullPath = request.getPath().toString();
                        String basePath = fullPath.split("/")[1];
                        List<String> requiredRoles = config.getRoleMapping().get("/" + basePath);

                        System.out.println("got required roles: " + requiredRoles);

                        if (roles.stream().noneMatch(requiredRoles::contains)) {
                            logger.info("Roles don't match, forbidden!");
                            return completeResponse(exchange, HttpStatus.FORBIDDEN);
                        }
                    }

                    System.out.println("Authorization Successful!");
                    return chain.filter(exchange);

                } else {
                    logger.info("Token is not provided or its type is not Bearer!");
                    return completeResponse(exchange, HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("OPEN ENDPOINT");
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
