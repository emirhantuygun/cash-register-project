package com.bit.reportservice;

import com.bit.reportservice.controller.ReportController;
import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(reportService)).build();
    }

    @Test
    public void testGetSaleById_ReturnsSaleResponse() throws Exception {
        Long saleId = 1L;
        SaleResponse saleResponse = new SaleResponse();
        when(reportService.getSale(saleId)).thenReturn(saleResponse);

        mockMvc.perform(get("/reports/{id}", saleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").exists());

        verify(reportService, times(1)).getSale(saleId);
    }

    @Test
    public void testGetAllSales_ReturnsListOfSaleResponse() throws Exception {
        List<SaleResponse> saleResponses = Collections.singletonList(new SaleResponse());
        when(reportService.getAllSales()).thenReturn(saleResponses);

        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());

        verify(reportService, times(1)).getAllSales();
    }

    @Test
    public void testGetDeletedSales_ReturnsListOfSaleResponse() throws Exception {
        List<SaleResponse> deletedSaleResponses = Collections.singletonList(new SaleResponse());
        when(reportService.getDeletedSales()).thenReturn(deletedSaleResponses);

        mockMvc.perform(get("/reports/deleted"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").exists());

        verify(reportService, times(1)).getDeletedSales();
    }

    @Test
    public void testGetAllSalesFilteredAndSorted_ReturnsPageOfSaleResponse() throws Exception {
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

        Page<SaleResponse> saleResponses = new PageImpl<>(Collections.singletonList(new SaleResponse()));
        when(reportService.getAllSalesFilteredAndSorted(
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate))
                .thenReturn(saleResponses);

        mockMvc.perform(get("/reports/filteredAndSorted")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("sortBy", sortBy)
                        .param("direction", direction)
                        .param("cashier", cashier)
                        .param("paymentMethod", paymentMethod)
                        .param("minPrice", minPrice.toString())
                        .param("maxPrice", maxPrice.toString())
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0]").exists());

        verify(reportService, times(1)).getAllSalesFilteredAndSorted(
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
    }

    @Test
    public void testGetReceiptById_ReturnsPdfBytes() throws Exception {
        Long saleId = 1L;
        byte[] pdfBytes = new byte[]{1, 2, 3, 4};
        when(reportService.getReceipt(saleId)).thenReturn(pdfBytes);

        mockMvc.perform(get("/reports/receipt/{id}", saleId))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"receipt" + saleId + ".pdf\""))
                .andExpect(content().bytes(pdfBytes));

        verify(reportService, times(1)).getReceipt(saleId);
    }
}
