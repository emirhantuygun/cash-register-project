package com.bit.saleservice;

import com.bit.saleservice.dto.ProductRequest;
import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.entity.Payment;
import com.bit.saleservice.entity.Product;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.SaleNotFoundException;
import com.bit.saleservice.exception.SaleNotSoftDeletedException;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import com.bit.saleservice.service.CampaignProcessService;
import com.bit.saleservice.service.GatewayService;
import com.bit.saleservice.service.SaleServiceImpl;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @InjectMocks
    private SaleServiceImpl saleService;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CampaignProcessService campaignProcessService;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private RabbitTemplate rabbitTemplate;

//    @Test
//    void testGetSale_ExistingSale_ReturnsSaleResponse() {
//        // Arrange
//        Long id = 1L;
//        Sale sale = new Sale();
//        when(saleRepository.findById(id)).thenReturn(Optional.of(sale));
//
//        // Act
//        SaleResponse saleResponse = saleService.getSale(id);
//
//        // Assert
//        assertEquals(sale, saleResponse.getSale());
//    }

    @Test
    void testGetSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.getSale(id));
    }

    @Test
    void testGetAllSales_ReturnsListSaleResponse() {
        // Arrange
        List<Sale> sales = new ArrayList<>();
        when(saleRepository.findAll()).thenReturn(sales);

        // Act
        List<SaleResponse> saleResponses = saleService.getAllSales();

        // Assert
        assertEquals(sales.size(), saleResponses.size());
    }

    @Test
    void testGetDeletedSales_ReturnsListSaleResponse() {
        // Arrange
        List<Sale> sales = new ArrayList<>();
        when(saleRepository.findSoftDeletedSales()).thenReturn(sales);

        // Act
        List<SaleResponse> saleResponses = saleService.getDeletedSales();

        // Assert
        assertEquals(sales.size(), saleResponses.size());
    }

//    @Test
//    void testGetAllSalesFilteredAndSorted_ReturnsPageSaleResponse() {
//        // Arrange
//        int page = 0;
//        int size = 10;
//        String sortBy = "id";
//        String direction = "ASC";
//        String cashier = "cashier";
//        String paymentMethod = "CASH";
//        BigDecimal minTotal = BigDecimal.ZERO;
//        BigDecimal maxTotal = BigDecimal.TEN;
//        String startDate = "2022-01-01";
//        String endDate = "2022-01-31";
//
//        Page<Sale> salesPage = Page.empty();
//        when(saleRepository.findAll(any(), any())).thenReturn(salesPage);
//
//        // Act
//        Page<SaleResponse> saleResponses = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);
//
//        // Assert
//        assertEquals(salesPage.getTotalElements(), saleResponses.getTotalElements());
//    }

    @Test
    void testCreateSale_ValidSaleRequest_ReturnsSaleResponse() throws HeaderProcessingException {
        // Arrange
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setProducts(List.of(new ProductRequest()));
        saleRequest.setPaymentMethod("paypal");
        when(saleRepository.save(any())).thenReturn(new Sale());

        // Act
        SaleResponse saleResponse = saleService.createSale(saleRequest);

        // Assert
        assertEquals(saleRequest.getCashier(), saleResponse.getCashier());
    }

    @Test
    void testUpdateSale_ValidSaleRequest_ReturnsSaleResponse() throws HeaderProcessingException {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setPaymentMethod("paypal");
        saleRequest.setProducts(List.of(new ProductRequest()));
        saleRequest.setCashier("John");
        Sale existingSale = new Sale();
        existingSale.setPaymentMethod(Payment.PAYPAL);
        existingSale.setProducts(List.of(new Product()));
        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));
        saleService = mock();
        doNothing().when(saleService).returnProducts(any(List.class));

        // Act
        SaleResponse saleResponse = saleService.updateSale(id, saleRequest);

        // Assert
        assertEquals(saleRequest.getCashier(), saleResponse.getCashier());
    }

    @Test
    void testUpdateSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.updateSale(id, saleRequest));
    }

//    @Test
//    void testCancelSale_ExistingSale_CancelsSale() throws HeaderProcessingException {
//        // Arrange
//        Long id =1L;
//        Sale existingSale = new Sale();
//        existingSale.setProducts(List.of(new Product()));
//        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));
//        ProductStockReturnRequest productStockReturnRequest = new ProductStockReturnRequest(1L, 1);
//        when(gatewayService.returnProducts(productStockReturnRequest)).thenReturn(null);
//
//        // Act
//        saleService.cancelSale(id);
//
//        // Assert
//        verify(saleRepository).save(existingSale);
//    }

    @Test
    void testCancelSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.cancelSale(id));
    }

//    @Test
//    void testRestoreSale_ExistingSoftDeletedSale_RestoresSale() {
//        // Arrange
//        Long id = 1L;
//        Sale existingSale = new Sale();
//        existingSale.setDeleted(true);
//        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));
//
//        // Act
//        saleService.restoreSale(id);
//
//        // Assert
//        verify(saleRepository).restoreSale(id);
//    }

    @Test
    void testRestoreSale_NonExistingSoftDeletedSale_ThrowsSaleNotSoftDeletedException() {
        // Arrange
        Long id = 1L;
        Sale existingSale = new Sale();
        lenient().when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act and Assert
        assertThrows(SaleNotSoftDeletedException.class, () -> saleService.restoreSale(id));
    }

    @Test
    void testDeleteSale_ExistingSale_DeletesSale() {
        // Arrange
        Long id = 1L;
        Sale existingSale = new Sale();
        lenient().when(saleRepository.existsById(id)).thenReturn(true);
        lenient().when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act
        saleService.deleteSale(id);

        // Assert
        verify(saleRepository).deleteById(id);
    }

    @Test
    void testDeleteSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        lenient().when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.deleteSale(id));
    }

    @Test
    void testDeleteSalePermanently_ExistingSale_DeletesSalePermanently() {
        // Arrange
        Long id = 1L;
        Sale existingSale = new Sale();
        when(saleRepository.existsById(id)).thenReturn(true);

        // Act
        saleService.deleteSalePermanently(id);

        // Assert
        verify(saleRepository).deletePermanently(id);
    }

    @Test
    void testDeleteSalePermanently_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        lenient().when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.deleteSalePermanently(id));
    }
}