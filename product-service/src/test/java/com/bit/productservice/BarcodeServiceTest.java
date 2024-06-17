package com.bit.productservice;

import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.service.BarcodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BarcodeServiceTest {

    @InjectMocks
    private BarcodeService barcodeService;

    @Mock
    private MessageDigest messageDigest;

    @BeforeEach
    public void setUp() {
//        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(barcodeService, "COMPANY_NAME", "COMPANY123");
    }

    @Test
    public void givenProductName_whenGenerateBarcodeNumber_thenReturnsBarcodeNumber() throws AlgorithmNotFoundException {
        // Given
        String productName = "Product";
        byte[] hashBytes = "hash".getBytes();
        when(messageDigest.digest(any())).thenReturn(hashBytes);
        String string = mock();
        when(string.format(anyString(), anyLong())).thenReturn("0123456789");

        // When
        String barcodeNumber = barcodeService.generateBarcodeNumber(productName);

        // Then
        assertEquals("0123456789", barcodeNumber);
    }

    @Test
    public void givenProductNameAndNoAlgorithm_whenGenerateBarcodeNumber_thenThrowsAlgorithmNotFoundException() throws NoSuchAlgorithmException {
        // Given
        String productName = "Product";
        doThrow(new NoSuchAlgorithmException()).when(messageDigest).getInstance("SHA-256");

        // Then
        assertThrows(AlgorithmNotFoundException.class, () -> barcodeService.generateBarcodeNumber(productName));
    }
}
