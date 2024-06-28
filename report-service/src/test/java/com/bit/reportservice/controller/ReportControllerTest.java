package com.bit.reportservice.controller;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.bit.reportservice.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private SaleResponse saleResponse;
    private Page<SaleResponse> saleResponsePage;

    @BeforeEach
    void setup() {
        saleResponse = new SaleResponse();
        saleResponsePage = new PageImpl<>(Collections.singletonList(saleResponse), PageRequest.of(0, 10), 1);
    }

    @Test
    void testGetSaleById_ReturnsSaleResponse() throws HeaderProcessingException {
        // Arrange
        Long saleId = 1L;
        when(reportService.getSale(saleId)).thenReturn(saleResponse);

        // Act
        ResponseEntity<SaleResponse> response = reportController.getSale(saleId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(saleResponse, response.getBody());
        verify(reportService, times(1)).getSale(saleId);
    }

    @Test
    void testGetAllSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        // Arrange
        when(reportService.getAllSales()).thenReturn(Collections.singletonList(saleResponse));

        // Act
        ResponseEntity<List<SaleResponse>> response = reportController.getAllSales();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(saleResponse, response.getBody().get(0));
        verify(reportService, times(1)).getAllSales();
    }

    @Test
    void testGetDeletedSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        // Arrange
        when(reportService.getDeletedSales()).thenReturn(Collections.singletonList(saleResponse));

        // Act
        ResponseEntity<List<SaleResponse>> response = reportController.getDeletedSales();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(saleResponse, response.getBody().get(0));
        verify(reportService, times(1)).getDeletedSales();
    }

    @Test
    void testGetAllSalesFilteredAndSorted_ReturnsPageOfSaleResponse() throws HeaderProcessingException {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String direction = "ASC";
        String cashier = "cashier";
        String paymentMethod = "credit";
        BigDecimal minPrice = BigDecimal.valueOf(10);
        BigDecimal maxPrice = BigDecimal.valueOf(100);
        String startDate = "2022-01-01";
        String endDate = "2022-12-31";

        when(reportService.getAllSalesFilteredAndSorted(
                eq(page), eq(size), eq(sortBy), eq(direction), eq(cashier), eq(paymentMethod), eq(minPrice), eq(maxPrice), eq(startDate), eq(endDate)))
                .thenReturn(saleResponsePage);

        // Act
        ResponseEntity<Page<SaleResponse>> response = reportController.getAllSalesFilteredAndSorted(
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(saleResponse, response.getBody().getContent().get(0));
        verify(reportService, times(1)).getAllSalesFilteredAndSorted(
                eq(page), eq(size), eq(sortBy), eq(direction), eq(cashier), eq(paymentMethod), eq(minPrice), eq(maxPrice), eq(startDate), eq(endDate));
    }

    @Test
    void testGetReceiptById_ReturnsPdfBytes() throws ReceiptGenerationException, HeaderProcessingException {
        // Arrange
        Long saleId = 1L;
        byte[] pdfBytes = new byte[]{1, 2, 3, 4};
        when(reportService.getReceipt(saleId)).thenReturn(pdfBytes);

        // Act
        ResponseEntity<byte[]> response = reportController.getReceipt(saleId);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertArrayEquals(pdfBytes, response.getBody());
        verify(reportService, times(1)).getReceipt(saleId);
    }

    @Test
    void testGetChart_shouldReturnChartPdf_whenUnitIsDefaultMonth() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        when(reportService.getChart(anyString())).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = reportController.getChart("month");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(pdfBytes, response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
        assertTrue(headers.getContentDisposition().getFilename().contains("chart_month_"));
        assertEquals("must-revalidate, post-check=0, pre-check=0", headers.getCacheControl());

        verify(reportService, times(1)).getChart("month");
    }

    @Test
    void testGetChart_shouldReturnChartPdf_whenUnitIsDay() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        when(reportService.getChart(anyString())).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = reportController.getChart("day");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(pdfBytes, response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
        assertTrue(headers.getContentDisposition().getFilename().contains("chart_day_"));
        assertEquals("must-revalidate, post-check=0, pre-check=0", headers.getCacheControl());

        verify(reportService, times(1)).getChart("day");
    }

    @Test
    void testGetChart_shouldThrowReceiptGenerationException_whenServiceThrowsException() throws Exception {
        when(reportService.getChart(anyString())).thenThrow(new ReceiptGenerationException("Receipt generation failed", new RuntimeException()));

        assertThrows(ReceiptGenerationException.class, () -> reportController.getChart("day"));

        verify(reportService, times(1)).getChart("day");
    }

    @Test
    void testGetChart_shouldThrowHeaderProcessingException_whenServiceThrowsException() throws Exception {
        when(reportService.getChart(anyString())).thenThrow(new HeaderProcessingException("Header processing failed"));

        assertThrows(HeaderProcessingException.class, () -> reportController.getChart("day"));

        verify(reportService, times(1)).getChart("day");
    }

    @Test
    void testGetChart_shouldReturnChartPdfWithCorrectHeaders() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        when(reportService.getChart(anyString())).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = reportController.getChart("week");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(pdfBytes, response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
        assertTrue(headers.getContentDisposition().getFilename().contains("chart_week_"));
        assertEquals("must-revalidate, post-check=0, pre-check=0", headers.getCacheControl());

        verify(reportService, times(1)).getChart("week");
    }
}

