package com.bit.productservice.service;

import com.bit.productservice.ProductServiceApplication;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.exception.ProductNotSoftDeletedException;
import com.bit.productservice.entity.Product;
import com.bit.productservice.repository.ProductRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LogManager.getLogger(ProductServiceApplication.class);
    private final ProductRepository productRepository;
    private final BarcodeService barcodeService;

    @Override
    public ProductResponse getProduct(Long id) {
        logger.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));

        logger.info("Retrieved product: {}", product);
        return mapToProductResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();

        logger.info("Retrieved {} products", products.size());
        return products.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    public List<ProductResponse> getDeletedProducts() {
        logger.info("Fetching all deleted products");
        List<Product> products = productRepository.findSoftDeletedProducts();

        logger.info("Retrieved {} deleted products", products.size());
        return products.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    public Page<ProductResponse> getAllProductsFilteredAndSorted(Pageable pageable, String name, String description, BigDecimal minPrice, BigDecimal maxPrice) {
        logger.info("Fetching all products with filters and sorting: pageable={}, name={}, description={}, minPrice={}, maxPrice={}",
                pageable, name, description, minPrice, maxPrice);
        Page<Product> productsPage = productRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(description)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        logger.info("Retrieved {} products", productsPage.getTotalElements());
        return productsPage.map(this::mapToProductResponse);
    }


    @Override
    public ProductResponse createProduct(ProductRequest productRequest) {
        logger.info("Creating product: {}", productRequest);
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .barcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()))
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);

        logger.info("Created product with ID: {}", product.getId());
        return mapToProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        logger.info("Updating product with ID {}: {}", id, productRequest);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product doesn't exist with id " + id));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setBarcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()));
        existingProduct.setPrice(productRequest.getPrice());

        productRepository.save(existingProduct);

        logger.info("Updated product with ID {}: {}", id, existingProduct);
        return mapToProductResponse(existingProduct);
    }

    @Override
    public ProductResponse restoreUser(Long id) {
        if (!productRepository.isProductSoftDeleted(id)) {
            throw new ProductNotSoftDeletedException("Product with id " + id + " is not soft-deleted and cannot be restored.");
        }
        productRepository.restoreProduct(id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Couldn't restore the product with id " + id));
        return mapToProductResponse(product);
    }

    @Override
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));

        productRepository.deleteById(id);
        logger.info("Deleted product: {}", existingProduct);
    }

    @Override
    public void deleteProductPermanently(Long id) {
        productRepository.deletePermanently(id);
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .barcodeNumber(product.getBarcodeNumber())
                .price(product.getPrice())
                .build();
    }
}
