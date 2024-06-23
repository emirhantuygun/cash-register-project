package com.bit.productservice.service;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * This interface defines the contract for the Product Service.
 * It provides methods for CRUD operations, filtering, sorting, and product stock management.
 *
 * @author Emirhan Tuygun
 */
public interface ProductService {

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id The unique identifier of the product.
     * @return The product response object containing the product details.
     */
    ProductResponse getProduct (Long id);

    /**
     * Retrieves all active products.
     *
     * @return A list of product response objects containing the product details.
     */
    List<ProductResponse> getAllProducts();

    /**
     * Retrieves all deleted products.
     *
     * @return A list of product response objects containing the deleted product details.
     */
    List<ProductResponse> getDeletedProducts();

    /**
     * Retrieves a paginated list of products based on filtering and sorting criteria.
     *
     * @param page The page number (0-indexed).
     * @param size The number of products per page.
     * @param sortBy The field to sort by.
     * @param direction The sorting direction (asc or desc).
     * @param name The product name filter.
     * @param description The product description filter.
     * @param minPrice The minimum price filter.
     * @param maxPrice The maximum price filter.
     * @param minStock The minimum stock filter.
     * @param maxStock The maximum stock filter.
     * @return A page of product response objects containing the filtered and sorted product details.
     */
    Page<ProductResponse> getAllProductsFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                          String name, String description, BigDecimal minPrice,
                                                          BigDecimal maxPrice, Integer minStock, Integer maxStock);

    /**
     * Creates a new product.
     *
     * @param productRequest The product request object containing the product details.
     * @return The product response object containing the created product details.
     * @throws AlgorithmNotFoundException If the algorithm specified in the request is not found.
     */
    ProductResponse createProduct(ProductRequest productRequest) throws AlgorithmNotFoundException;

    /**
     * Updates an existing product.
     *
     * @param id The unique identifier of the product to update.
     * @param updatedProduct The updated product request object containing the new product details.
     * @return The product response object containing the updated product details.
     * @throws AlgorithmNotFoundException If the algorithm specified in the request is not found.
     */
    ProductResponse updateProduct (Long id, ProductRequest updatedProduct) throws AlgorithmNotFoundException;

    /**
     * Deletes a product by marking it as deleted.
     *
     * @param id The unique identifier of the product to delete.
     */
    void deleteProduct (Long id);

    /**
     * Restores a deleted product.
     *
     * @param id The unique identifier of the deleted product to restore.
     * @return The product response object containing the restored product details.
     */
    ProductResponse restoreProduct(Long id);

    /**
     * Permanently deletes a product.
     *
     * @param id The unique identifier of the product to delete permanently.
     */
    void deleteProductPermanently(Long id);

    /**
     * Returns products to the stock based on the provided request.
     *
     * @param request The product stock return request object containing the product details and return quantity.
     */
    void returnProducts(ProductStockReturnRequest request);
}
