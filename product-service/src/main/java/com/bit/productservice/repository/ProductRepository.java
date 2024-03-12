package com.bit.productservice.repository;

import com.bit.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM products WHERE deleted = true", nativeQuery = true)
    List<Product> findSoftDeletedProducts();

    Page<Product> findByNameContaining(String name, Pageable pageable);
    Page<Product> findByDescriptionContaining(String description, Pageable pageable);
    Page<Product> findByPriceGreaterThanEqual(BigDecimal minPrice, Pageable pageable);
    Page<Product> findByPriceLessThanEqual(BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByNameContainingAndDescriptionContaining(String name, String description, Pageable pageable);
    Page<Product> findByNameContainingAndPriceGreaterThanEqual(String name, BigDecimal minPrice, Pageable pageable);
    Page<Product> findByNameContainingAndPriceLessThanEqual(String name, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByDescriptionContainingAndPriceGreaterThanEqual(String description, BigDecimal minPrice, Pageable pageable);
    Page<Product> findByDescriptionContainingAndPriceLessThanEqual(String description, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByNameContainingAndDescriptionContainingAndPriceGreaterThanEqual(String name, String description, BigDecimal minPrice, Pageable pageable);
    Page<Product> findByNameContainingAndDescriptionContainingAndPriceLessThanEqual(String name, String description, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByNameContainingAndPriceBetween(String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByDescriptionContainingAndPriceBetween(String description, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> findByNameContainingAndDescriptionContainingAndPriceBetween(String name, String description, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);



}
