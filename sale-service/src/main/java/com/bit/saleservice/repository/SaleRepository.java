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

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findAll(Specification<Sale> spec, Pageable pageable);

    @Query(value = "SELECT * FROM sales WHERE deleted = true", nativeQuery = true)
    List<Sale> findSoftDeletedSales();

    @Query(value = "SELECT CASE WHEN deleted = true THEN true ELSE false END FROM sales WHERE id = :id", nativeQuery = true)
    boolean isSaleSoftDeleted(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE sales SET deleted = true WHERE id = :id", nativeQuery = true)
    void deleteById(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE sales SET deleted = false WHERE id = :id", nativeQuery = true)
    void restoreSale(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sales WHERE id = :id", nativeQuery = true)
    void deletePermanently(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM sale_campaigns WHERE sale_id = :id", nativeQuery = true)
    void deleteCampaignsForSale(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM products WHERE sale_id = :id", nativeQuery = true)
    void deleteProductsForSale(@Param("id") Long id);
}
