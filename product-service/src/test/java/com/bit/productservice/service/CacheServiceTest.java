package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;

    @Mock
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("product_id");
    }

    @Test
    void givenValidProductResponse_whenCreateProductCache_thenProductResponseIsCached() {
        // Arrange
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        // Act
        ProductResponse result = cacheService.createProductCache(productResponse);

        // Assert
        assertNotNull(result);
        assertEquals(productResponse, result);
        assertTrue(cacheManager.getCache("product_id").get(1L).get() instanceof ProductResponse);
    }

    @Test
    void givenNullProductResponse_whenCreateProductCache_thenNoCachingOccurs() {
        // Act
        ProductResponse result = cacheService.createProductCache(null);

        // Assert
        assertNull(result);
        assertNull(cacheManager.getCache("product_id").get(1L));
    }

    @Test
    void givenValidProductResponse_whenUpdateProductCache_thenProductResponseIsUpdatedInCache() {
        // Arrange
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        // Act
        ProductResponse result = cacheService.updateProductCache(productResponse);

        // Assert
        assertNotNull(result);
        assertEquals(productResponse, result);
        assertTrue(cacheManager.getCache("product_id").get(1L).get() instanceof ProductResponse);
    }

    @Test
    void givenNullProductResponse_whenUpdateProductCache_thenNoCachingOccurs() {
        // Act
        ProductResponse result = cacheService.updateProductCache(null);

        // Assert
        assertNull(result);
        assertNull(cacheManager.getCache("product_id").get(1L));
    }
}
