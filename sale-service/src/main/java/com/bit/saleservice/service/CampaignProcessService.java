package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignProcessRequest;
import com.bit.saleservice.dto.CampaignProcessResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.exception.CampaignNotApplicableException;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.exception.DuplicateCampaignException;
import com.bit.saleservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class CampaignProcessService {

    private final CampaignRepository campaignRepository;

    protected List<Campaign> getCampaigns(List<Long> ids) {
        log.trace("Entering getCampaigns method in CampaignProcessService with ids: {}", ids);

        List<Campaign> campaigns;
        try {
            campaigns = ids.stream()
                    .map(campaignRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved campaigns with ids: {}", ids);
        } catch (Exception e) {
            log.error("Error occurred while retrieving campaigns with ids: {}", ids, e);
            throw e;
        }

        log.trace("Exiting getCampaigns method in CampaignProcessService with ids: {}", ids);
        return campaigns;
    }

    protected CampaignProcessResponse processCampaigns(CampaignProcessRequest campaignProcessRequest) {
        log.trace("Entering processCampaigns method in CampaignProcessService with campaignProcessRequest: {}", campaignProcessRequest);

        List<Long> ids = campaignProcessRequest.getCampaignIds();
        boolean hasDuplicates = ids.stream().distinct().count() < ids.size();

        if (hasDuplicates) {
            log.warn("Duplicate campaign ids found in campaignProcessRequest: {}", campaignProcessRequest);
            throw new DuplicateCampaignException("Same campaign cannot be used more than once!");
        }

        CampaignProcessResponse campaignProcessResponse = CampaignProcessResponse.builder()
                .products(campaignProcessRequest.getProducts())
                .total(campaignProcessRequest.getTotal())
                .build();

        for (long id : ids) {
            isCampaignValidAndNotExpired(id);

            switch ((int) id) {
                case 1:
                    log.debug("Processing campaign 1 for campaignProcessResponse: {}", campaignProcessResponse);
                    campaign_1(campaignProcessResponse);
                    break;

                case 2:
                    log.debug("Processing campaign 2 for campaignProcessResponse: {}", campaignProcessResponse);
                    campaign_2(campaignProcessResponse);
                    break;

                case 3:
                    log.debug("Processing campaign 3 for campaignProcessResponse: {}", campaignProcessResponse);
                    campaign_3(campaignProcessResponse);
                    break;
            }
        }
        log.info("Successfully processed campaigns for campaignProcessRequest: {}", campaignProcessRequest);

        log.trace("Exiting processCampaigns method in CampaignProcessService with campaignProcessRequest: {}", campaignProcessRequest);
        return campaignProcessResponse;
    }

    // Spend $200, Save $50
    protected void campaign_1(CampaignProcessResponse campaignProcessResponse) {
        log.trace("Entering campaign_1 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);

        BigDecimal limitTotal = BigDecimal.valueOf(200);
        BigDecimal total = campaignProcessResponse.getTotal();

        if (total.compareTo(limitTotal) < 0) {
            log.warn("Campaign 1 not applicable. Total: {} is less than limit: {}", total, limitTotal);
            throw new CampaignNotApplicableException("Campaign cannot be applied. Total must be equal to or over 200.");
        }

        BigDecimal newTotal = total.subtract(BigDecimal.valueOf(50));
        campaignProcessResponse.setTotal(newTotal);
        log.info("Successfully applied campaign 1. New total: {}", newTotal);

        log.trace("Exiting campaign_1 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);
    }

    // Buy 2, Get 1 Free
    protected void campaign_2(CampaignProcessResponse campaignProcessResponse) {
        log.trace("Entering campaign_2 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);

        boolean isApplicable = false;
        for (var product : campaignProcessResponse.getProducts()) {
            if (product.getQuantity() >= 3) {
                isApplicable = true;
                int freeProducts = product.getQuantity() / 3;

                BigDecimal totalPriceOfFreeProducts = product.getPrice().multiply(BigDecimal.valueOf(freeProducts));

                BigDecimal newTotalPrice = product.getTotalPrice().subtract(totalPriceOfFreeProducts);
                product.setTotalPrice(newTotalPrice);

                BigDecimal newTotal = campaignProcessResponse.getTotal().subtract(totalPriceOfFreeProducts);
                campaignProcessResponse.setTotal(newTotal);

                log.info("Successfully applied campaign 2 to the product with id: {}", product.getProductId());
            }
        }
        if (!isApplicable) {
            log.warn("Campaign 2 not applicable. No product with quantity greter than or equal to 3 found in campaignProcessResponse: {}", campaignProcessResponse);
            throw new CampaignNotApplicableException("Campaign cannot be applied. Requires a minimum purchase of the same 3 products to be applied.");
        }
        log.trace("Exiting campaign_2 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);
    }

    // 20% Off Your Entire Purchase
    protected void campaign_3(CampaignProcessResponse campaignProcessResponse) {
        log.trace("Entering campaign_3 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);

        BigDecimal newTotal = campaignProcessResponse.getTotal().multiply(BigDecimal.valueOf(0.80));
        campaignProcessResponse.setTotal(newTotal);
        log.info("Successfully applied campaign 3. New total: {}", newTotal);

        log.trace("Exiting campaign_3 method in CampaignProcessService with campaignProcessResponse: {}", campaignProcessResponse);
    }

    protected void isCampaignValidAndNotExpired(Long id) {
        log.trace("Entering isCampaignValidAndNotExpired method in CampaignProcessService with id: {}", id);

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with id: " + id));

        boolean isExpired = campaign.getExpiration().before(new Date());
        if (isExpired) {
            log.warn("Campaign expired with id: {} and name: {}", id, campaign.getName());
            throw new CampaignNotApplicableException("Campaign expired with name: " + campaign.getName());
        }
        log.info("Campaign is valid and not expired with id: {}", id);

        log.trace("Exiting isCampaignValidAndNotExpired method in CampaignProcessService with id: {}", id);
    }
}
