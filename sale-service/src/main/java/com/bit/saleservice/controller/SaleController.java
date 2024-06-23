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

/**
 * Controller for handling sale-related operations.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    /**
     * Retrieves a sale by its unique identifier.
     *
     * @param id The unique identifier of the sale to retrieve.
     * @return A ResponseEntity containing the retrieved sale and a status code of OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSale(@PathVariable("id") Long id) {
        log.trace("Entering getSale method in SaleController with id: {}", id);

        SaleResponse saleResponse = saleService.getSale(id);
        log.info("Successfully retrieved sale with id: {}", id);

        log.trace("Exiting getSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    /**
     * Retrieves all sales from the system.
     *
     * @return A ResponseEntity containing a list of SaleResponse objects and a status code of OK (200).
     *         The list of SaleResponse objects represents all sales in the system.
     */
    @GetMapping()
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        log.trace("Entering getAllSales method in SaleController");

        List<SaleResponse> saleResponses = saleService.getAllSales();
        log.info("Successfully retrieved all sales");

        log.trace("Exiting getAllSales method in SaleController");
        return new ResponseEntity<>(saleResponses, HttpStatus.OK);
    }

    /**
     * Retrieves all soft deleted sales from the system.
     *
     * @return A ResponseEntity containing a list of SaleResponse objects and a status code of OK (200).
     *         The list of SaleResponse objects represents all soft deleted sales in the system.
     */
    @GetMapping("/deleted")
    public ResponseEntity<List<SaleResponse>> getDeletedSales() {
        log.trace("Entering getDeletedSales method in SaleController");

        List<SaleResponse> deletedSaleResponses = saleService.getDeletedSales();
        log.info("Successfully retrieved deleted sales");

        log.trace("Exiting getDeletedSales method in SaleController");
        return new ResponseEntity<>(deletedSaleResponses, HttpStatus.OK);
    }

    /**
     * Retrieves all sales from the system, applying filtering and sorting.
     *
     * @param page The page number for pagination. Default is 0.
     * @param size The number of sales per page. Default is 10.
     * @param sortBy The field to sort by. Default is 'id'.
     * @param direction The sorting direction. Default is 'ASC'.
     * @param cashier The cashier's name to filter by. Optional.
     * @param paymentMethod The payment method to filter by. Optional.
     * @param minTotal The minimum total amount to filter by. Optional.
     * @param maxTotal The maximum total amount to filter by. Optional.
     * @param startDate The start date to filter by. Optional.
     * @param endDate The end date to filter by. Optional.
     * @return A ResponseEntity containing a PageWrapper of SaleResponse objects and a status code of OK (200).
     *         The PageWrapper represents the filtered and sorted sales in the system.
     */
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

    /**
     * Creates a new sale in the system.
     *
     * @param saleRequest The request object containing the details of the sale to be created.
     * @return A ResponseEntity containing the created SaleResponse object and a status code of CREATED (201).
     */
    @PostMapping()
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering createSale method in SaleController with saleRequest: {}", saleRequest);

        SaleResponse saleResponse = saleService.createSale(saleRequest);
        log.info("Successfully created sale");

        log.trace("Exiting createSale method in SaleController");
        return new ResponseEntity<>(saleResponse, HttpStatus.CREATED);
    }

    /**
     * Updates an existing sale in the system.
     *
     * @param id The unique identifier of the sale to update.
     * @param saleRequest The request object containing the updated details of the sale.
     * @return A ResponseEntity containing the updated SaleResponse object and a status code of OK (200).
     * @throws HeaderProcessingException If there is an error processing the request headers.
     */
    @PutMapping("{id}")
    public ResponseEntity<SaleResponse> updateSale(@PathVariable Long id, @RequestBody @Valid SaleRequest saleRequest) throws HeaderProcessingException {
        log.trace("Entering updateSale method in SaleController with id: {} and saleRequest: {}", id, saleRequest);

        SaleResponse saleResponse = saleService.updateSale(id, saleRequest);
        log.info("Successfully updated sale with id: {}", id);

        log.trace("Exiting updateSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    /**
     * Cancels an existing sale in the system.
     *
     * @param id The unique identifier of the sale to cancel.
     * @return A ResponseEntity containing a success message and a status code of OK (200).
     *         The success message indicates that the sale has been cancelled successfully.
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<String> cancelSale(@PathVariable Long id) {
        log.trace("Entering cancelSale method in SaleController with id: {}", id);

        saleService.cancelSale(id);
        log.info("Successfully cancelled sale with id: {}", id);

        log.trace("Exiting cancelSale method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale cancelled successfully!", HttpStatus.OK);
    }

    /**
     * Restores a soft deleted sale in the system.
     *
     * @param id The unique identifier of the sale to restore.
     * @return A ResponseEntity containing the restored SaleResponse object and a status code of OK (200).
     *         The SaleResponse object represents the restored sale.
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<SaleResponse> restoreSale(@PathVariable Long id) {
        log.trace("Entering restoreSale method in SaleController with id: {}", id);

        SaleResponse saleResponse = saleService.restoreSale(id);
        log.info("Successfully restored sale with id: {}", id);

        log.trace("Exiting restoreSale method in SaleController with id: {}", id);
        return new ResponseEntity<>(saleResponse, HttpStatus.OK);
    }

    /**
     * Deletes a sale from the system by marking it as soft deleted.
     *
     * @param id The unique identifier of the sale to delete.
     * @return A ResponseEntity containing a success message and a status code of OK (200).
     *         The success message indicates that the sale has been soft deleted successfully.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSale(@PathVariable Long id) {
        log.trace("Entering deleteSale method in SaleController with id: {}", id);

        saleService.deleteSale(id);
        log.info("Successfully soft deleted sale with id: {}", id);

        log.trace("Exiting deleteSale method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale soft deleted successfully!", HttpStatus.OK);
    }

    /**
     * Deletes a sale from the system permanently.
     * This method marks the sale as deleted in the database and removes it from the system.
     *
     * @param id The unique identifier of the sale to delete permanently.
     * @return A ResponseEntity containing a success message and a status code of OK (200).
     *         The success message indicates that the sale has been permanently deleted successfully.
     */
    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteSalePermanently(@PathVariable Long id) {
        log.trace("Entering deleteSalePermanently method in SaleController with id: {}", id);

        saleService.deleteSalePermanently(id);
        log.info("Successfully permanently deleted sale with id: {}", id);

        log.trace("Exiting deleteSalePermanently method in SaleController with id: {}", id);
        return new ResponseEntity<>("Sale deleted permanently!", HttpStatus.OK);
    }
}
