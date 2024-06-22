package com.bit.saleservice.controller;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.service.SaleService;
import com.bit.saleservice.wrapper.PageWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @InjectMocks
    private SaleController saleController;

    @Mock
    private SaleService saleService;

    @Test
    void testGetSaleById_ReturnsSaleResponse_WhenIdIsValid() {
        // Arrange
        Long id = 1L;
        SaleResponse saleResponse = new SaleResponse();
        when(saleService.getSale(id)).thenReturn(saleResponse);

        // Act
        ResponseEntity<SaleResponse> response = saleController.getSale(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleResponse, response.getBody());
        verify(saleService).getSale(id);
    }

    @Test
    void testGetAllSales_ReturnsListOfSaleResponses_WhenNoFiltersAreApplied() {
        // Arrange
        List<SaleResponse> saleResponses = List.of(new SaleResponse(), new SaleResponse());
        when(saleService.getAllSales()).thenReturn(saleResponses);

        // Act
        ResponseEntity<List<SaleResponse>> response = saleController.getAllSales();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleResponses, response.getBody());
        verify(saleService).getAllSales();
    }

    @Test
    void testGetDeletedSales_ReturnsListOfSaleResponses_WhenNoFiltersAreApplied() {
        // Arrange
        List<SaleResponse> deletedSaleResponses = List.of(new SaleResponse(), new SaleResponse());
        when(saleService.getDeletedSales()).thenReturn(deletedSaleResponses);

        // Act
        ResponseEntity<List<SaleResponse>> response = saleController.getDeletedSales();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(deletedSaleResponses, response.getBody());
        verify(saleService).getDeletedSales();
    }

    @Test
    void testGetAllSalesFilteredAndSorted_ReturnsPageWrapperOfSaleResponses_WhenFiltersAreApplied() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String direction = "ASC";
        String cashier = "cashier";
        String paymentMethod = "paymentMethod";
        BigDecimal minPrice = BigDecimal.valueOf(10);
        BigDecimal maxPrice = BigDecimal.valueOf(20);
        String startDate = "2022-01-01";
        String endDate = "2022-01-31";
        Page<SaleResponse> saleResponses = new PageImpl<>(List.of(new SaleResponse()), PageRequest.of(0, 10), 1);
        PageWrapper<SaleResponse> response = new PageWrapper<>();
        response.setContent(saleResponses.getContent());
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(saleResponses.getTotalElements());
        when(saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate)).thenReturn(response);

        // Act
        ResponseEntity<PageWrapper<SaleResponse>> result = saleController.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(saleService).getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
    }

    @Test
    void testCreateSale_ReturnsSaleResponse_WhenSaleRequestIsValid() throws HeaderProcessingException {
        // Arrange
        SaleRequest saleRequest = new SaleRequest();
        SaleResponse saleResponse = new SaleResponse();
        when(saleService.createSale(saleRequest)).thenReturn(saleResponse);

        // Act
        ResponseEntity<SaleResponse> response = saleController.createSale(saleRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saleResponse, response.getBody());
        verify(saleService).createSale(saleRequest);
    }

    @Test
    void testUpdateSale_ReturnsSaleResponse_WhenSaleRequestIsValid() throws HeaderProcessingException {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        SaleResponse saleResponse = new SaleResponse();
        when(saleService.updateSale(id, saleRequest)).thenReturn(saleResponse);

        // Act
        ResponseEntity<SaleResponse> response = saleController.updateSale(id, saleRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleResponse, response.getBody());
        verify(saleService).updateSale(id, saleRequest);
    }

    @Test
    void testCancelSale_ReturnsSuccessMessage_WhenIdIsValid() {
        // Arrange
        Long id = 1L;

        // Act
        ResponseEntity<String> response =saleController.cancelSale(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sale cancelled successfully!", response.getBody());
        verify(saleService).cancelSale(id);
    }

    @Test
    void testRestoreSale_ReturnsSaleResponse_WhenIdIsValid() {
        // Arrange
        Long id = 1L;
        SaleResponse saleResponse = new SaleResponse();
        when(saleService.restoreSale(id)).thenReturn(saleResponse);

        // Act
        ResponseEntity<SaleResponse> response = saleController.restoreSale(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(saleResponse, response.getBody());
        verify(saleService).restoreSale(id);
    }

    @Test
    void testDeleteSale_ReturnsSuccessMessage_WhenIdIsValid() {
        // Arrange
        Long id = 1L;

        // Act
        ResponseEntity<String> response = saleController.deleteSale(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sale soft deleted successfully!", response.getBody());
        verify(saleService).deleteSale(id);
    }

    @Test
    void testDeleteSalePermanently_ReturnsSuccessMessage_WhenIdIsValid() {
        // Arrange
        Long id = 1L;

        // Act
        ResponseEntity<String> response = saleController.deleteSalePermanently(id);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sale deleted permanently!", response.getBody());
        verify(saleService).deleteSalePermanently(id);
    }
}