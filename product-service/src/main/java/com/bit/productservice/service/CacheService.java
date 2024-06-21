package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Cacheable(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse createProductCache(ProductResponse productResponse) {
        return productResponse;
    }

    @CachePut(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse updateProductCache(ProductResponse productResponse) {
        return productResponse;
    }
}