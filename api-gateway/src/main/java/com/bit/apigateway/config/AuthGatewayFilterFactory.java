package com.bit.apigateway.config;

import com.bit.apigateway.exception.*;
import com.bit.apigateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 * This class is a custom filter factory for Spring Cloud Gateway.
 * It is responsible for adding authentication and authorization logic to the gateway.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {

    private final JwtUtils jwtUtils;
    private final RouteValidator routeValidator;

    /**
     * Constructor for AuthGatewayFilterFactory class.
     * Initializes the JwtUtils and RouteValidator instances.
     *
     * @param jwtUtils An instance of JwtUtils for token validation and extraction.
     * @param routeValidator An instance of RouteValidator for route validation.
     */
    public AuthGatewayFilterFactory(JwtUtils jwtUtils, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
        this.routeValidator = routeValidator;
    }

    /**
     * This method applies the custom authentication and authorization logic to the gateway.
     * It checks if the request requires security, extracts the token from the Authorization header,
     * validates the token, and performs role-based authorization if necessary.
     *
     * @param config The configuration object containing the role mapping for each base path.
     * @return A GatewayFilter that applies the authentication and authorization logic.
     * @throws InvalidTokenException if the token has no claims or not valid.
     * @throws LoggedOutTokenException if the token is logged out.
     * @throws MissingRolesException if the token has no roles
     * @throws InsufficientRolesException if the token has insufficient roles for the endpoint.
     * @throws MissingAuthorizationHeaderException if the request has no or invalid authorization header.
     */
    @Override
    public GatewayFilter apply(Config config) {
        log.trace("Entering apply method in AuthGatewayFilterFactory class");

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Request URI: {}", request.getURI());

            if (routeValidator.isSecured.test(request)) {
                String token = exchange.getRequest().getHeaders().getFirst("Authorization");
                log.debug("Authorization header: {}", token);

                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    log.debug("Token after removing Bearer prefix: {}", token);

                    Claims claims = jwtUtils.getClaimsAndValidate(token);
                    log.debug("Claims extracted: {}", claims);

                    if (claims == null) {
                        log.error("Invalid token");
                        throw new InvalidTokenException("Invalid token");
                    }

                    if (jwtUtils.isLoggedOut(token)) {
                        log.error("Token is logged out");
                        throw new LoggedOutTokenException("Token is logged out");
                    }

                    if (routeValidator.isRoleBasedAuthorizationNeeded.test(request)) {
                        List<String> roles = jwtUtils.getRoles(claims);
                        log.debug("Roles extracted: {}", roles);

                        if (roles == null || roles.isEmpty()) {
                            log.error("No roles found in token");
                            throw new MissingRolesException("No roles found in token");
                        }

                        String fullPath = request.getPath().toString();
                        String basePath = fullPath.split("/")[1];
                        List<String> requiredRoles = config.getRoleMapping().get("/" + basePath);
                        log.debug("Required roles for basePath {}: {}", basePath, requiredRoles);

                        if (roles.stream().noneMatch(requiredRoles::contains)) {
                            log.error("Insufficient roles");
                            throw new InsufficientRolesException("Insufficient roles");
                        }
                    }

                    log.trace("Exiting apply method in AuthGatewayFilterFactory class with successful authentication");
                    return chain.filter(exchange);

                } else {
                    log.error("Missing or Invalid Authorization header");
                    throw new MissingAuthorizationHeaderException("Missing or Invalid Authorization header");
                }
            } else {
                log.info("Request does not require security");
                return chain.filter(exchange);
            }
        };
    }

    /**
     * This inner static class represents the configuration for the AuthGatewayFilterFactory.
     * It contains a map of base paths to their required roles.
     *
     * @author Emirhan Tuygun
     */
    @Getter
    public static class Config {
        private Map<String, List<String>> roleMapping;

        /**
         * Sets the role mapping for the AuthGatewayFilterFactory.
         *
         * @param roleMapping A map of base paths to their required roles.
         * @return The current Config instance for method chaining.
         */
        public Config setRoleMapping(Map<String, List<String>> roleMapping) {
            this.roleMapping = roleMapping;
            return this;
        }
    }
}
