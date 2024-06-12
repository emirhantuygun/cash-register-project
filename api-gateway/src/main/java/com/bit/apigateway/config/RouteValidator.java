package com.bit.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openEndpoints = List.of(
            "/auth/login",
            "/auth/refresh-token",
            "/auth/logout"
    );

    public static final List<String> noRoleBasedAuthorizationEndpoints = List.of(
            "/products"
    );


    public Predicate<ServerHttpRequest> isSecured =
            request -> openEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    public Predicate<ServerHttpRequest> isRoleBasedAuthorizationNeeded =
            request -> noRoleBasedAuthorizationEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}