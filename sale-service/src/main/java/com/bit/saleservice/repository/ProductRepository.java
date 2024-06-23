package com.bit.saleservice.repository;

import com.bit.saleservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * This interface represents a repository for managing {@link Product} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations.
 * Additionally, it includes custom methods for deleting and restoring products based on sale ID.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Updates the 'deleted' field of all products associated with the given sale ID to true.
     * This method is annotated with {@link Transactional} to ensure atomicity and {@link Modifying} to indicate that it modifies data.
     *
     * @param saleId The ID of the sale for which the products will be deleted.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = true WHERE sale_id = :id", nativeQuery = true)
    void deleteAllBySaleId(@Param("id") Long saleId);

    /**
     * Updates the 'deleted' field of all products associated with the given sale ID to false.
     * This method is annotated with {@link Transactional} to ensure atomicity and {@link Modifying} to indicate that it modifies data.
     *
     * @param saleId The ID of the sale for which the products will be restored.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = false WHERE sale_id = :id", nativeQuery = true)
    void restoreProductsBySaleId(@Param("id") Long saleId);
}
