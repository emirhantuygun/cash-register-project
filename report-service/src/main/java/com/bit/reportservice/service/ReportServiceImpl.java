package com.bit.reportservice.service;

import com.bit.reportservice.ReportServiceApplication;
import com.bit.reportservice.dto.SaleResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService{

    private static final Logger logger = LogManager.getLogger(ReportServiceApplication.class);
    private final GatewayService gatewayService;
    private final ReceiptService receiptService;


    @Override
    public SaleResponse getSale(Long id) {
        logger.info("Fetching sale with ID: {}", id);
        return gatewayService.getSale(id);
    }

    @Override
    public List<SaleResponse> getAllSales() {
        logger.info("Fetching all sales");
        return gatewayService.getAllSales();
    }

    @Override
    public List<SaleResponse> getDeletedSales() {
        logger.info("Fetching all deleted sales");
        return gatewayService.getDeletedSales();
    }

    @Override
    public Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction, String cashier, String paymentMethod, BigDecimal minPrice, BigDecimal maxPrice, String startDate, String endDate) {
        logger.info("Fetching all deleted sales");
        return gatewayService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
    }

    @Override
    public byte[] getReceipt(Long id) {
        SaleResponse saleResponse = getSale(id);
        return receiptService.generateReceipt(saleResponse);
    }
}