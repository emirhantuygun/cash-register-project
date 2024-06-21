package com.bit.productservice.service;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse getProduct (Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getDeletedProducts();
    Page<ProductResponse> getAllProductsFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                          String name, String description, BigDecimal minPrice,
                                                          BigDecimal maxPrice, Integer minStock, Integer maxStock);
    ProductResponse createProduct(ProductRequest productRequest) throws AlgorithmNotFoundException;
    ProductResponse updateProduct (Long id, ProductRequest updatedProduct) throws AlgorithmNotFoundException;
    void deleteProduct (Long id);
    ProductResponse restoreProduct(Long id);
    void deleteProductPermanently(Long id);
    void returnProducts(ProductStockReturnRequest request);
}
