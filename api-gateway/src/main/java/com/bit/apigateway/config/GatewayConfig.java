package com.bit.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for configuring the routes of the API Gateway.
 * It uses Spring Cloud Gateway to define the routes and apply filters.
 * The routes are configured to forward requests to the respective microservices based on the path.
 * It also applies authentication and authorization filters using the AuthGatewayFilterFactory.
 * Circuit breakers are configured for each route to handle failures gracefully.
 *
 * @author Emirhan Tuygun
 */
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
    @Value("${circuit-breaker-name}")
    private String CIRCUIT_BREAKER_NAME;
    private final Map<String, List<String>> endpointRoleMapping = new HashMap<>();

    /**
     * Constructor for GatewayConfig class.
     * Initializes the endpointRoleMapping with predefined roles for each endpoint.
     * This mapping is used in the authentication filter to check the user's role and allow or deny access to the endpoint.
     */
    public GatewayConfig() {
        endpointRoleMapping.put("/users", List.of("ADMIN"));
        endpointRoleMapping.put("/sales", List.of("CASHIER", "MANAGER"));
        endpointRoleMapping.put("/campaigns", List.of("CASHIER"));
        endpointRoleMapping.put("/reports", List.of("MANAGER"));
    }

    /**
     * This method configures the routes for the API Gateway.
     * It uses Spring Cloud Gateway to define the routes and apply filters.
     * The routes are configured to forward requests to the respective microservices based on the path.
     * It also applies authentication and authorization filters using the AuthGatewayFilterFactory.
     * Circuit breakers are configured for each route to handle failures gracefully.
     *
     * @param builder The RouteLocatorBuilder instance to build the routes.
     * @param authGatewayFilterFactory The AuthGatewayFilterFactory instance to create authentication and authorization filters.
     * @return The configured RouteLocator instance.
     */
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, AuthGatewayFilterFactory authGatewayFilterFactory) {
        return builder.routes()

                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/auth"))
                        )
                        .uri(AUTH_URI))

                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/user"))
                        )
                        .uri(USER_URI))

                .route("product-service", r -> r.path("/products/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/product"))
                        )
                        .uri(PRODUCT_URI))

                .route("sale-service", r -> r.path("/sales/**", "/campaigns/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/sale"))
                        )
                        .uri(SALE_URI))

                .route("report-service", r -> r.path("/reports/**")
                        .filters(f -> f
                                .filter(authGatewayFilterFactory.apply(new AuthGatewayFilterFactory.Config().setRoleMapping(endpointRoleMapping)))
                                .circuitBreaker(c -> c.setName(CIRCUIT_BREAKER_NAME).setFallbackUri("forward:/fallback/report"))
                        )
                        .uri(REPORT_URI))

                .build();
    }
}