package com.bit.productservice.controller;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.service.ProductService;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for handling product-related operations.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id The unique identifier of the product.
     * @return A ResponseEntity containing the product response and a status code of OK (200).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long id) {
        log.trace("Entering getProduct method in ProductController class");

        ProductResponse productResponse = productService.getProduct(id);
        log.info("Returning product response: {}", productResponse);

        log.trace("Exiting getProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    /**
     * Retrieves all products from the system.
     *
     * @return A ResponseEntity containing a list of product responses and a status code of OK (200).
     *         The list of product responses is wrapped in the ResponseEntity body.
     */
    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.trace("Entering getAllProducts method in ProductController class");

        List<ProductResponse> productResponses = productService.getAllProducts();
        log.info("Returning {} product responses", productResponses.size());

        log.trace("Exiting getAllProducts method in ProductController class");
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    /**
     * Retrieves all soft deleted products from the system.
     *
     * @return A ResponseEntity containing a list of soft deleted product responses and a status code of OK (200).
     *         The list of product responses is wrapped in the ResponseEntity body.
     */
    @GetMapping("/deleted")
    public ResponseEntity<List<ProductResponse>> getDeletedProducts() {
        log.trace("Entering getDeletedProducts method in ProductController class");

        List<ProductResponse> deletedProductResponses = productService.getDeletedProducts();
        log.info("Returning {} deleted product responses", deletedProductResponses.size());

        log.trace("Exiting getDeletedProducts method in ProductController class");
        return new ResponseEntity<>(deletedProductResponses, HttpStatus.OK);
    }

    /**
     * Retrieves all products from the system, applying filtering and sorting.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of products per page (default is 10).
     * @param sortBy The field to sort by (default is "id").
     * @param direction The sorting direction (default is "ASC").
     * @param name The product name to filter by (optional).
     * @param description The product description to filter by (optional).
     * @param minPrice The minimum product price to filter by (optional).
     * @param maxPrice The maximum product price to filter by (optional).
     * @param minStock The minimum product stock to filter by (optional).
     * @param maxStock The maximum product stock to filter by (optional).
     * @return A ResponseEntity containing a page of product responses and a status code of OK (200).
     *         The page of product responses is wrapped in the ResponseEntity body.
     */
    @GetMapping("/filteredAndSorted")
    public ResponseEntity<Page<ProductResponse>> getAllProductsFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock
    ) {
        log.trace("Entering getAllProductsFilteredAndSorted method in ProductController class");

        Page<ProductResponse> productResponses = productService.getAllProductsFilteredAndSorted(page, size, sortBy, direction, name, description, minPrice, maxPrice, minStock, maxStock);
        log.info("Returning {} filtered and sorted product responses", productResponses.getTotalElements());

        log.trace("Exiting getAllProductsFilteredAndSorted method in ProductController class");
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    /**
     * Processes a product return request.
     *
     * @param request The product return request containing the list of products to return and their respective quantities.
     * @return A ResponseEntity with a status code of OK (200) and a message indicating that the product return request was processed successfully.
     */
    @PostMapping("/return")
    public ResponseEntity<String> returnProducts(@Valid @RequestBody ProductStockReturnRequest request) {
        log.trace("Entering returnProducts method in ProductController class");

        productService.returnProducts(request);
        log.info("Product return request processed successfully for request: {}", request);

        log.trace("Exiting returnProducts method in ProductController class");
        return ResponseEntity.ok("Product return request processed successfully.");
    }

    /**
     * Creates a new product in the system.
     *
     * @param productRequest The request object containing the details of the product to be created.
     * @return A ResponseEntity containing the created product's response and a status code of CREATED (201).
     * @throws AlgorithmNotFoundException If the algorithm specified in the product request is not found.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering createProduct method in ProductController class");

        ProductResponse productResponse = productService.createProduct(productRequest);
        log.info("Returning product response's ID: {}", productResponse.getId());

        log.trace("Exiting createProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }

    /**
     * Updates an existing product in the system.
     *
     * @param id The unique identifier of the product to be updated.
     * @param productRequest The request object containing the updated details of the product.
     * @return A ResponseEntity containing the updated product's response and a status code of OK (200).
     * @throws AlgorithmNotFoundException If the algorithm specified in the product request is not found.
     */
    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @RequestBody @Valid ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering updateProduct method in ProductController class");

        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        log.info("Returning product response with ID {}: {}", id, productResponse);

        log.trace("Exiting updateProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    /**
     * Restores a soft deleted product in the system.
     *
     * @param id The unique identifier of the product to be restored.
     * @return A ResponseEntity containing the restored product's response and a status code of OK (200).
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long id) {
        log.trace("Entering restoreProduct method in ProductController class");

        ProductResponse productResponse = productService.restoreProduct(id);
        log.info("Returning restored product response with ID {}: {}", id, productResponse);

        log.trace("Exiting restoreProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    /**
     * Deletes a product from the system by marking it as soft deleted.
     *
     * @param id The unique identifier of the product to be deleted.
     * @return A ResponseEntity containing a success message and a status code of OK (200).
     *         The success message is wrapped in the ResponseEntity body.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        log.trace("Entering deleteProduct method in ProductController class");

        productService.deleteProduct(id);
        log.info("Product with ID {} deleted successfully", id);

        log.trace("Exiting deleteProduct method in ProductController class");
        return new ResponseEntity<>("Product soft deleted successfully!", HttpStatus.OK);
    }

    /**
     * Deletes a product from the system permanently.
     * This method marks the product as deleted in the database and removes all related data.
     *
     * @param id The unique identifier of the product to be deleted permanently.
     * @return A ResponseEntity containing a success message and a status code of OK (200).
     *         The success message is wrapped in the ResponseEntity body.
     */
    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteProductPermanently(@PathVariable Long id) {
        log.trace("Entering deleteProductPermanently method in ProductController class");

        productService.deleteProductPermanently(id);
        log.info("Product with ID {} permanently deleted successfully", id);

        log.trace("Exiting deleteProductPermanently method in ProductController class");
        return new ResponseEntity<>("Product deleted permanently!", HttpStatus.OK);
    }
}