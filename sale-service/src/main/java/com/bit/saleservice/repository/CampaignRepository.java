package com.bit.saleservice.repository;

import com.bit.saleservice.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This interface represents a repository for managing {@link Campaign} entities.
 * It extends Spring Data JPA's {@link JpaRepository} interface, providing basic CRUD operations
 * and additional methods for advanced querying.
 *
 * @author Emirhan Tuygun
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    /**
     * Finds all campaigns that satisfy the given {@link Specification} and are paginated according to the provided {@link Pageable}.
     *
     * @param spec the {@link Specification} to filter the campaigns
     * @param pageable the {@link Pageable} to define the pagination
     * @return a {@link Page} of {@link Campaign} entities that satisfy the given {@link Specification}
     */
    Page<Campaign> findAll(Specification<Campaign> spec, Pageable pageable);
}
