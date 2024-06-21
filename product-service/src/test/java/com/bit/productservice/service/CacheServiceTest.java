package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("product_id");
        cacheService = new CacheService();
    }

    @Test
    void givenNullProductResponse_whenCreateProductCache_thenNoCachingOccurs() {
        // Act
        ProductResponse result = cacheService.createProductCache(null);

        // Assert
        assertNull(result);

        Cache cache = cacheManager.getCache("product_id");
        assertNotNull(cache);
        Cache.ValueWrapper valueWrapper = cache.get(1L);
        assertNull(valueWrapper);
    }

    @Test
    void givenNullProductResponse_whenUpdateProductCache_thenNoCachingOccurs() {
        // Act
        ProductResponse result = cacheService.updateProductCache(null);

        // Assert
        assertNull(result);

        Cache cache = cacheManager.getCache("product_id");
        assertNotNull(cache);
        Cache.ValueWrapper valueWrapper = cache.get(1L);
        assertNull(valueWrapper);
    }
}
