package com.bit.productservice.controller;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.service.ProductService;
import com.bit.productservice.wrapper.ProductStockCheckRequest;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductResponse productResponse;
    private Page<ProductResponse> pagedResponse;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse();
        productResponse.setId(1L);

        pagedResponse = new PageImpl<>(Collections.singletonList(productResponse), PageRequest.of(0, 10), 1);
    }

    @Test
    void testGetProduct_ShouldReturnProduct() {
        when(productService.getProduct(anyLong())).thenReturn(productResponse);

        ResponseEntity<ProductResponse> response = productController.getProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    void testGetAllProducts_ShouldReturnProductList() {
        when(productService.getAllProducts()).thenReturn(Collections.singletonList(productResponse));

        ResponseEntity<List<ProductResponse>> response = productController.getAllProducts();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).get(0).getId());
    }

    @Test
    void testGetDeletedProducts_ShouldReturnDeletedProductList() {
        when(productService.getDeletedProducts()).thenReturn(Collections.singletonList(productResponse));

        ResponseEntity<List<ProductResponse>> response = productController.getDeletedProducts();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testGetAllProductsFilteredAndSorted_ShouldReturnPagedProducts() {
        when(productService.getAllProductsFilteredAndSorted(anyInt(), anyInt(), anyString(), anyString(), any(), any(), any(), any()))
                .thenReturn(pagedResponse);

        ResponseEntity<Page<ProductResponse>> response = productController.getAllProductsFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).getContent().get(0).getId());
    }

    @Test
    void testCreateProduct_ShouldReturnCreatedProduct() throws AlgorithmNotFoundException {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        ProductRequest request = ProductRequest.builder().name("Product").build();
        ResponseEntity<ProductResponse> response = productController.createProduct(request);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    void testUpdateProduct_ShouldReturnUpdatedProduct() throws AlgorithmNotFoundException {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(productResponse);

        ProductRequest request = ProductRequest.builder().name("Product").build();
        ResponseEntity<ProductResponse> response = productController.updateProduct(1L, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).getId());
    }

    @Test
    void testRestoreProduct_ShouldReturnRestoredProduct() {
        when(productService.restoreProduct(anyLong())).thenReturn(productResponse);

        ResponseEntity<ProductResponse> response = productController.restoreProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testDeleteProduct_ShouldReturnSuccessMessage() {
        ResponseEntity<String> response = productController.deleteProduct(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Product soft deleted successfully!", response.getBody());
    }

    @Test
    void testDeleteProductPermanently_ShouldReturnSuccessMessage() {
        ResponseEntity<String> response = productController.deleteProductPermanently(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Product deleted permanently!", response.getBody());
    }

    @Test
    void testCheckStock_ShouldReturnStockStatus() {
        when(productService.checkStock(anyLong(), anyInt())).thenReturn(true);

        ResponseEntity<Boolean> response = productController.checkStock(new ProductStockCheckRequest(1L, 10));

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody());
    }

    @Test
    void testReturnProducts_ShouldReturnSuccessMessage() {
        ResponseEntity<String> response = productController.returnProducts(new ProductStockReturnRequest(1L, 10));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Product return request processed successfully.", response.getBody());
    }
}
