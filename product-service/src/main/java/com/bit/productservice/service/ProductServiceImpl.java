package com.bit.productservice.service;

import com.bit.productservice.ProductServiceApplication;
import com.bit.productservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.exception.ProductNotSoftDeletedException;
import com.bit.productservice.entity.Product;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.wrapper.ProductStockReduceRequest;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CachePut;
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
    public Page<ProductResponse> getAllProductsFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                                 String name, String description, BigDecimal minPrice,
                                                                 BigDecimal maxPrice, Integer minStock, Integer maxStock) {
        logger.info("Fetching all products with filters and sorting");
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Product> productsPage = productRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicates(name, description, minPrice, maxPrice, minStock, maxStock, criteriaBuilder, root);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        logger.info("Retrieved {} products", productsPage.getTotalElements());
        return productsPage.map(this::mapToProductResponse);
    }


    @Override
    public ProductResponse createProduct(ProductRequest productRequest) throws AlgorithmNotFoundException {
        logger.info("Creating product: {}", productRequest);
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .barcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()))
                .stockQuantity(productRequest.getStockQuantity())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);

        logger.info("Created product with ID: {}", product.getId());
        return mapToProductResponse(product);
    }

    @Override
    @CachePut(cacheNames = "product_id", key = "#id", unless = "#result == null")
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) throws AlgorithmNotFoundException {
        logger.info("Updating product with ID {}: {}", id, productRequest);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product doesn't exist with id " + id));

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setBarcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()));
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setPrice(productRequest.getPrice());

        productRepository.save(existingProduct);

        logger.info("Updated product with ID {}: {}", id, existingProduct);
        return mapToProductResponse(existingProduct);
    }

    @Override
    public ProductResponse restoreProduct(Long id) {
        if (!productRepository.existsByIdAndDeletedTrue(id))
            throw new ProductNotSoftDeletedException("Product with id " + id + " is not soft-deleted and cannot be restored.");

        productRepository.restoreProduct(id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Couldn't restore the product with id " + id));
        return mapToProductResponse(product);
    }

    @Override
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id))
            throw new ProductNotFoundException("Product not found with id " + id);

        productRepository.deleteById(id);
        logger.info("Product deleted!");
    }

    @Override
    public void deleteProductPermanently(Long id) {
        if (!productRepository.existsById(id))
            throw new ProductNotFoundException("Product not found with id " + id);
        productRepository.deletePermanently(id);
    }

    @Transactional
    @RabbitListener(queues = "${rabbitmq.queue}")
    @CachePut(cacheNames = "product_id", key = "#request.id", unless = "#result == null")
    public ProductResponse reduceProductStock(ProductStockReduceRequest request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + request.getId()));

        product.setStockQuantity(product.getStockQuantity() - request.getRequestedQuantity());
        productRepository.save(product);
        return mapToProductResponse(product);
    }

    @Override
    @CachePut(cacheNames = "product_id", key = "#request.id", unless = "#result == null")
    public ProductResponse returnProducts(ProductStockReturnRequest request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + request.getId()));

        product.setStockQuantity(product.getStockQuantity() + request.getReturnedQuantity());
        productRepository.save(product);
        return mapToProductResponse(product);
    }

    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates (String name, String description, BigDecimal minPrice, BigDecimal maxPrice,
                                           Integer minStock, Integer maxStock,
                                           CriteriaBuilder criteriaBuilder, Root<Product> root){
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
        if (minStock != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("stockQuantity"), minStock));
        }
        if (maxStock != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("stockQuantity"), maxStock));
        }
        return predicates;
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .barcodeNumber(product.getBarcodeNumber())
                .stockQuantity(product.getStockQuantity())
                .price(product.getPrice())
                .build();
    }
}
