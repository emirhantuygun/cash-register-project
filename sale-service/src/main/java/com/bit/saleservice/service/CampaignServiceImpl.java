package com.bit.saleservice.service;

import com.bit.saleservice.annotation.ExcludeFromGeneratedCoverage;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService{

    private final CampaignRepository campaignRepository;

    @Override
    public CampaignResponse getCampaign(Long id) {
        log.trace("Entering getCampaign method in CampaignServiceImpl with id: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Campaign not found with id: {}", id);
                    return new CampaignNotFoundException("Campaign not found with id " + id);
                });
        log.info("Successfully retrieved campaign with id: {}", id);

        log.trace("Exiting getCampaign method in CampaignServiceImpl with id: {}", id);
        return mapToCampaignResponse(campaign);
    }

    @Override
    public List<CampaignResponse> getAllCampaigns() {
        log.trace("Entering getAllCampaigns method in CampaignServiceImpl");

        List<Campaign> campaigns = campaignRepository.findAll();
        log.info("Successfully retrieved all campaigns");

        log.trace("Exiting getAllCampaigns method in CampaignServiceImpl");
        return campaigns.stream().map(this::mapToCampaignResponse).toList();
    }

    @Override
    public Page<CampaignResponse> getAllCampaignsFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String details, Boolean isExpired) {
        log.trace("Entering getAllCampaignsFilteredAndSorted method in CampaignServiceImpl with page: {}, size: {}, sortBy: {}, direction: {}, name: {}, details: {}, isExpired: {}",
                page, size, sortBy, direction, name, details, isExpired);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Campaign> campaignsPage = campaignRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = getPredicates(name, details, isExpired, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
        log.info("Successfully retrieved filtered and sorted campaigns");

        log.trace("Exiting getAllCampaignsFilteredAndSorted method in CampaignServiceImpl");
        return campaignsPage.map(this::mapToCampaignResponse);
    }

    @ExcludeFromGeneratedCoverage
    private List<Predicate> getPredicates(String name, String details, Boolean isExpired, Root<Campaign> root, CriteriaBuilder criteriaBuilder){
        log.trace("Entering getPredicates method in CampaignServiceImpl with name: {}, details: {}, isExpired: {}", name, details, isExpired);

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(name)) {
            log.debug("name query parameter is not empty");
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(details)) {
            log.debug("details query parameter is not empty");
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("details")), "%" + details.toLowerCase() + "%"));
        }
        if (isExpired != null) {
            log.debug("isExpired query parameter is not null");
            if (isExpired) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiration"), new Date()));
            } else {
                predicates.add(criteriaBuilder.greaterThan(root.get("expiration"), new Date()));
            }
        }

        log.trace("Exiting getPredicates method in CampaignServiceImpl with predicates: {}", predicates);
        return predicates;
    }

    private CampaignResponse mapToCampaignResponse(Campaign campaign) {
        log.trace("Entering mapToCampaignResponse method in CampaignServiceImpl with campaign: {}", campaign);

        List<Long> ids = campaign.getSales().stream().map(Sale::getId).toList();

        log.trace("Exiting mapToCampaignResponse method in CampaignServiceImpl");
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .details(campaign.getDetails())
                .expiration(campaign.getExpiration())
                .saleIds(ids).build();
    }
}
