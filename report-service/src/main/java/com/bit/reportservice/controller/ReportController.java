package com.bit.reportservice.controller;

import com.bit.reportservice.ReportServiceApplication;
import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final Logger logger = LogManager.getLogger(ReportServiceApplication.class);
    private final ReportService reportService;

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable("id") Long id) throws HeaderProcessingException {
        logger.info("Received request to fetch sale with ID: {}", id);
        SaleResponse saleResponse = reportService.getSale(id);

        logger.info("Returning sale response: {}", saleResponse);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<SaleResponse>> getAllSales() throws HeaderProcessingException {
        logger.info("Received request to fetch all sales");
        List<SaleResponse> saleResponses = reportService.getAllSales();

        logger.info("Returning {} sale responses", saleResponses.size());
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<SaleResponse>> getDeletedSales() throws HeaderProcessingException {
        logger.info("Received request to fetch all deleted sales");
        List<SaleResponse> deletedSaleResponses = reportService.getDeletedSales();

        logger.info("Returning {} deleted sale responses", deletedSaleResponses.size());
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
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) throws HeaderProcessingException {
        logger.info("Received request to fetch all sales with filters and sorting: page={}, size={}, sortBy={}, direction={}, cashier={}, paymentMethod={}, minPrice={}, maxPrice={}, startDate={}, endDate={}",
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
        Page<SaleResponse> saleResponses = reportService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        logger.info("Returning {} sale responses filtered and sorted", saleResponses.getTotalElements());
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<byte[]> getReceipt(@PathVariable("id") Long id) throws HeaderProcessingException {
        logger.info("Received request to generate receipt for sale with ID: {}", id);
        byte[] pdfBytes = reportService.getReceipt(id);

        logger.info("Receipt ready!");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "receipt" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.CREATED);
    }
}
