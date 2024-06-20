package com.bit.saleservice.service;

import com.bit.saleservice.dto.*;
import com.bit.saleservice.entity.*;
import com.bit.saleservice.exception.*;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.bit.saleservice.wrapper.ProductStockReduceRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
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
    private RabbitTemplate rabbitTemplate;

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
    void testUpdateSale_SamePaymentMethod_ThrowsPaymentMethodUpdateNotAllowedException() {
        // Arrange
        Long id = 1L;
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setPaymentMethod("paypal");
        Sale existingSale = new Sale();
        existingSale.setId(id);
        existingSale.setPaymentMethod(Payment.CREDIT_CARD);

        when(saleRepository.findById(id)).thenReturn(Optional.of(existingSale));

        // Act & Assert
        assertThrows(PaymentMethodUpdateNotAllowedException.class, () -> saleService.updateSale(id, saleRequest));
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

    @Test
    void processCampaigns_shouldReturnCampaignProcessResult_whenCampaignsAreProcessedSuccessfully() {
        // Arrange
        List<Long> campaignIds = List.of(1L, 2L);
        List<Product> products = List.of(new Product());
        BigDecimal total = BigDecimal.valueOf(100);

        CampaignProcessResponse campaignProcessResponse = new CampaignProcessResponse();
        campaignProcessResponse.setProducts(products);
        campaignProcessResponse.setTotal(total);

        when(campaignProcessService.processCampaigns(any(CampaignProcessRequest.class))).thenReturn(campaignProcessResponse);
        when(campaignProcessService.getCampaigns(any(List.class))).thenReturn(List.of(new Campaign()));

        // Act
        CampaignProcessResult result = saleService.processCampaigns(campaignIds, products, total);

        // Assert
        assertNotNull(result);
        assertEquals(products, result.getProducts());
        assertEquals(total, result.getTotalWithCampaign());
        assertEquals(1, result.getCampaigns().size());
        verify(campaignProcessService, times(1)).processCampaigns(any(CampaignProcessRequest.class));
        verify(campaignProcessService, times(1)).getCampaigns(any(List.class));
    }

    @Test
    void processCashPayment_shouldReturnChange_whenCashIsSufficient() {
        // Arrange
        BigDecimal cash = BigDecimal.valueOf(150);
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act
        BigDecimal change = saleService.processCashPayment(cash, totalWithCampaign);

        // Assert
        assertEquals(BigDecimal.valueOf(50), change);
    }

    @Test
    void processCashPayment_shouldThrowException_whenCashIsNotProvided() {
        // Arrange
        BigDecimal cash = null;
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(CashNotProvidedException.class, () -> saleService.processCashPayment(cash, totalWithCampaign));
    }

    @Test
    void processCashPayment_shouldThrowException_whenCashIsInsufficient() {
        // Arrange
        BigDecimal cash = BigDecimal.valueOf(50);
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(InsufficientCashException.class, () -> saleService.processCashPayment(cash, totalWithCampaign));
    }

    @Test
    void processMixedPayment_shouldReturnChange_whenMixedPaymentIsSufficient() {
        // Arrange
        MixedPayment mixedPayment = new MixedPayment();
        mixedPayment.setCashAmount(BigDecimal.valueOf(50));
        mixedPayment.setCreditCardAmount(BigDecimal.valueOf(60));
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act
        BigDecimal change = saleService.processMixedPayment(mixedPayment, totalWithCampaign);

        // Assert
        assertEquals(BigDecimal.valueOf(10), change);
    }

    @Test
    void processMixedPayment_shouldThrowException_whenMixedPaymentIsNull() {
        // Arrange
        MixedPayment mixedPayment = null;
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(MixedPaymentNotFoundException.class, () -> saleService.processMixedPayment(mixedPayment, totalWithCampaign));
    }

    @Test
    void processMixedPayment_shouldThrowException_whenMixedPaymentIsInvalid() {
        // Arrange
        MixedPayment mixedPayment = new MixedPayment();
        mixedPayment.setCashAmount(null);
        mixedPayment.setCreditCardAmount(null);
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(InvalidMixedPaymentException.class, () -> saleService.processMixedPayment(mixedPayment, totalWithCampaign));
    }

    @Test
    void processMixedPayment_shouldThrowException_whenTotalPaymentIsInsufficient() {
        // Arrange
        MixedPayment mixedPayment = new MixedPayment();
        mixedPayment.setCashAmount(BigDecimal.valueOf(30));
        mixedPayment.setCreditCardAmount(BigDecimal.valueOf(40));
        BigDecimal totalWithCampaign = BigDecimal.valueOf(100);

        // Act & Assert
        assertThrows(InsufficientMixedPaymentException.class, () -> saleService.processMixedPayment(mixedPayment, totalWithCampaign));
    }
    @Test
    void givenValidFilters_whenGetAllSalesFilteredAndSorted_thenReturnsFilteredAndSortedSales() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "ASC";
        String cashier = "john";
        String paymentMethod = "CASH";
        BigDecimal minTotal = BigDecimal.valueOf(50);
        BigDecimal maxTotal = BigDecimal.valueOf(500);
        String startDate = "2023-01-01";
        String endDate = "2023-12-31";

        Sale sale = new Sale();
        sale.setPaymentMethod(Payment.PAYPAL);
        sale.setProducts(List.of(Product.builder().sale(sale).build()));
        Page<Sale> salePage = new PageImpl<>(Collections.singletonList(sale));
        when(saleRepository.findAll((Specification<Sale>) any(), any(Pageable.class))).thenReturn(salePage);

        // Act
        Page<SaleResponse> result = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(saleRepository, times(1)).findAll((Specification<Sale>) any(), any(Pageable.class));
    }

    @Test
    void givenInvalidDirection_whenGetAllSalesFilteredAndSorted_thenThrowsIllegalArgumentException() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "INVALID";
        String cashier = "";
        String paymentMethod = "";
        BigDecimal minTotal = null;
        BigDecimal maxTotal = null;
        String startDate = "";
        String endDate = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate));
    }

    @Test
    void givenValidFiltersWithInvalidDates_whenGetAllSalesFilteredAndSorted_thenLogsErrorAndReturnsSales() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "ASC";
        String cashier = "";
        String paymentMethod = "";
        BigDecimal minTotal = null;
        BigDecimal maxTotal = null;
        String startDate = "invalid-date";
        String endDate = "invalid-date";

        Sale sale = new Sale();
        sale.setPaymentMethod(Payment.PAYPAL);
        sale.setProducts(List.of(Product.builder().sale(sale).build()));
        Page<Sale> salePage = new PageImpl<>(Collections.singletonList(sale));
        when(saleRepository.findAll((Specification<Sale>) any(), any(Pageable.class))).thenReturn(salePage);

        // Act
        Page<SaleResponse> result = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(saleRepository, times(1)).findAll((Specification<Sale>) any(), any(Pageable.class));
    }

    @Test
    void givenEmptyResult_whenGetAllSalesFilteredAndSorted_thenReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "date";
        String direction = "ASC";
        String cashier = "";
        String paymentMethod = "";
        BigDecimal minTotal = null;
        BigDecimal maxTotal = null;
        String startDate = "";
        String endDate = "";

        Page<Sale> salePage = Page.empty();
        when(saleRepository.findAll((Specification<Sale>) any(), any(Pageable.class))).thenReturn(salePage);

        // Act
        Page<SaleResponse> result = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(saleRepository, times(1)).findAll((Specification<Sale>) any(), any(Pageable.class));
    }


    @Test
    void testReduceStocks_shouldSendReduceMessagesToRabbitMQ() {
        // Arrange
        ReflectionTestUtils.setField(saleService, "EXCHANGE", "some-exchange");
        ReflectionTestUtils.setField(saleService, "ROUTING_KEY", "some-routing-key");
        Product product1 = Product.builder()
                .productId(1L)
                .quantity(10)
                .build();

        Product product2 = Product.builder()
                .productId(2L)
                .quantity(5)
                .build();

        List<Product> products = List.of(product1, product2);

        // Act
        saleService.reduceStocks(products);

        // Assert
        ArgumentCaptor<ProductStockReduceRequest> captor = ArgumentCaptor.forClass(ProductStockReduceRequest.class);
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), captor.capture());

        List<ProductStockReduceRequest> allValues = captor.getAllValues();
        assertEquals(1L, allValues.get(0).getId());
        assertEquals(10, allValues.get(0).getRequestedQuantity());
        assertEquals(2L, allValues.get(1).getId());
        assertEquals(5, allValues.get(1).getRequestedQuantity());
    }

    @Test
    void testReduceStocks_shouldThrowRabbitMQExceptionWhenSendingFails() {
        // Arrange
        Product product1 = Product.builder()
                .productId(1L)
                .quantity(10)
                .build();

        Product product2 = Product.builder()
                .productId(2L)
                .quantity(5)
                .build();

        List<Product> products = List.of(product1, product2);

        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> saleService.reduceStocks(products));
        assertEquals("Failed to send reduce message to RabbitMQ", exception.getMessage());
    }

    @Test
    void testRestoreSale_SaleFoundAndRestored_ReturnsRestoredSaleResponse() {
        // Arrange
        Long id = 1L;
        Sale sale = Sale.builder().id(id).paymentMethod(Payment.PAYPAL).build();
        Product product = Product.builder().sale(sale).build();
        sale.setProducts(List.of(product));

        when(saleRepository.existsByIdAndDeletedTrue(id)).thenReturn(true);
        doNothing().when(productRepository).restoreProductsBySaleId(id);
        doNothing().when(saleRepository).restoreSale(id);
        when(saleRepository.findById(id)).thenReturn(Optional.of(sale));

        // Act
        SaleResponse result = saleService.restoreSale(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testRestoreSale_SaleNotFound_ThrowsSaleNotFoundException() {
        // Arrange
        Long id = 1L;
        when(saleRepository.existsByIdAndDeletedTrue(id)).thenReturn(true);
        doNothing().when(productRepository).restoreProductsBySaleId(id);
        doNothing().when(saleRepository).restoreSale(id);
        when(saleRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SaleNotFoundException.class, () -> saleService.restoreSale(id));
    }
}