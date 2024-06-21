package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class CacheService {

    @Cacheable(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse createProductCache(ProductResponse productResponse) {
        log.trace("Entering createProductCache method in CacheService class");
        log.debug("Creating cache for product with ID: {}", productResponse.getId());

        log.trace("Exiting createProductCache method in CacheService class");
        return productResponse;
    }

    @CachePut(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse updateProductCache(ProductResponse productResponse) {
        log.trace("Entering updateProductCache method in CacheService class");
        log.debug("Updating cache for product with ID: {}", productResponse.getId());

        log.trace("Exiting updateProductCache method in CacheService class");
        return productResponse;
    }
}