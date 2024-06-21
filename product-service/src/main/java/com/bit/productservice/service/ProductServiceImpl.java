package com.bit.productservice.service;

import com.bit.productservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.entity.Product;
import com.bit.productservice.exception.AlgorithmNotFoundException;
import com.bit.productservice.exception.ProductNotFoundException;
import com.bit.productservice.exception.ProductNotSoftDeletedException;
import com.bit.productservice.repository.ProductRepository;
import com.bit.productservice.wrapper.ProductStockReduceRequest;
import com.bit.productservice.wrapper.ProductStockReturnRequest;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BarcodeService barcodeService;
    private final CacheService cacheService;

    @Override
    @Cacheable(cacheNames = "product_id", key = "#id", unless = "#result == null")
    public ProductResponse getProduct(Long id) {
        log.trace("Entering getProduct method in ProductServiceImpl class with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + id));
        ProductResponse response = mapToProductResponse(product);
        log.debug("Product found: {}", response);
        log.trace("Exiting getProduct method in ProductServiceImpl class");
        return response;
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.trace("Entering getAllProducts method in ProductServiceImpl class");
        List<Product> products = productRepository.findAll();
        List<ProductResponse> responses = products.stream().map(this::mapToProductResponse).toList();
        log.debug("Found {} products", responses.size());
        log.trace("Exiting getAllProducts method in ProductServiceImpl class");
        return responses;
    }

    @Override
    public List<ProductResponse> getDeletedProducts() {
        log.trace("Entering getDeletedProducts method in ProductServiceImpl class");
        List<Product> products = productRepository.findSoftDeletedProducts();
        List<ProductResponse> responses = products.stream().map(this::mapToProductResponse).toList();
        log.debug("Found {} deleted products", responses.size());
        log.trace("Exiting getDeletedProducts method in ProductServiceImpl class");
        return responses;
    }

    @Override
    public Page<ProductResponse> getAllProductsFilteredAndSorted(int page, int size, String sortBy, String direction,
                                                                 String name, String description, BigDecimal minPrice,
                                                                 BigDecimal maxPrice, Integer minStock, Integer maxStock) {
        log.trace("Entering getAllProductsFilteredAndSorted method in ProductServiceImpl class");
        log.debug("Filter and sort parameters - page: {}, size: {}, sortBy: {}, direction: {}, name: {}, description: {}, minPrice: {}, maxPrice: {}, minStock: {}, maxStock: {}",
                page, size, sortBy, direction, name, description, minPrice, maxPrice, minStock, maxStock);
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Product> productsPage = productRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicates(name, description, minPrice, maxPrice, minStock, maxStock, criteriaBuilder, root);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        Page<ProductResponse> responsePage = productsPage.map(this::mapToProductResponse);
        log.debug("Found {} products on the filtered and sorted page", responsePage.getTotalElements());
        log.trace("Exiting getAllProductsFilteredAndSorted method in ProductServiceImpl class");
        return responsePage;
    }

    @Override
    public ProductResponse createProduct(ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering createProduct method in ProductServiceImpl class");
        log.debug("Creating product with request: {}", productRequest);
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .barcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()))
                .stockQuantity(productRequest.getStockQuantity())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        ProductResponse productResponse = mapToProductResponse(product);
        cacheService.createProductCache(productResponse);
        log.info("Product created with ID: {}", product.getId());
        log.trace("Exiting createProduct method in ProductServiceImpl class");
        return productResponse;
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) throws AlgorithmNotFoundException {
        log.trace("Entering updateProduct method in ProductServiceImpl class");
        log.debug("Updating product with ID {} and request: {}", id, productRequest);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product doesn't exist with id " + id));
        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setBarcodeNumber(barcodeService.generateBarcodeNumber(productRequest.getName()));
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setPrice(productRequest.getPrice());
        productRepository.save(existingProduct);
        ProductResponse productResponse = mapToProductResponse(existingProduct);
        cacheService.updateProductCache(productResponse);
        log.info("Product updated with ID: {}", existingProduct.getId());
        log.trace("Exiting updateProduct method in ProductServiceImpl class");
        return productResponse;
    }

    @Override
    public ProductResponse restoreProduct(Long id) {
        log.trace("Entering restoreProduct method in ProductServiceImpl class with id: {}", id);
        if (!productRepository.existsByIdAndDeletedTrue(id)) {
            log.warn("Product with id {} is not soft-deleted and cannot be restored", id);
            throw new ProductNotSoftDeletedException("Product with id " + id + " is not soft-deleted and cannot be restored.");
        }
        productRepository.restoreProduct(id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Couldn't restore the product with id " + id));
        ProductResponse productResponse = mapToProductResponse(product);
        cacheService.createProductCache(productResponse);
        log.info("Product restored with ID: {}", id);
        log.trace("Exiting restoreProduct method in ProductServiceImpl class");
        return productResponse;
    }

    @Override
    @CacheEvict(cacheNames = "product_id", key = "#id")
    public void deleteProduct(Long id) {
        log.trace("Entering deleteProduct method in ProductServiceImpl class with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product not found with id {}", id);
            throw new ProductNotFoundException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
        log.info("Product soft deleted with ID: {}", id);
        log.trace("Exiting deleteProduct method in ProductServiceImpl class");
    }

    @Override
    @CacheEvict(cacheNames = "product_id", key = "#id")
    public void deleteProductPermanently(Long id) {
        log.trace("Entering deleteProductPermanently method in ProductServiceImpl class with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Product not found with id {}", id);
            throw new ProductNotFoundException("Product not found with id " + id);
        }
        productRepository.deletePermanently(id);
        log.info("Product permanently deleted with ID: {}", id);
        log.trace("Exiting deleteProductPermanently method in ProductServiceImpl class");
    }

    @Transactional
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void reduceProductStock(ProductStockReduceRequest request) {
        log.trace("Entering reduceProductStock method in ProductServiceImpl class with request: {}", request);
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + request.getId()));
        product.setStockQuantity(product.getStockQuantity() - request.getRequestedQuantity());
        productRepository.save(product);
        cacheService.updateProductCache(mapToProductResponse(product));
        log.info("Reduced stock for product with ID: {}", request.getId());
        log.trace("Exiting reduceProductStock method in ProductServiceImpl class");
    }

    @Override
    public void returnProducts(ProductStockReturnRequest request) {
        log.trace("Entering returnProducts method in ProductServiceImpl class with request: {}", request);
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id " + request.getId()));
        product.setStockQuantity(product.getStockQuantity() + request.getReturnedQuantity());
        productRepository.save(product);
        cacheService.updateProductCache(mapToProductResponse(product));
        log.info("Returned products for product with ID: {}", request.getId());
        log.trace("Exiting returnProducts method in ProductServiceImpl class");
    }

    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates(String name, String description, BigDecimal minPrice, BigDecimal maxPrice,
                                          Integer minStock, Integer maxStock,
                                          CriteriaBuilder criteriaBuilder, Root<Product> root) {
        log.trace("Entering getPredicates method in ProductServiceImpl class");
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
        log.trace("Exiting getPredicates method in ProductServiceImpl class");
        return predicates;
    }

    private ProductResponse mapToProductResponse(Product product) {
        log.trace("Entering mapToProductResponse method in ProductServiceImpl class");
        ProductResponse response = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .barcodeNumber(product.getBarcodeNumber())
                .stockQuantity(product.getStockQuantity())
                .price(product.getPrice())
                .build();
        log.trace("Exiting mapToProductResponse method in ProductServiceImpl class");
        return response;
    }
}
