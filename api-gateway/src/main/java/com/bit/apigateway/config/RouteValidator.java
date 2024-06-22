package com.bit.apigateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Predicate;

/**
 * This class is responsible for validating routes and checking if they require security or role-based authorization.
 *
 * @author Emirhan Tuygun
 */
@Component
public class RouteValidator {

    public List<String> openEndpoints = List.of(
            "/auth"
    );

    public List<String> noRoleBasedAuthorizationEndpoints = List.of(
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