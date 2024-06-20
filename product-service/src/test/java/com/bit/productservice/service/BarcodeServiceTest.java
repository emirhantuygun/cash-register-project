package com.bit.productservice.service;

import com.bit.productservice.exception.AlgorithmNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {

    @InjectMocks
    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(barcodeService, "COMPANY_NAME", "COMPANY123");
    }

    @Test
    void givenProductName_whenGenerateBarcodeNumber_thenReturnsBarcodeNumber() throws AlgorithmNotFoundException {
        // Arrange
        String productName = "Product";

        // Act
        String barcodeNumber = barcodeService.generateBarcodeNumber(productName);

        // Assert
        assertEquals("1223932356863", barcodeNumber);
    }

    @Test
    void givenProductNameAndNoAlgorithm_whenGenerateBarcodeNumber_thenThrowsAlgorithmNotFoundException() {
        // Arrange
        String productName = "Product";

        try (MockedStatic<MessageDigest> mocked = mockStatic(MessageDigest.class)) {

            mocked.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(NoSuchAlgorithmException.class);

            // Act & Assert
            assertThrows(AlgorithmNotFoundException.class, () -> barcodeService.generateBarcodeNumber(productName));
        }
    }
}
