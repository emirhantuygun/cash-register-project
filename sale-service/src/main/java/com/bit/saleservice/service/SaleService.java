package com.bit.saleservice.service;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.List;

public interface SaleService {

    SaleResponse createSale(SaleRequest saleRequest);

    SaleResponse getSale(Long id);

    List<SaleResponse> getAllSales();

    List<SaleResponse> getDeletedSales();

    Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                    String cashier, String paymentMethod,
                                                    BigDecimal minTotal, BigDecimal maxTotal,
                                                    String startDate, String endDate);

    SaleResponse updateSale(Long id, SaleRequest saleRequest);

    SaleResponse restoreSale(Long id);

    void deleteSale(Long id);

    void deleteSalePermanently(Long id);
}
