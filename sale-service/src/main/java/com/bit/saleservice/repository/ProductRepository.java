package com.bit.saleservice.repository;

import com.bit.saleservice.entity.Product;
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

    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = true WHERE sale_id = :id", nativeQuery = true)
    void deleteAllBySaleId(@Param("id") Long saleId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = false WHERE sale_id = :id", nativeQuery = true)
    void restoreProductsBySaleId(@Param("id") Long saleId);

}
