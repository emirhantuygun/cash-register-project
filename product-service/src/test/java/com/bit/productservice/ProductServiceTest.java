package com.bit.productservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.entity.Product;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.service.BarcodeService;
import com.bit.productservice.service.ProductServiceImpl;
import com.bit.productservice.dto.ProductResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProductServiceTest {

    private ProductRepository productRepository;

    private BarcodeService barcodeService;
    private ProductServiceImpl productService;

    @BeforeEach
    public void setUp() {
        productRepository = mock(ProductRepository.class);
        barcodeService = mock(BarcodeService.class);
        productService = new ProductServiceImpl(productRepository, barcodeService);
    }

    @Test
    @DisplayName("getProduct - Should return product when valid id is provided")
    public void shouldReturnProduct_WhenValidIdIsProvided() {
        // Arrange
        Long productId = 1L;
        Product product = new Product();
        product.setId(productId);
        product.setName("Product 1");

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        // Act
        ProductResponse result = productService.getProduct(productId);

        // Assert
        assertEquals(productId, result.getId());
        assertEquals("Product 1", result.getName());

        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("getProduct - Should throw ProductNotFoundException when invalid id is provided")
    public void shouldThrowProductNotFoundException_WhenInvalidIdIsProvided() {
        // Arrange
        Long invalidProductId = 999L;
        when(productRepository.findById(invalidProductId)).thenReturn(java.util.Optional.empty());

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> productService.getProduct(invalidProductId));

        verify(productRepository).findById(invalidProductId);
    }

    @Test
    @DisplayName("getAllProducts - Should return a list of all products")
    public void shouldReturnAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(
                Product.builder().id(1L).name("Product 1").build(),
                Product.builder().id(2L).name("Product 2").build()
        );
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductResponse> result = productService.getAllProducts();

        // Assert
        assertEquals(products.size(), result.size());
        assertEquals(products.get(0).getId(), result.get(0).getId());
        assertEquals(products.get(0).getName(), result.get(0).getName());
        assertEquals(products.get(1).getId(), result.get(1).getId());
        assertEquals(products.get(1).getName(), result.get(1).getName());

        // Verify
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("getDeletedProducts - Should return all soft deleted products")
    public void shouldReturnAllSoftDeletedProducts() {
        // Given
        List<Product> softDeletedProducts = Arrays.asList(
                Product.builder().id(1L).name("Product 1").build(),
                Product.builder().id(2L).name("Product 2").build()
        );
        when(productRepository.findSoftDeletedProducts()).thenReturn(softDeletedProducts);

        // When
        List<ProductResponse> result = productService.getDeletedProducts();

        // Then
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Product 1");

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Product 2");

        verify(productRepository, times(1)).findSoftDeletedProducts();
        verifyNoMoreInteractions(productRepository);
    }

//    @Test
//    @SuppressWarnings("unchecked")
//    @DisplayName("getAllProductsFilteredAndSorted - Should return filtered and sorted products when empty values provided")
//    public void shouldReturnFilteredAndSortedProducts_WhenEmptyValuesProvided() {
//        Page<Product> productPage = mock(Page.class);
//        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class))).thenReturn(productPage);
//
//        // Mocked page of product responses
//        Page<ProductResponse> productResponsePage = mock(Page.class);
//        when(productPage.map(any(Function.class))).thenReturn(productResponsePage);
//
//        // Setting up the service method arguments
//        Page<ProductResponse> result = productService.getAllProductsFilteredAndSorted(PageRequest.of(0, 10), null, null, null, null);
//
//        // Verifying that the findAll method was called with the correct arguments
//        verify(productRepository, times(1)).findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class));
//
//        // Assertions
//        assertNotNull(result);
//    }




//    @Test
//    @DisplayName("getAllProductsFilteredAndSorted - Should return filtered and sorted products when all values provided")
//    public void shouldReturnFilteredAndSortedProducts_WhenAllValuesProvided() {
//        // Mocked page of products
//        Pageable pageable = PageRequest.of(0, 10);
//        String name = "test";
//        String description = "description";
//        BigDecimal minPrice = BigDecimal.valueOf(10);
//        BigDecimal maxPrice = BigDecimal.valueOf(100);
//
//        // Mock products and repository
//        List<Product> products = Arrays.asList(
//                new Product(1L, "Test Product 1", "Description 1", barcodeService.generateBarcodeNumber("Test Product 1"),BigDecimal.valueOf(50)),
//                new Product(2L, "Test Product 2", "Description 2",barcodeService.generateBarcodeNumber("Test Product 2"), BigDecimal.valueOf(80))
//        );
//        Page<Product> productPage = new PageImpl<>(products);
//
//        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable))).thenReturn(productPage);
//
//        // Call the service method
//        Page<ProductResponse> result = productService.getAllProductsFilteredAndSorted(pageable, name, description, minPrice, maxPrice);
//
//        // Verify the result
//        assertNotNull(result);
//        assertEquals(2, result.getContent().size());
//
//        verify(productRepository, times(1)).findAll(ArgumentMatchers.<Specification<Product>>any(), eq(pageable));
//        verifyNoMoreInteractions(productRepository);
//    }

    @Test
    @DisplayName("createProduct - Should return created product")
    public void shouldReturnCreatedProduct() {
        // Mock product request
        ProductRequest productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(10.0))
                .build();

        // Mock product
        Product product = Product.builder()
                .id(1L)
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        // Mock the productRepository.save method
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(1L); // Set the ID
            return savedProduct;
        });

        // Call the createProduct method
        ProductResponse result = productService.createProduct(productRequest);

        // Verify that productRepository.save was called with the correct product
        verify(productRepository, times(1)).save(any(Product.class));

        // Assertions
        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
    }

    @Test
    @DisplayName("updateProduct - Should update existing product")
    public void shouldReturnUpdatedProduct() {
        // Mock the existing product
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Existing Product")
                .description("Existing Description")
                .price(BigDecimal.valueOf(20.0))
                .build();

        // Mock the updated product request
        ProductRequest updatedProductRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(30.0))
                .build();

        // Mock the product repository findById method
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // Call the updateProduct method
        ProductResponse result = productService.updateProduct(1L, updatedProductRequest);

        // Verify that productRepository.findById was called with the correct ID
        verify(productRepository, times(1)).findById(1L);

        // Verify that the existing product was updated and saved
        verify(productRepository, times(1)).save(existingProduct);

        // Assertions
        assertNotNull(result);
        assertEquals(existingProduct.getId(), result.getId());
        assertEquals(updatedProductRequest.getName(), result.getName());
        assertEquals(updatedProductRequest.getDescription(), result.getDescription());
        assertEquals(updatedProductRequest.getPrice(), result.getPrice());
    }

    @Test
    @DisplayName("updateProduct - Should throw ProductNotFoundException when updating non-existing product")
    public void shouldThrowProductNotFoundException_WhenUpdatingNonExistingProduct() {

        ProductRequest updatedProductRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(30.0))
                .build();

        // Mock the product repository findById method
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the updateProduct method and expect an exception
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, updatedProductRequest));

        // Verify that productRepository.findById was called with the correct ID
        verify(productRepository, times(1)).findById(1L);

        // Verify that productRepository.save was not called
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct - Should delete existing product")
    public void shouldDeleteExistingProduct() {
        // Mock the existing product
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Existing Product")
                .description("Existing Description")
                .price(BigDecimal.valueOf(20.0))
                .build();

        // Mock the product repository findById method
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // Call the deleteProduct method
        productService.deleteProduct(1L);

        // Verify that productRepository.findById was called with the correct ID
        verify(productRepository, times(1)).findById(1L);

        // Verify that productRepository.deleteById was called with the correct ID
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteProduct - Should throw ProductNotFoundException when deleting non-existing product")
    public void shouldThrowProductNotFoundException_WhenDeletingNonExistingProduct() {
        // Mock the product repository findById method
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the deleteProduct method and expect an exception
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));

        // Verify that productRepository.findById was called with the correct ID
        verify(productRepository, times(1)).findById(1L);

        // Verify that productRepository.deleteById was not called
        verify(productRepository, never()).deleteById(anyLong());
    }

}


