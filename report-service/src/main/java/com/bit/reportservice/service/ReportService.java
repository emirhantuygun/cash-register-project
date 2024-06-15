package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {
    SaleResponse getSale(Long id) throws HeaderProcessingException;

    List<SaleResponse> getAllSales() throws HeaderProcessingException;

    List<SaleResponse> getDeletedSales() throws HeaderProcessingException;

    Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                    String cashier, String paymentMethod,
                                                    BigDecimal minPrice, BigDecimal maxPrice,
                                                    String startDate, String endDate) throws HeaderProcessingException;

    byte[] getReceipt(Long id) throws HeaderProcessingException, ReceiptGenerationException;

}
