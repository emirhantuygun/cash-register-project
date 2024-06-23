package com.bit.saleservice.repository;

import com.bit.saleservice.entity.Sale;
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
 * Repository interface for Sale entity.
 * Provides methods for interacting with the Sale table in the database.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Finds all Sales that match the given Specification and are paginated.
     *
     * @param spec The Specification to filter Sales.
     * @param pageable The Pageable object for pagination.
     * @return A Page of Sale entities that match the given Specification.
     */
    Page<Sale> findAll(Specification<Sale> spec, Pageable pageable);

    /**
     * Finds all soft-deleted Sales.
     *
     * @return A List of Sale entities that have been soft-deleted.
     */
    @Query(value = "SELECT * FROM sales WHERE deleted = true", nativeQuery = true)
    List<Sale> findSoftDeletedSales();

    /**
     * Checks if a Sale with the given ID exists and is soft-deleted.
     *
     * @param id The ID of the Sale to check.
     * @return True if a Sale with the given ID exists and is soft-deleted, false otherwise.
     */
    @Query(value = "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM sales s WHERE s.id = :id AND s.deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(@Param("id") Long id);

    /**
     * Soft-deletes a Sale with the given ID.
     *
     * @param id The ID of the Sale to soft-delete.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE sales SET deleted = true WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);

    /**
     * Restores a soft-deleted Sale with the given ID.
     *
     * @param id The ID of the Sale to restore.
     */
    @Transactional
    @Modifying
    @Query(value = "UPDATE sales SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreSale(@Param("id") Long id);

    /**
     * Permanently deletes a Sale with the given ID.
     *
     * @param id The ID of the Sale to delete permanently.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sales WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    /**
     * Deletes all SaleCampaigns associated with a Sale with the given ID.
     *
     * @param id The ID of the Sale to delete SaleCampaigns for.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sale_campaigns WHERE sale_id = :id", nativeQuery = true)
    void deleteCampaignsForSale(@Param("id") Long id);

    /**
     * Deletes all Products associated with a Sale with the given ID.
     *
     * @param id The ID of the Sale to delete Products for.
     */
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM products WHERE sale_id = :id", nativeQuery = true)
    void deleteProductsForSale(@Param("id") Long id);
}
