package com.bit.apigateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GatewayConfigTest {

    @InjectMocks
    private GatewayConfig gatewayConfig;

    @Mock
    private RouteLocatorBuilder routeLocatorBuilder;

    @Mock
    private RouteLocatorBuilder.Builder routesBuilder;

    @Mock
    private AuthGatewayFilterFactory authGatewayFilterFactory;

    @Mock
    private RouteLocator routeLocator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gatewayConfig, "AUTH_URI", "http://auth-service");
        ReflectionTestUtils.setField(gatewayConfig, "USER_URI", "http://user-service");
        ReflectionTestUtils.setField(gatewayConfig, "PRODUCT_URI", "http://product-service");
        ReflectionTestUtils.setField(gatewayConfig, "SALE_URI", "http://sale-service");
        ReflectionTestUtils.setField(gatewayConfig, "REPORT_URI", "http://report-service");

        when(routeLocatorBuilder.routes()).thenReturn(routesBuilder);
        when(routesBuilder.route(anyString(), any())).thenReturn(routesBuilder);
        when(routesBuilder.build()).thenReturn(routeLocator);
    }

    @Test
    void givenAuthServiceRoute_whenRoutesMethodIsCalled_thenRouteIsConfiguredCorrectly() {
        // Act
        RouteLocator routeLocator = gatewayConfig.routes(routeLocatorBuilder, authGatewayFilterFactory);

        // Assert
        assertNotNull(routeLocator);
        verify(routesBuilder, times(1)).route(eq("auth-service"), any());
    }

    @Test
    void givenUserServiceRoute_whenRoutesMethodIsCalled_thenRouteIsConfiguredCorrectly() {
        // Act
        RouteLocator routeLocator = gatewayConfig.routes(routeLocatorBuilder, authGatewayFilterFactory);

        // Assert
        assertNotNull(routeLocator);
        verify(routesBuilder, times(1)).route(eq("user-service"), any());
    }

    @Test
    void givenProductServiceRoute_whenRoutesMethodIsCalled_thenRouteIsConfiguredCorrectly() {
        // Act
        RouteLocator routeLocator = gatewayConfig.routes(routeLocatorBuilder, authGatewayFilterFactory);

        // Assert
        assertNotNull(routeLocator);
        verify(routesBuilder, times(1)).route(eq("product-service"), any());
    }

    @Test
    void givenSaleServiceRoute_whenRoutesMethodIsCalled_thenRouteIsConfiguredCorrectly() {
        // Act
        RouteLocator routeLocator = gatewayConfig.routes(routeLocatorBuilder, authGatewayFilterFactory);

        // Assert
        assertNotNull(routeLocator);
        verify(routesBuilder, times(1)).route(eq("sale-service"), any());
    }

    @Test
    void givenReportServiceRoute_whenRoutesMethodIsCalled_thenRouteIsConfiguredCorrectly() {
        // Act
        RouteLocator routeLocator = gatewayConfig.routes(routeLocatorBuilder, authGatewayFilterFactory);

        // Assert
        assertNotNull(routeLocator);
        verify(routesBuilder, times(1)).route(eq("report-service"), any());
    }
}
