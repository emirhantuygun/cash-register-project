package com.bit.productservice.repository;

import com.bit.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM products WHERE deleted = true", nativeQuery = true)
    List<Product> findSoftDeletedProducts();

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM products p WHERE p.id = :id AND p.deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(@Param("id") Long id);

    @Query(value = "SELECT CASE WHEN deleted = true THEN true ELSE false END FROM products WHERE id = :id", nativeQuery = true)
    boolean isProductSoftDeleted(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreProduct(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM products WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

}
