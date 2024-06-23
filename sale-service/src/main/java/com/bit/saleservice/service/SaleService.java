package com.bit.saleservice.service;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.wrapper.PageWrapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * This interface defines the contract for the Sale Service.
 * It provides methods for creating, retrieving, updating, canceling, restoring, and deleting sales.
 * It also provides a method for filtering and sorting sales.
 *
 * @author Emirhan Tuygun
 */
public interface SaleService {

    /**
     * Creates a new sale.
     *
     * @param saleRequest The request object containing the sale details.
     * @return The response object containing the created sale details.
     * @throws HeaderProcessingException If there is an issue with processing the request headers.
     */
    SaleResponse createSale(SaleRequest saleRequest) throws HeaderProcessingException;

    /**
     * Retrieves a sale by its ID.
     *
     * @param id The ID of the sale to retrieve.
     * @return The response object containing the retrieved sale details.
     */
    SaleResponse getSale(Long id);

    /**
     * Retrieves all active sales.
     *
     * @return A list of response objects containing the details of all active sales.
     */
    List<SaleResponse> getAllSales();

    /**
     * Retrieves all deleted sales.
     *
     * @return A list of response objects containing the details of all deleted sales.
     */
    List<SaleResponse> getDeletedSales();

    /**
     * Retrieves all sales, filtered and sorted based on the provided parameters.
     *
     * @param page The page number for pagination.
     * @param size The number of records per page.
     * @param sortBy The field to sort by.
     * @param direction The sorting direction (asc or desc).
     * @param cashier The cashier's name to filter by.
     * @param paymentMethod The payment method to filter by.
     * @param minTotal The minimum total amount to filter by.
     * @param maxTotal The maximum total amount to filter by.
     * @param startDate The start date to filter by.
     * @param endDate The end date to filter by.
     * @return A PageWrapper object containing the filtered and sorted sales.
     */
    PageWrapper<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                           String cashier, String paymentMethod,
                                                           BigDecimal minTotal, BigDecimal maxTotal,
                                                           String startDate, String endDate);

    /**
     * Updates an existing sale.
     *
     * @param id The ID of the sale to update.
     * @param saleRequest The request object containing the updated sale details.
     * @return The response object containing the updated sale details.
     * @throws HeaderProcessingException If there is an issue with processing the request headers.
     */
    SaleResponse updateSale(Long id, SaleRequest saleRequest) throws HeaderProcessingException;

    /**
     * Cancels an existing sale.
     *
     * @param id The ID of the sale to cancel.
     */
    void cancelSale(Long id);

    /**
     * Restores a deleted sale.
     *
     * @param id The ID of the sale to restore.
     * @return The response object containing the restored sale details.
     */
    SaleResponse restoreSale(Long id);

    /**
     * Deletes a sale (soft delete).
     *
     * @param id The ID of the sale to delete.
     */
    void deleteSale(Long id);

    /**
     * Permanently deletes a sale.
     *
     * @param id The ID of the sale to delete permanently.
     */
    void deleteSalePermanently(Long id);
}
