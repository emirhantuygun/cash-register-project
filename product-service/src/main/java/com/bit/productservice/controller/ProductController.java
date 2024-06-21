package com.bit.productservice.controller;

import com.bit.productservice.ProductServiceApplication;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.service.ProductService;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long id) {
        log.trace("Entering getProduct method in ProductController class");
        log.info("Received request to fetch product with ID: {}", id);

        ProductResponse productResponse = productService.getProduct(id);

        log.info("Returning product response: {}", productResponse);
        log.trace("Exiting getProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.trace("Entering getAllProducts method in ProductController class");
        log.info("Received request to fetch all products");

        List<ProductResponse> productResponses = productService.getAllProducts();

        log.info("Returning {} product responses", productResponses.size());
        log.trace("Exiting getAllProducts method in ProductController class");
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<ProductResponse>> getDeletedProducts() {
        log.trace("Entering getDeletedProducts method in ProductController class");
        log.info("Received request to fetch all deleted products");

        List<ProductResponse> deletedProductResponses = productService.getDeletedProducts();

        log.info("Returning {} deleted product responses", deletedProductResponses.size());
        log.trace("Exiting getDeletedProducts method in ProductController class");
        return new ResponseEntity<>(deletedProductResponses, HttpStatus.OK);
    }

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

    @PostMapping("/return")
    public ResponseEntity<String> returnProducts(@Valid @RequestBody ProductStockReturnRequest request) {
        log.trace("Entering returnProducts method in ProductController class");

        productService.returnProducts(request);

        log.info("Product return request processed successfully for request: {}", request);
        log.trace("Exiting returnProducts method in ProductController class");
        return ResponseEntity.ok("Product return request processed successfully.");
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering createProduct method in ProductController class");
        log.info("Received request to create product: {}", productRequest);

        ProductResponse productResponse = productService.createProduct(productRequest);

        log.info("Returning product response's ID: {}", productResponse.getId());
        log.trace("Exiting createProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @RequestBody @Valid ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering updateProduct method in ProductController class");
        log.info("Received request to update product with ID {}: {}", id, productRequest);

        ProductResponse productResponse = productService.updateProduct(id, productRequest);

        log.info("Returning product response with ID {}: {}", id, productResponse);
        log.trace("Exiting updateProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long id) {
        log.trace("Entering restoreProduct method in ProductController class");

        ProductResponse productResponse = productService.restoreProduct(id);

        log.info("Returning restored product response with ID {}: {}", id, productResponse);
        log.trace("Exiting restoreProduct method in ProductController class");
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        log.trace("Entering deleteProduct method in ProductController class");
        log.info("Received request to delete product with ID: {}", id);

        productService.deleteProduct(id);

        log.info("Product with ID {} deleted successfully", id);
        log.trace("Exiting deleteProduct method in ProductController class");
        return new ResponseEntity<>("Product soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteProductPermanently(@PathVariable Long id) {
        log.trace("Entering deleteProductPermanently method in ProductController class");

        productService.deleteProductPermanently(id);

        log.info("Product with ID {} permanently deleted successfully", id);
        log.trace("Exiting deleteProductPermanently method in ProductController class");
        return new ResponseEntity<>("Product deleted permanently!", HttpStatus.OK);
    }
}