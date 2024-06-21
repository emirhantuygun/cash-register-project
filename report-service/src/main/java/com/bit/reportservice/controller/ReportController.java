package com.bit.reportservice.controller;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import com.bit.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable("id") Long id) throws HeaderProcessingException {
        log.trace("Entering getSale method in ReportController with id: {}", id);

        SaleResponse saleResponse = reportService.getSale(id);

        log.info("Returning sale response for id: {}", id);
        log.trace("Exiting getSale method in ReportController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<SaleResponse>> getAllSales() throws HeaderProcessingException {
        log.trace("Entering getAllSales method in ReportController");

        List<SaleResponse> saleResponses = reportService.getAllSales();

        log.info("Returning all sales response with count: {}", saleResponses.size());
        log.trace("Exiting getAllSales method in ReportController");
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<SaleResponse>> getDeletedSales() throws HeaderProcessingException {
        log.trace("Entering getDeletedSales method in ReportController");

        List<SaleResponse> deletedSaleResponses = reportService.getDeletedSales();

        log.info("Returning deleted sales response with count: {}", deletedSaleResponses.size());
        log.trace("Exiting getDeletedSales method in ReportController");
        return new ResponseEntity<>(deletedSaleResponses, HttpStatus.OK);
    }

    @GetMapping("/filteredAndSorted")
    public ResponseEntity<Page<SaleResponse>> getAllSalesFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cashier,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws HeaderProcessingException {
        log.trace("Entering getAllSalesFilteredAndSorted method in ReportController with parameters: page={}, size={}, sortBy={}, direction={}, cashier={}, paymentMethod={}, minTotal={}, maxTotal={}, startDate={}, endDate={}",
                page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        Page<SaleResponse> saleResponses = reportService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        log.info("Returning filtered and sorted sales response with page size: {}", saleResponses.getSize());
        log.trace("Exiting getAllSalesFilteredAndSorted method in ReportController");
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<byte[]> getReceipt(@PathVariable("id") Long id) throws HeaderProcessingException, ReceiptGenerationException {
        log.trace("Entering getReceipt method in ReportController with id: {}", id);

        byte[] pdfBytes = reportService.getReceipt(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "receipt" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        log.info("Returning receipt PDF for id: {}", id);
        log.trace("Exiting getReceipt method in ReportController with id: {}", id);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.CREATED);
    }
}
