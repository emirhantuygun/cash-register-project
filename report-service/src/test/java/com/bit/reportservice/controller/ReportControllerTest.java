package com.bit.reportservice.controller;

import com.bit.reportservice.controller.ReportController;
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
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    private SaleResponse saleResponse;
    private Page<SaleResponse> saleResponsePage;

    @BeforeEach
    public void setup() {
        saleResponse = new SaleResponse();
        saleResponsePage = new PageImpl<>(Collections.singletonList(saleResponse), PageRequest.of(0, 10), 1);
    }

    @Test
    public void testGetSaleById_ReturnsSaleResponse() throws HeaderProcessingException {
        Long saleId = 1L;
        when(reportService.getSale(saleId)).thenReturn(saleResponse);

        ResponseEntity<SaleResponse> response = reportController.getSale(saleId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(saleResponse, response.getBody());
        verify(reportService, times(1)).getSale(saleId);
    }

    @Test
    public void testGetAllSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        when(reportService.getAllSales()).thenReturn(Collections.singletonList(saleResponse));

        ResponseEntity<List<SaleResponse>> response = reportController.getAllSales();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(saleResponse, response.getBody().get(0));
        verify(reportService, times(1)).getAllSales();
    }

    @Test
    public void testGetDeletedSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        when(reportService.getDeletedSales()).thenReturn(Collections.singletonList(saleResponse));

        ResponseEntity<List<SaleResponse>> response = reportController.getDeletedSales();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(saleResponse, response.getBody().get(0));
        verify(reportService, times(1)).getDeletedSales();
    }

    @Test
    public void testGetAllSalesFilteredAndSorted_ReturnsPageOfSaleResponse() throws HeaderProcessingException {
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

        ResponseEntity<Page<SaleResponse>> response = reportController.getAllSalesFilteredAndSorted(
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(saleResponse, response.getBody().getContent().get(0));
        verify(reportService, times(1)).getAllSalesFilteredAndSorted(
                eq(page), eq(size), eq(sortBy), eq(direction), eq(cashier), eq(paymentMethod), eq(minPrice), eq(maxPrice), eq(startDate), eq(endDate));
    }

    @Test
    public void testGetReceiptById_ReturnsPdfBytes() throws ReceiptGenerationException, HeaderProcessingException {
        Long saleId = 1L;
        byte[] pdfBytes = new byte[]{1, 2, 3, 4};
        when(reportService.getReceipt(saleId)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = reportController.getReceipt(saleId);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertArrayEquals(pdfBytes, response.getBody());
        verify(reportService, times(1)).getReceipt(saleId);
    }
}

