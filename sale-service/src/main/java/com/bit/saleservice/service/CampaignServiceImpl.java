package com.bit.saleservice.service;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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

    private CampaignResponse mapToCampaignResponse(Campaign campaign) {
        List<Long> ids = campaign.getSales().stream().map(Sale::getId).toList();
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .details(campaign.getDetails())
                .expiration(campaign.getExpiration())
                .sales(ids).build();
    }
}
