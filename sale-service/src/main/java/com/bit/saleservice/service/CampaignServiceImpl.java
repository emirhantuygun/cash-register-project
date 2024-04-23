package com.bit.saleservice.service;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService{

    private final Logger logger = LogManager.getLogger(SaleServiceApplication.class);
    private final CampaignRepository campaignRepository;

    @Override
    public CampaignResponse getCampaign(Long id) {
        logger.info("Fetching campaign with ID: {}", id);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with id " + id));

        logger.info("Retrieved campaign: {}", campaign);
        return mapToCampaignResponse(campaign);
    }

    @Override
    public List<CampaignResponse> getAllCampaigns() {
        logger.info("Fetching all campaigns");
        List<Campaign> campaigns = campaignRepository.findAll();

        logger.info("Retrieved {} campaigns", campaigns.size());
        return campaigns.stream().map(this::mapToCampaignResponse).toList();
    }

    @Override
    public Page<CampaignResponse> getAllCampaignsFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String details, Boolean isExpired) {
        logger.info("Fetching all campaigns with filters and sorting");
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        Page<Campaign> campaignsPage = campaignRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(details)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("details")), "%" + details.toLowerCase() + "%"));
            }
            if (isExpired != null) {
                if (isExpired) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expiration"), new Date()));
                } else {
                    predicates.add(criteriaBuilder.greaterThan(root.get("expiration"), new Date()));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        logger.info("Retrieved {} campaigns", campaignsPage.getTotalElements());
        return campaignsPage.map(this::mapToCampaignResponse);
    }

    private CampaignResponse mapToCampaignResponse(Campaign campaign) {
        List<Long> ids = campaign.getSales().stream().map(Sale::getId).toList();
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .details(campaign.getDetails())
                .expiration(campaign.getExpiration())
                .saleIds(ids).build();
    }
}
