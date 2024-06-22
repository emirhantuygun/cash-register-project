package com.bit.productservice.service;

import com.bit.productservice.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
    }

    @Test
    void createProductCache_shouldCreateCacheForGivenProductResponse() {
        // Given
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        // When
        ProductResponse cachedProductResponse = cacheService.createProductCache(productResponse);

        // Then
        assertNotNull(cachedProductResponse);
        assertEquals(productResponse.getId(), cachedProductResponse.getId());
    }

    @Test
    void updateProductCache_shouldUpdateCacheForGivenProductResponse() {
        // Given
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        // When
        ProductResponse cachedProductResponse = cacheService.updateProductCache(productResponse);

        // Then
        assertNotNull(cachedProductResponse);
        assertEquals(productResponse.getId(), cachedProductResponse.getId());
    }
}
