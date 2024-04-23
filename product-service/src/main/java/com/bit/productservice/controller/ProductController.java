package com.bit.productservice.controller;

import com.bit.productservice.ProductServiceApplication;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final Logger logger = LogManager.getLogger(ProductServiceApplication.class);

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long id) {
        logger.info("Received request to fetch product with ID: {}", id);
        ProductResponse productResponse = productService.getProduct(id);

        logger.info("Returning product response: {}", productResponse);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        logger.info("Received request to fetch all products");
        List<ProductResponse> productResponses = productService.getAllProducts();

        logger.info("Returning {} product responses", productResponses.size());
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<ProductResponse>> getDeletedProducts() {
        logger.info("Received request to fetch all deleted products");
        List<ProductResponse> deletedProductResponses = productService.getDeletedProducts();

        logger.info("Returning {} deleted product responses", deletedProductResponses.size());
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
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        logger.info("Received request to fetch all products with filters and sorting: page={}, size={}, sortBy={}, direction={}, name={}, description={}, minPrice={}, maxPrice={}",
                page, size, sortBy, direction, name, description, minPrice, maxPrice);
        Page<ProductResponse> productResponses = productService.getAllProductsFilteredAndSorted(page, size, sortBy, direction, name, description, minPrice, maxPrice);

        logger.info("Returning {} product responses filtered and sorted", productResponses.getTotalElements());
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest){
        logger.info("Received request to create product: {}", productRequest);
        ProductResponse productResponse = productService.createProduct(productRequest);

        logger.info("Returning product response's ID: {}", productResponse.getId());
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                @RequestBody @Valid ProductRequest productRequest){
        logger.info("Received request to update product with ID {}: {}", id, productRequest);
        ProductResponse productResponse = productService.updateProduct(id, productRequest);

        logger.info("Returning product response with ID {}: {}", id, productResponse);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long id){
        ProductResponse productResponse = productService.restoreProduct(id);

        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        logger.info("Received request to delete product with ID: {}", id);
        productService.deleteProduct(id);

        logger.info("Product with ID {} deleted successfully", id);
        return new ResponseEntity<>("Product soft deleted successfully!", HttpStatus.OK);
    }

    @DeleteMapping("/permanent/{id}")
    public ResponseEntity<String> deleteProductPermanently(@PathVariable Long id){
        productService.deleteProductPermanently(id);

        return new ResponseEntity<>("Product deleted permanently!", HttpStatus.OK);
    }
}