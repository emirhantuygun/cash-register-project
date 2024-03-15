package com.bit.productservice.repository;

import com.bit.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = "SELECT * FROM products WHERE deleted = true", nativeQuery = true)
    List<Product> findSoftDeletedProducts();

    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
}
