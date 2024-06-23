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

/**
 * This interface represents the Product Repository, which extends the JpaRepository interface.
 * It provides methods for interacting with the Product entity in the database.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * This method retrieves all soft-deleted products from the database.
     *
     * @return a list of soft-deleted products
     */
    @Query(value = "SELECT * FROM products WHERE deleted = true", nativeQuery = true)
    List<Product> findSoftDeletedProducts();

    /**
     * This method retrieves a paginated list of products based on the given specification and pageable.
     *
     * @param spec the specification for filtering and sorting the products
     * @param pageable the pageable object for pagination
     * @return a page of products
     */
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    /**
     * This method checks if a product with the given id exists and is soft-deleted.
     *
     * @param id the id of the product
     * @return true if the product exists and is soft-deleted, false otherwise
     */
    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM products p WHERE p.id = :id AND p.deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(@Param("id") Long id);

    /**
     * This method restores a soft-deleted product by setting its deleted flag to false.
     *
     * @param id the id of the product to be restored
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE products SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreProduct(@Param("id") Long id);

    /**
     * This method permanently deletes a product from the database.
     *
     * @param id the id of the product to be deleted permanently
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM products WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

}
