package com.bit.productservice.service;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse getProduct (Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getDeletedProducts();
    Page<ProductResponse> getAllProductsFilteredAndSorted(Pageable pageable, String name, String description, BigDecimal minPrice, BigDecimal maxPrice);
    ProductResponse createProduct(ProductRequest productRequest);
    ProductResponse updateProduct (Long id, ProductRequest updatedProduct);
    void deleteProduct (Long id);

}
