package com.bit.saleservice.service;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.wrapper.PageWrapper;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.List;

public interface SaleService {

    SaleResponse createSale(SaleRequest saleRequest) throws HeaderProcessingException;

    SaleResponse getSale(Long id);

    List<SaleResponse> getAllSales();

    List<SaleResponse> getDeletedSales();

    PageWrapper<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                           String cashier, String paymentMethod,
                                                           BigDecimal minTotal, BigDecimal maxTotal,
                                                           String startDate, String endDate);

    SaleResponse updateSale(Long id, SaleRequest saleRequest) throws HeaderProcessingException;

    void cancelSale(Long id);

    SaleResponse restoreSale(Long id);

    void deleteSale(Long id);

    void deleteSalePermanently(Long id);
}
