package com.bit.productservice.service;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.entity.Product;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.exception.ProductNotSoftDeletedException;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.service.BarcodeService;
import com.bit.productservice.service.ProductServiceImpl;
import com.bit.productservice.wrapper.ProductStockReduceRequest;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BarcodeService barcodeService;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getProduct_shouldReturnProductResponse_whenProductExists() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(1L));
    }

    @Test
    void getAllProducts_shouldReturnListOfProductResponses() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getDeletedProducts_shouldReturnListOfDeletedProductResponses() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findSoftDeletedProducts()).thenReturn(Collections.singletonList(product));

        // Act
        List<ProductResponse> responses = productService.getDeletedProducts();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void getAllProductsFilteredAndSorted_shouldReturnPageOfProductResponses() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));
        //noinspection unchecked
        when(productRepository.findAll((Specification<Product>) any(), any(Pageable.class))).thenReturn(productPage);

        // Act
        Page<ProductResponse> responsePage = productService.getAllProductsFilteredAndSorted(
                0, 10, "id", "ASC", null, null, null, null);

        // Assert
        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
    }

    @Test
    void createProduct_shouldReturnProductResponse() throws AlgorithmNotFoundException {
        // Arrange
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Test Product");

        when(barcodeService.generateBarcodeNumber(anyString())).thenReturn("1234567890123");
        when(productRepository.save(any(Product.class))).thenReturn(null);

        // Act
        ProductResponse response = productService.createProduct(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
    }

    @Test
    void updateProduct_shouldReturnUpdatedProductResponse_whenProductExists() throws AlgorithmNotFoundException {
        // Arrange
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Updated Product");
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(existingProduct));
        when(barcodeService.generateBarcodeNumber(anyString())).thenReturn("1234567890123");
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        // Act
        ProductResponse response = productService.updateProduct(1L, productRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Product", response.getName());
    }

    @Test
    void updateProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        ProductRequest productRequest = new ProductRequest();
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, productRequest));
    }

    @Test
    void restoreProduct_shouldReturnRestoredProductResponse_whenProductIsSoftDeleted() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        when(productRepository.existsByIdAndDeletedTrue(anyLong())).thenReturn(true);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.restoreProduct(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void restoreProduct_shouldThrowProductNotSoftDeletedException_whenProductIsNotSoftDeleted() {
        // Arrange
        when(productRepository.existsByIdAndDeletedTrue(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotSoftDeletedException.class, () -> productService.restoreProduct(1L));
    }

    @Test
    void deleteProduct_shouldDeleteProduct_whenProductExists() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(productRepository).deleteById(anyLong());

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deleteProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(productRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));
    }

    @Test
    void checkStock_shouldReturnTrue_whenRequestedQuantityIsLessThanOrEqualToStockQuantity() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(1L, 5);

        // Assert
        assertTrue(result);
    }

    @Test
    void checkStock_shouldReturnFalse_whenRequestedQuantityIsGreaterThanStockQuantity() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(1L, 15);

        // Assert
        assertFalse(result);
    }

    @Test
    void checkStock_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> productService.checkStock(1L, 5));
    }

    @Test
    void reduceProductStock_shouldReduceStock_whenProductExists() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        ProductStockReduceRequest request = new ProductStockReduceRequest(1L, 5);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        productService.reduceProductStock(request);

        // Assert
        assertEquals(5, product.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void returnProducts_shouldIncreaseStock_whenProductExists() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 5);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // Act
        productService.returnProducts(request);

        // Assert
        assertEquals(15, product.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }
}
