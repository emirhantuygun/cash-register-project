package com.bit.saleservice.controller;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.service.SaleService;
import com.bit.saleservice.wrapper.PageWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SaleController {

    private final Logger logger = LogManager.getLogger(SaleServiceApplication.class);
    private final SaleService saleService;

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable("id") Long id) {
        logger.info("Received request to fetch sale with ID: {}", id);
        SaleResponse saleResponse = saleService.getSale(id);

        logger.info("Returning sale response: {}", saleResponse);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        logger.info("Received request to fetch all sales");
        List<SaleResponse> saleResponses = saleService.getAllSales();

        logger.info("Returning {} sale responses", saleResponses.size());
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<SaleResponse>> getDeletedSales() {
        logger.info("Received request to fetch all deleted sales");
        List<SaleResponse> deletedSaleResponses = saleService.getDeletedSales();

        logger.info("Returning {} deleted sale responses", deletedSaleResponses.size());
        return new ResponseEntity<>(deletedSaleResponses, HttpStatus.OK);
    }

    @GetMapping("/filteredAndSorted")
    public ResponseEntity<PageWrapper<SaleResponse>> getAllSalesFilteredAndSorted(
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
    ) {
        logger.info("Received request to fetch all sales with filters and sorting: page={}, size={}, sortBy={}, direction={}, cashier={}, paymentMethod={}, minPrice={}, maxPrice={}, startDate={}, endDate={}",
                page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
        Page<SaleResponse> saleResponses = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);

        logger.info("Returning {} sale responses filtered and sorted", saleResponses.getTotalElements());
        PageWrapper<SaleResponse> response = new PageWrapper<>();
        response.setContent(saleResponses.getContent());
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(saleResponses.getTotalElements());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest saleRequest){
        logger.info("Received request to create sale: {}", saleRequest);
         SaleResponse saleResponse = saleService.createSale(saleRequest);

        logger.info("Returning sale response's ID: {}", saleResponse.getId());
        return new ResponseEntity<>(saleResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable Long id,
                                                         @RequestBody @Valid SaleRequest saleRequest){
        logger.info("Received request to update sale with ID {}: {}", id, saleRequest);
        SaleResponse saleResponse = saleService.updateSale(id, saleRequest);

        logger.info("Returning sale response with ID {}: {}", id, saleResponse);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<String> cancelSale(@PathVariable Long id){
        saleService.cancelSale(id);

        return new ResponseEntity<>("Sale cancelled successfully!", HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<SaleResponse> restoreSale(@PathVariable Long id){
        SaleResponse saleResponse = saleService.restoreSale(id);

        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSale(@PathVariable Long id){
        logger.info("Received request to delete sale with ID: {}", id);
        saleService.deleteSale(id);

        logger.info("Sale with ID {} soft deleted successfully", id);
        return new ResponseEntity<>("Sale soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteSalePermanently(@PathVariable Long id){
        saleService.deleteSalePermanently(id);

        return new ResponseEntity<>("Sale deleted permanently!", HttpStatus.OK);
    }

}
