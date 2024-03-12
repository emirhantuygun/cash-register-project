package com.bit.productservice.controller;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") Long id) {
        ProductResponse productResponse = productService.getProduct(id);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return new ResponseEntity<> (productService.getAllProducts(), HttpStatus.OK);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<ProductResponse>> getDeletedProducts() {
        return new ResponseEntity<> (productService.getDeletedProducts(), HttpStatus.OK);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductResponse>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ProductResponse> productResponses = productService.getAllProductsPaginated(page, size);
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<ProductResponse>> getAllProductsSorted(
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        List<ProductResponse> productResponses = productService.getAllProductsSorted(sortBy, Sort.Direction.valueOf(direction.toUpperCase()));
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/paginatedAndSorted")
    public ResponseEntity<Page<ProductResponse>> getAllProductsPaginatedAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        Page<ProductResponse> productResponses = productService.getAllProductsPaginatedAndSorted(page, size, sortBy, Sort.Direction.valueOf(direction.toUpperCase()));
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @GetMapping("/filtered")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        Page<ProductResponse> productResponses = productService.getAllProductsFiltered(name, description, minPrice, maxPrice, pageable);
        return new ResponseEntity<>(productResponses, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest){
        ProductResponse productResponse = productService.createProduct(productRequest);
        return new ResponseEntity<>(productResponse, HttpStatus.CREATED);
    }


    @PutMapping("{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                @RequestBody @Valid ProductRequest updatedProduct){

        ProductResponse productResponse = productService.updateProduct(id, updatedProduct);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
         productService.deleteProduct(id);
         return new ResponseEntity<>("Product deleted successfully!", HttpStatus.OK);

    }
}