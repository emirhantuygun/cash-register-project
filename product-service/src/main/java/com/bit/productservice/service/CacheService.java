package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for managing product cache operations.
 * It uses Spring's caching annotations to cache product data.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
public class CacheService {

    /**
     * Creates a cache entry for a product.
     *
     * @param productResponse The product data to be cached.
     * @return The same product data that was passed as a parameter.
     */
    @Cacheable(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse createProductCache(ProductResponse productResponse) {
        log.trace("Entering createProductCache method in CacheService class");
        log.debug("Creating cache for product with ID: {}", productResponse.getId());

        log.trace("Exiting createProductCache method in CacheService class");
        return productResponse;
    }

    /**
     * Updates the cache entry for a product.
     *
     * @param productResponse The updated product data to be cached.
     * @return The same product data that was passed as a parameter.
     */
    @CachePut(cacheNames = "product_id", key = "#productResponse.id", unless = "#result == null")
    public ProductResponse updateProductCache(ProductResponse productResponse) {
        log.trace("Entering updateProductCache method in CacheService class");
        log.debug("Updating cache for product with ID: {}", productResponse.getId());

        log.trace("Exiting updateProductCache method in CacheService class");
        return productResponse;
    }
}