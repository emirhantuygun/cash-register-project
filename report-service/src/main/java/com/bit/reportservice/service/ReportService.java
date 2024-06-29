package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.ReceiptGenerationException;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * This interface defines the contract for the Report Service.
 * It provides methods for retrieving sales data and generating receipts.
 *
 * @author Emirhan Tuygun
 */
public interface ReportService {

    /**
     * Retrieves a sale by its unique identifier.
     *
     * @param id The unique identifier of the sale.
     * @return The SaleResponse object representing the sale.
     * @throws HeaderProcessingException If there is an error processing the header.
     */
    SaleResponse getSale(Long id) throws HeaderProcessingException;

    /**
     * Retrieves all sales.
     *
     * @return A list of SaleResponse objects representing all sales.
     * @throws HeaderProcessingException If there is an error processing the header.
     */
    List<SaleResponse> getAllSales() throws HeaderProcessingException;

    /**
     * Retrieves all deleted sales.
     *
     * @return A list of SaleResponse objects representing all deleted sales.
     * @throws HeaderProcessingException If there is an error processing the header.
     */
    List<SaleResponse> getDeletedSales() throws HeaderProcessingException;

    /**
     * Retrieves sales based on filtering and sorting criteria.
     *
     * @param page The page number for pagination.
     * @param size The number of records per page.
     * @param sortBy The field to sort by.
     * @param direction The sorting direction (asc or desc).
     * @param cashier The cashier's name for filtering.
     * @param paymentMethod The payment method for filtering.
     * @param minTotal The minimum total amount for filtering.
     * @param maxTotal The maximum total amount for filtering.
     * @param startDate The start date for filtering.
     * @param endDate The end date for filtering.
     * @return A Page object containing SaleResponse objects representing the filtered and sorted sales.
     * @throws HeaderProcessingException If there is an error processing the header.
     */
    Page<SaleResponse> getAllSalesFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                    String cashier, String paymentMethod,
                                                    BigDecimal minTotal, BigDecimal maxTotal,
                                                    String startDate, String endDate) throws HeaderProcessingException;

    /**
     * Generates a receipt for a sale based on its unique identifier.
     *
     * @param id The unique identifier of the sale.
     * @return A byte array representing the receipt.
     * @throws HeaderProcessingException If there is an error processing the header.
     * @throws ReceiptGenerationException If there is an error generating the receipt.
     */
    byte[] getReceipt(Long id) throws HeaderProcessingException, ReceiptGenerationException;

    /**
     * This method generates a chart based on the sales data for a given time unit.
     *
     * @param unit The time unit for which the chart should be generated. It can be "day", "week", "month", or "year".
     * @return A byte array representing the generated chart in PDF format.
     * @throws HeaderProcessingException If there is an error processing the request headers.
     */
    byte[] getChart(String unit) throws HeaderProcessingException, ReceiptGenerationException;
}
