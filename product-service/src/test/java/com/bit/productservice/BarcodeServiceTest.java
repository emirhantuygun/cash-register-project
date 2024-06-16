package com.bit.productservice;

import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.service.BarcodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {

    @InjectMocks
    private BarcodeService barcodeService;

    @Value("${company.name}")
    private String COMPANY_NAME;

    @Test
    void generateBarcodeNumber_shouldGenerateBarcodeNumber() throws AlgorithmNotFoundException {
        // Arrange
        String productName = "Product1";
        String expectedBarcode = "1000000000001";

        // Act
        String barcodeNumber = barcodeService.generateBarcodeNumber(productName);

        // Assert
        assertEquals(expectedBarcode, barcodeNumber);
    }

    @Test
    void generateBarcodeNumber_shouldHandleZeroFirstDigit() throws AlgorithmNotFoundException {
        // Arrange
        String productName = "Product1";
        String expectedBarcode = "2000000000001";

        // Mocking the value of COMPANY_NAME to be "200 Company"
        when(COMPANY_NAME.replaceAll("\\D+", "")).thenReturn("200");

        // Act
        String barcodeNumber = barcodeService.generateBarcodeNumber(productName);

        // Assert
        assertEquals(expectedBarcode, barcodeNumber);
    }

    @Test
    void generateBarcodeNumber_shouldThrowAlgorithmNotFoundException() throws AlgorithmNotFoundException {
        // Arrange
        String productName = "Product1";

        // Mocking NoSuchAlgorithmException
        doThrow(NoSuchAlgorithmException.class).when(barcodeService).generateBarcodeNumber(productName);

        // Assert
        assertThrows(AlgorithmNotFoundException.class, () -> barcodeService.generateBarcodeNumber(productName));
    }
}
