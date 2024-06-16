package com.bit.productservice;

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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProduct_shouldReturnProductResponse_whenProductExists() {
        // Arrange
        Long productId = 1L;
        ProductResponse expectedResponse = new ProductResponse();
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

        // Act
        ProductResponse actualResponse = productService.getProduct(productId);

        // Assert
        assertNotNull(actualResponse);
    }

    @Test
    void getProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(productId));
    }

    @Test
    void getAllProducts_shouldReturnListOfProductResponses() {
        // Arrange
        List<ProductResponse> expectedResponse = Collections.singletonList(new ProductResponse());
        when(productRepository.findAll()).thenReturn(Collections.singletonList(new Product()));

        // Act
        List<ProductResponse> actualResponse = productService.getAllProducts();

        // Assert
        assertEquals(expectedResponse.size(), actualResponse.size());
    }

    @Test
    void getDeletedProducts_shouldReturnListOfDeletedProductResponses() {
        // Arrange
        List<ProductResponse> expectedResponse = Collections.singletonList(new ProductResponse());
        when(productRepository.findSoftDeletedProducts()).thenReturn(Collections.singletonList(new Product()));

        // Act
        List<ProductResponse> actualResponse = productService.getDeletedProducts();

        // Assert
        assertEquals(expectedResponse.size(), actualResponse.size());
    }

    @Test
    void getAllProductsFilteredAndSorted_shouldReturnPageOfProductResponses() {
        // Arrange
        List<Product> products = Collections.singletonList(new Product());
        Page<Product> expectedResponse = new PageImpl<>(products);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedResponse);

        // Act
        Page<ProductResponse> actualResponse = productService.getAllProductsFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null);

        // Assert
        assertEquals(expectedResponse.getTotalElements(), actualResponse.getTotalElements());
    }


    @Test
    void createProduct_shouldCreateAndReturnProductResponse() throws AlgorithmNotFoundException {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setDescription("Test Description");
        request.setPrice(BigDecimal.TEN);
        request.setStockQuantity(100);
        when(barcodeService.generateBarcodeNumber(anyString())).thenReturn("1234567890123");
        when(productRepository.save(any())).thenReturn(new Product());

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
    }

    @Test
    void updateProduct_shouldUpdateAndReturnProductResponse() throws AlgorithmNotFoundException {
        // Arrange
        Long productId = 1L;
        ProductRequest request = new ProductRequest();
        request.setName("Updated Product");
        request.setDescription("Updated Description");
        request.setPrice(BigDecimal.valueOf(20));
        request.setStockQuantity(50);
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(barcodeService.generateBarcodeNumber(anyString())).thenReturn("1234567890123");
        when(productRepository.save(any())).thenReturn(existingProduct);

        // Act
        ProductResponse response = productService.updateProduct(productId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Product", response.getName());
    }

    @Test
    void updateProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        Long productId = 1L;
        ProductRequest request = new ProductRequest();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(productId, request));
    }

    @Test
    void restoreProduct_shouldRestoreAndReturnProductResponse() {
        // Arrange
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        when(productRepository.existsByIdAndDeletedTrue(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any())).thenReturn(existingProduct);
        // Act
        ProductResponse response = productService.restoreProduct(productId);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getId());
    }

    @Test
    void restoreProduct_shouldThrowProductNotSoftDeletedException_whenProductIsNotSoftDeleted() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsByIdAndDeletedTrue(productId)).thenReturn(false);

        // Act and Assert
        assertThrows(ProductNotSoftDeletedException.class, () -> productService.restoreProduct(productId));
    }

    @Test
    void deleteProduct_shouldDeleteProduct() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(productId));
    }

    @Test
    void deleteProductPermanently_shouldDeleteProductPermanently() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // Act
        productService.deleteProductPermanently(productId);

        // Assert
        verify(productRepository, times(1)).deletePermanently(productId);
    }

    @Test
    void deleteProductPermanently_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProductPermanently(productId));
    }

    @Test
    void checkStock_shouldReturnTrue_whenRequestedQuantityIsLessThanOrEqualToStockQuantity() {
        // Arrange
        Long productId = 1L;
        int requestedQuantity = 5;
        Product product = new Product();
        product.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(productId, requestedQuantity);

        // Assert
        assertTrue(result);
    }

    @Test
    void checkStock_shouldReturnFalse_whenRequestedQuantityIsGreaterThanStockQuantity() {
        // Arrange
        Long productId = 1L;
        int requestedQuantity = 15;
        Product product = new Product();
        product.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(productId, requestedQuantity);

        // Assert
        assertFalse(result);
    }

    @Test
    void reduceProductStock_shouldReduceStockQuantity() {
        // Arrange
        Long productId = 1L;
        int requestedQuantity = 5;
        Product existingProduct = new Product();
        existingProduct.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act
        productService.reduceProductStock(new ProductStockReduceRequest(productId, requestedQuantity));

        // Assert
        assertEquals(5, existingProduct.getStockQuantity());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void returnProducts_shouldIncreaseStockQuantity() {
        // Arrange
        Long productId = 1L;
        int returnedQuantity = 5;
        Product existingProduct = new Product();
        existingProduct.setStockQuantity(10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        // Act
        productService.returnProducts(new ProductStockReturnRequest(productId, returnedQuantity));

        // Assert
        assertEquals(15, existingProduct.getStockQuantity());
        verify(productRepository, times(1)).save(existingProduct);
    }

//    private static class PageImpl<T> implements Page<T> {
//        private final List<T> content;
//
//        public PageImpl(List<T> content) {
//            this.content = content;
//        }
//
//        @Override
//        public int getTotalPages() {
//            return 1;
//        }
//
//        @Override
//        public long getTotalElements() {
//            return content.size();
//        }
//
//        @Override
//        public <U> Page<U> map(Function<? super T, ? extends U> converter) {
//            return null;
//        }
//
//        @Override
//        public int getNumber() {
//            return 0;
//        }
//
//        @Override
//        public int getSize() {
//            return content.size();
//        }
//
//        @Override
//        public int getNumberOfElements() {
//            return content.size();
//        }
//
//        @Override
//        public List<T> getContent() {
//            return content;
//        }
//
//        @Override
//        public boolean hasContent() {
//            return !content.isEmpty();
//        }
//
//        @Override
//        public Sort getSort() {
//            return null;
//        }
//
//        @Override
//        public boolean isFirst() {
//            return false;
//        }
//
//        @Override
//        public boolean isLast() {
//            return false;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return false;
//        }
//
//        @Override
//        public boolean hasPrevious() {
//            return false;
//        }
//
//        @Override
//        public Pageable nextPageable() {
//            return null;
//        }
//
//        @Override
//        public Pageable previousPageable() {
//            return null;
//        }
//
//        @Override
//        public Iterator<T> iterator() {
//            return null;
//        }
    }
