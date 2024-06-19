package com.bit.saleservice.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SaleServiceImplTest {

    @InjectMocks
    private SaleServiceImpl saleService;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private GatewayService gatewayService;


    @Test
    void testGetSale_ExistingSale_ReturnsSaleResponse() {
        // Arrange
        Long id = 1L;
        Sale sale = new Sale();
        sale.setId(id);
        sale.setPaymentMethod(Payment.PAYPAL);
        Product product = new Product();
        product.setSale(sale);
        sale.setProducts(List.of(product));
        when(saleRepository.findById(id)).thenReturn(Optional.of(sale));

        // Act
        SaleResponse saleResponse = saleService.getSale(id);

        // Assert
        assertEquals(sale.getId(), saleResponse.getId());
    }

    @Test
    void testGetSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
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

    @Test
    void testGetAllSalesFilteredAndSorted_ReturnsPageSaleResponse() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String direction = "ASC";
        String cashier = "cashier";
        String paymentMethod = "CASH";
        BigDecimal minTotal = BigDecimal.ZERO;
        BigDecimal maxTotal = BigDecimal.TEN;
        String startDate = "2022-01-01";
        String endDate = "2022-01-31";

        Page<Sale> salesPage = Page.empty();
        when(saleRepository.findAll((Specification<Sale>) any(), any())).thenReturn(salesPage);

        // Act
        Page<SaleResponse> saleResponses = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        // Assert
        assertEquals(salesPage.getTotalElements(), saleResponses.getTotalElements());
    }

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
    void testUpdateSale_ExistingSale_ReturnsUpdatedSaleResponse() throws HeaderProcessingException {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setCashier("New Cashier");
        saleRequest.setPaymentMethod("paypal");
        ProductRequest productRequest = new ProductRequest();
        productRequest.setId(id);
        productRequest.setQuantity(1);
        saleRequest.setProducts(List.of(productRequest));
        Sale existingSale = new Sale();
        existingSale.setId(id);
        existingSale.setCashier("Old Cashier");
        existingSale.setPaymentMethod(Payment.PAYPAL);
        Product product = new Product();
        product.setId(id);
        product.setQuantity(1);
        existingSale.setProducts(List.of(product));
        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act
        SaleResponse saleResponse = saleService.updateSale(id, saleRequest);

        // Assert
        assertEquals("New Cashier", saleResponse.getCashier());
        verify(saleRepository).save(existingSale);
    }

    @Test
    void testUpdateSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.updateSale(id, saleRequest));
    }

    @Test
    void testCancelSale_ExistingSale_CancelsSale() {
        // Arrange
        Long id =1L;
        Sale existingSale = new Sale();
        Product product = new Product();
        product.setId(id);
        product.setQuantity(1);
        existingSale.setProducts(List.of(product));
        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act
        saleService.cancelSale(id);

        // Assert
        verify(saleRepository).save(existingSale);
    }

    @Test
    void testCancelSale_NonExistingSale_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.cancelSale(id));
    }

    @Test
    void testRestoreSale_NonExistingSoftDeletedSale_ThrowsSaleNotSoftDeletedException() {
        // Arrange
        Long id = 1L;
        Sale existingSale = new Sale();
        lenient().when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act & Assert
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

        // Act & Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.deleteSale(id));
    }

    @Test
    void testDeleteSalePermanently_ExistingSale_DeletesSalePermanently() {
        // Arrange
        Long id = 1L;
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

        // Act & Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.deleteSalePermanently(id));
    }
}