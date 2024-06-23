package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class implements the ReportService interface and provides methods for generating sales reports.
 * It uses the GatewayService and ReceiptService to fetch and process data.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final GatewayService gatewayService;
    private final ReceiptService receiptService;

    @Override
    public SaleResponse getSale(Long id) throws HeaderProcessingException {
        log.trace("Entering getSale method in ReportServiceImpl");
        SaleResponse saleResponse = gatewayService.getSale(id);

        log.trace("Exiting getSale method in ReportServiceImpl");
        return saleResponse;
    }

    @Override
    public List<SaleResponse> getAllSales() throws HeaderProcessingException {
        log.trace("Entering getAllSales method in ReportServiceImpl");
        List<SaleResponse> saleResponses = gatewayService.getAllSales();

        log.trace("Exiting getAllSales method in ReportServiceImpl");
        return saleResponses;
    }

    @Override
    public List<SaleResponse> getDeletedSales() throws HeaderProcessingException {
        log.trace("Entering getDeletedSales method in ReportServiceImpl");
        List<SaleResponse> deletedSaleResponses = gatewayService.getDeletedSales();

        log.trace("Exiting getDeletedSales method in ReportServiceImpl");
        return deletedSaleResponses;
    }

    @Override
    public Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction, String cashier, String paymentMethod, BigDecimal minTotal, BigDecimal maxTotal, String startDate, String endDate) throws HeaderProcessingException {
        log.trace("Entering getAllSalesFilteredAndSorted method in ReportServiceImpl");
        Page<SaleResponse> saleResponses = gatewayService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        log.trace("Exiting getAllSalesFilteredAndSorted method in ReportServiceImpl");
        return saleResponses;
    }

    @Override
    public byte[] getReceipt(Long id) throws HeaderProcessingException, ReceiptGenerationException {
        log.trace("Entering getReceipt method in ReportServiceImpl");
        SaleResponse saleResponse = getSale(id);

        byte[] pdfBytes = receiptService.generateReceipt(saleResponse);
        log.trace("Exiting getReceipt method in ReportServiceImpl");
        return pdfBytes;
    }
}

