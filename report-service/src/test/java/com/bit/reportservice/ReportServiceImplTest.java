package com.bit.reportservice;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.bit.reportservice.service.GatewayService;
import com.bit.reportservice.service.ReceiptService;
import com.bit.reportservice.service.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private GatewayService gatewayService;

    @Mock
    private ReceiptService receiptService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetSale_ReturnsSaleResponse() throws HeaderProcessingException {
        Long id = 1L;
        SaleResponse saleResponse = new SaleResponse();
        
        when(gatewayService.getSale(id)).thenReturn(saleResponse);

        SaleResponse result = reportService.getSale(id);

        assertThat(result).isEqualTo(saleResponse);
        verify(gatewayService, times(1)).getSale(id);
    }

    @Test
    public void testGetAllSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
        
        when(gatewayService.getAllSales()).thenReturn(saleResponseList);

        List<SaleResponse> result = reportService.getAllSales();

        assertThat(result).isEqualTo(saleResponseList);
        verify(gatewayService, times(1)).getAllSales();
    }

    @Test
    public void testGetDeletedSales_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
        
        when(gatewayService.getDeletedSales()).thenReturn(saleResponseList);

        List<SaleResponse> result = reportService.getDeletedSales();

        assertThat(result).isEqualTo(saleResponseList);
        verify(gatewayService, times(1)).getDeletedSales();
    }

    @Test
    public void testGetAllSalesFilteredAndSorted_ReturnsPageOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
        Page<SaleResponse> saleResponsePage = new PageImpl<>(saleResponseList);
        
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

        when(gatewayService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate))
                .thenReturn(saleResponsePage);

        Page<SaleResponse> result = reportService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        assertThat(result).isEqualTo(saleResponsePage);
        verify(gatewayService, times(1)).getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
    }

    @Test
    public void testGetReceipt_ReturnsReceiptBytes() throws HeaderProcessingException, ReceiptGenerationException {
        Long id = 1L;
        SaleResponse saleResponse = new SaleResponse();
        byte[] receiptBytes = new byte[]{1, 2, 3};
        
        when(gatewayService.getSale(id)).thenReturn(saleResponse);
        when(receiptService.generateReceipt(saleResponse)).thenReturn(receiptBytes);

        byte[] result = reportService.getReceipt(id);

        assertThat(result).isEqualTo(receiptBytes);
        verify(gatewayService, times(1)).getSale(id);
        verify(receiptService, times(1)).generateReceipt(saleResponse);
    }

    @Test
    public void testGetReceipt_ThrowsHeaderProcessingException() throws HeaderProcessingException {
        Long id = 1L;
        
        when(gatewayService.getSale(id)).thenThrow(HeaderProcessingException.class);

        assertThrows(HeaderProcessingException.class, () -> reportService.getReceipt(id));
    }

    @Test
    public void testGetReceipt_ThrowsReceiptGenerationException() throws HeaderProcessingException, ReceiptGenerationException {
        Long id = 1L;
        SaleResponse saleResponse = new SaleResponse();
        
        when(gatewayService.getSale(id)).thenReturn(saleResponse);
        when(receiptService.generateReceipt(saleResponse)).thenThrow(ReceiptGenerationException.class);

        assertThrows(ReceiptGenerationException.class, () -> reportService.getReceipt(id));
    }
}
