package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {
    SaleResponse getSale(Long id);

    List<SaleResponse> getAllSales();

    List<SaleResponse> getDeletedSales();

    Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                    String cashier, String paymentMethod,
                                                    BigDecimal minPrice, BigDecimal maxPrice,
                                                    String startDate, String endDate);

    byte[] getReceipt(Long id);

}
