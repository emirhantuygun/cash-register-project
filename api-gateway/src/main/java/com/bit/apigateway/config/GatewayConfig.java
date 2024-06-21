package com.bit.apigateway.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class GatewayConfig {

    @Value("${route.auth}")
    private String AUTH_URI;
    @Value("${route.user}")
    private String USER_URI;
    @Value("${route.product}")
    private String PRODUCT_URI;
    @Value("${route.sale}")
    private String SALE_URI;
    @Value("${route.report}")
    private String REPORT_URI;
    private final Map<String, List<String>> endpointRoleMapping = new HashMap<>();

    public GatewayConfig() {
        endpointRoleMapping.put("/users", List.of("ADMIN"));
        endpointRoleMapping.put("/sales", List.of("CASHIER", "MANAGER"));
        endpointRoleMapping.put("/campaigns", List.of("CASHIER"));
        endpointRoleMapping.put("/reports", List.of("MANAGER"));
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthGatewayFilterFactory authGatewayFilterFactory) {
        return builder.routes()

                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName("circuit-breaker").setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri(AUTH_URI))

                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName("circuit-breaker").setFallbackUri("forward:/fallback/user"))
                        )
                        .uri(USER_URI))

                .route("product-service", r -> r.path("/products/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName("circuit-breaker").setFallbackUri("forward:/fallback/product"))
                        )
                        .uri(PRODUCT_URI))

                .route("sale-service", r -> r.path("/sales/**", "/campaigns/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName("circuit-breaker").setFallbackUri("forward:/fallback/sale"))
                        )
                        .uri(SALE_URI))

                .route("report-service", r -> r.path("/reports/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName("circuit-breaker").setFallbackUri("forward:/fallback/report"))
                        )
                        .uri(REPORT_URI))

                .build();
    }

}