package com.bit.productservice.service;


import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.model.Product;
import com.bit.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse getProduct (Long id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    public List<ProductResponse> getDeletedProducts() {
        List<Product> products = productRepository.findSoftDeletedProducts();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    public Page<ProductResponse> getAllProductsPaginated(int page, int size) {
        Page<Product> productPage = productRepository.findAll(PageRequest.of(page, size));
        return productPage.map(this::mapToProductResponse);
    }

    @Override
    public List<ProductResponse> getAllProductsSorted(String sortBy, Sort.Direction direction) {
        List<Product> products = productRepository.findAll(Sort.by(direction, sortBy));
        return products.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    public Page<ProductResponse> getAllProductsPaginatedAndSorted(int page, int size, String sortBy, Sort.Direction direction) {
        Page<Product> productPage = productRepository.findAll(PageRequest.of(page, size, direction, sortBy));
        return productPage.map(this::mapToProductResponse);
    }

    public Page<ProductResponse> getAllProductsFiltered(String name, String description, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {

        Page<Product> productPage = Page.empty();
        if (name != null && description == null && minPrice == null && maxPrice == null) {
            productPage = productRepository.findByNameContaining(name, pageable);
        }
        if (name == null && description != null && minPrice == null && maxPrice == null) {
            productPage = productRepository.findByDescriptionContaining(description, pageable);
        }
        if (name == null && description == null && minPrice != null && maxPrice == null) {
            productPage = productRepository.findByPriceGreaterThanEqual(minPrice, pageable);
        }
        if (name == null && description == null && minPrice == null && maxPrice != null) {
            productPage = productRepository.findByPriceLessThanEqual(maxPrice, pageable);
        }
        if (name != null && description != null && minPrice == null && maxPrice == null) {
            productPage = productRepository.findByNameContainingAndDescriptionContaining(name, description, pageable);
        }
        if (name != null && description == null && minPrice != null && maxPrice == null) {
            productPage = productRepository.findByNameContainingAndPriceGreaterThanEqual(name, minPrice, pageable);
        }
        if (name != null && description == null && minPrice == null && maxPrice != null) {
            productPage = productRepository.findByNameContainingAndPriceLessThanEqual(name, maxPrice, pageable);
        }
        if (name == null && description != null && minPrice != null && maxPrice == null) {
            productPage = productRepository.findByDescriptionContainingAndPriceGreaterThanEqual(description, minPrice, pageable);
        }
        if (name == null && description != null && minPrice == null && maxPrice != null) {
            productPage = productRepository.findByDescriptionContainingAndPriceLessThanEqual(description, maxPrice, pageable);
        }
        if (name == null && description == null && minPrice != null && maxPrice != null) {
            productPage = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        }
        if (name != null && description != null && minPrice != null && maxPrice == null) {
            productPage = productRepository.findByNameContainingAndDescriptionContainingAndPriceGreaterThanEqual(name, description, minPrice, pageable);
        }
        if (name != null && description != null && minPrice == null && maxPrice != null) {
            productPage = productRepository.findByNameContainingAndDescriptionContainingAndPriceLessThanEqual(name, description, maxPrice, pageable);
        }
        if (name != null && description == null && minPrice != null && maxPrice != null) {
            productPage = productRepository.findByNameContainingAndPriceBetween(name, minPrice, maxPrice, pageable);
        }
        if (name == null && description != null && minPrice != null && maxPrice != null) {
            productPage = productRepository.findByDescriptionContainingAndPriceBetween(description, minPrice, maxPrice, pageable);
        }
        if (name != null && description != null && minPrice != null && maxPrice != null) {
            productPage = productRepository.findByNameContainingAndDescriptionContainingAndPriceBetween(name, description, minPrice, maxPrice, pageable);
        }


        return productPage.map(this::mapToProductResponse);
    }


    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        return mapToProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct (Long id, ProductRequest updatedProduct) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product doesn't exist with id " + id));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());

        productRepository.save(existingProduct);

        return mapToProductResponse(existingProduct);
    }

    @Override
    public void deleteProduct (Long id) {

        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));

        productRepository.deleteById(id);
    }


    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
