package com.bit.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class GatewayConfig {

    private final Map<String, List<String>> endpointRoleMapping = new HashMap<>();

    public GatewayConfig() {
        endpointRoleMapping.put("/users", List.of("ADMIN"));
        endpointRoleMapping.put("/sales", List.of("CASHIER"));
        endpointRoleMapping.put("/campaigns", List.of("CASHIER"));
        endpointRoleMapping.put("/reports", List.of("ADMIN"));
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthGatewayFilterFactory authGatewayFilterFactory) {
        return builder.routes()

                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping))))
                        .uri("lb://auth-service"))

                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping))))
                        .uri("lb://user-service"))

                .route("product-service", r -> r.path("/products/**")
                        .uri("lb://product-service"))

                .route("sale-service", r -> r.path("/sales/**", "/campaigns/**")
                        .filters(f -> f.filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping))))
                        .uri("lb://sale-service"))

                .build();
    }
}