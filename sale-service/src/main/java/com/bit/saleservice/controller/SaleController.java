package com.bit.saleservice.controller;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.service.SaleService;
import com.bit.saleservice.wrapper.PageWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable("id") Long id) {
        log.trace("Entering getSale method in SaleController with id: {}", id);

        SaleResponse saleResponse = saleService.getSale(id);
        log.info("Successfully retrieved sale with id: {}", id);

        log.trace("Exiting getSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        log.trace("Entering getAllSales method in SaleController");

        List<SaleResponse> saleResponses = saleService.getAllSales();
        log.info("Successfully retrieved all sales");

        log.trace("Exiting getAllSales method in SaleController");
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<SaleResponse>> getDeletedSales() {
        log.trace("Entering getDeletedSales method in SaleController");

        List<SaleResponse> deletedSaleResponses = saleService.getDeletedSales();
        log.info("Successfully retrieved deleted sales");

        log.trace("Exiting getDeletedSales method in SaleController");
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
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        log.trace("Entering getAllSalesFilteredAndSorted method in SaleController with page: {}, size: {}, sortBy: {}, direction: {}, cashier: {}, paymentMethod: {}, minTotal: {}, maxTotal: {}, startDate: {}, endDate: {}",
                page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);

        PageWrapper<SaleResponse> saleResponsePageWrapper = saleService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minTotal, maxTotal, startDate, endDate);
        log.info("Successfully retrieved filtered and sorted sales");

        log.trace("Exiting getAllSalesFilteredAndSorted method in SaleController");
        return new ResponseEntity<>(saleResponsePageWrapper, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering createSale method in SaleController with saleRequest: {}", saleRequest);

        SaleResponse saleResponse = saleService.createSale(saleRequest);
        log.info("Successfully created sale");

        log.trace("Exiting createSale method in SaleController");
        return new ResponseEntity<>(saleResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable Long id, @RequestBody @Valid SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering updateSale method in SaleController with id: {} and saleRequest: {}", id, saleRequest);

        SaleResponse saleResponse = saleService.updateSale(id, saleRequest);
        log.info("Successfully updated sale with id: {}", id);

        log.trace("Exiting updateSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<String> cancelSale(@PathVariable Long id) {
        log.trace("Entering cancelSale method in SaleController with id: {}", id);

        saleService.cancelSale(id);
        log.info("Successfully cancelled sale with id: {}", id);

        log.trace("Exiting cancelSale method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale cancelled successfully!", HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<SaleResponse> restoreSale(@PathVariable Long id) {
        log.trace("Entering restoreSale method in SaleController with id: {}", id);

        SaleResponse saleResponse = saleService.restoreSale(id);
        log.info("Successfully restored sale with id: {}", id);

        log.trace("Exiting restoreSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSale(@PathVariable Long id) {
        log.trace("Entering deleteSale method in SaleController with id: {}", id);

        saleService.deleteSale(id);
        log.info("Successfully soft deleted sale with id: {}", id);

        log.trace("Exiting deleteSale method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteSalePermanently(@PathVariable Long id) {
        log.trace("Entering deleteSalePermanently method in SaleController with id: {}", id);

        saleService.deleteSalePermanently(id);
        log.info("Successfully permanently deleted sale with id: {}", id);

        log.trace("Exiting deleteSalePermanently method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale deleted permanently!", HttpStatus.OK);
    }
}
