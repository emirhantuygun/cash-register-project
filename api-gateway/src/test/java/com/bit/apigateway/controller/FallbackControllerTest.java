package com.bit.apigateway.controller;

import com.bit.apigateway.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class FallbackControllerTest {

    @InjectMocks
    private FallbackController fallbackController;

    @Test
    public void whenFallbackAuthEndpointIsCalled_thenAuthServiceUnavailableExceptionIsThrown() {

        // Act & Assert
        Exception exception = assertThrows(AuthServiceUnavailableException.class,
                () -> fallbackController.fallbackAuth());

        assertEquals("Auth Service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    public void whenFallbackUserEndpointIsCalled_thenUserServiceUnavailableExceptionIsThrown() {

        // Act & Assert
        Exception exception = assertThrows(UserServiceUnavailableException.class,
                () -> fallbackController.fallbackUser());

        assertEquals("User Service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    public void whenFallbackProductEndpointIsCalled_thenProductServiceUnavailableExceptionIsThrown() {

        // Act & Assert
        Exception exception = assertThrows(ProductServiceUnavailableException.class,
                () -> fallbackController.fallbackProduct());

        assertEquals("Product Service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    public void whenFallbackSaleEndpointIsCalled_thenSaleServiceUnavailableExceptionIsThrown() {

        // Act & Assert
        Exception exception = assertThrows(SaleServiceUnavailableException.class,
                () -> fallbackController.fallbackSale());

        assertEquals("Sale Service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    public void whenFallbackReportEndpointIsCalled_thenReportServiceUnavailableExceptionIsThrown() {

        // Act & Assert
        Exception exception = assertThrows(ReportServiceUnavailableException.class,
                () -> fallbackController.fallbackReport());

        assertEquals("Report Service is temporarily unavailable. Please try again later.", exception.getMessage());
    }
}
