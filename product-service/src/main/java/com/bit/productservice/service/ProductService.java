package com.bit.productservice.service;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse getProduct (Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getDeletedProducts();
    Page<ProductResponse> getAllProductsPaginated(int page, int size);
    List<ProductResponse> getAllProductsSorted(String sortBy, Sort.Direction direction);
    Page<ProductResponse> getAllProductsPaginatedAndSorted(int page, int size, String sortBy, Sort.Direction direction);
    Page<ProductResponse> getAllProductsFiltered(String name, String description, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);


    ProductResponse createProduct(ProductRequest productRequest);

    ProductResponse updateProduct (Long id, ProductRequest updatedProduct);

    void deleteProduct (Long id);

}
