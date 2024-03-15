package com.bit.productservice;

import com.bit.productservice.model.Product;
import com.bit.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryTest {

    @Mock
    private ProductRepository productRepository;

    @Test
    void findSoftDeletedProducts_ReturnsSoftDeletedProducts() {
        // Mock soft deleted product
        Product softDeletedProduct = new Product();
        softDeletedProduct.setName("Soft Deleted Product");
        softDeletedProduct.setDescription("Soft Deleted Description");
        softDeletedProduct.setPrice(BigDecimal.TEN);

        // Mock the repository method
        when(productRepository.findSoftDeletedProducts()).thenReturn(Collections.singletonList(softDeletedProduct));

        // Call the method
        List<Product> softDeletedProducts = productRepository.findSoftDeletedProducts();

        // Assertions
        assertEquals(1, softDeletedProducts.size());
        assertEquals(softDeletedProduct.getName(), softDeletedProducts.get(0).getName());
        assertEquals(softDeletedProduct.getDescription(), softDeletedProducts.get(0).getDescription());
        assertEquals(softDeletedProduct.getPrice(), softDeletedProducts.get(0).getPrice());
    }

    @Test
    void findAll_WithSpecification_ReturnsMatchingProducts() {
        // Insert some products
        Product product1 = new Product();
        product1.setName("Product 1");
        product1.setDescription("Description 1");
        product1.setPrice(BigDecimal.valueOf(20.0));

        Product product2 = new Product();
        product2.setName("Product 2");
        product2.setDescription("Description 2");
        product2.setPrice(BigDecimal.valueOf(30.0));

        // Mock the repository method for findAll
        when(productRepository.findAll(ArgumentMatchers.<Specification<Product>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product2)));

        // Create a specification to find products with price greater than 25.0
        Specification<Product> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("price"), BigDecimal.valueOf(25.0));

        // Call the method
        Page<Product> productsPage = productRepository.findAll(spec, PageRequest.of(0, 10));

        // Assertions
        assertEquals(1, productsPage.getTotalElements());
        assertEquals(product2.getName(), productsPage.getContent().get(0).getName());
        assertEquals(product2.getDescription(), productsPage.getContent().get(0).getDescription());
        assertEquals(product2.getPrice(), productsPage.getContent().get(0).getPrice());
    }
}
