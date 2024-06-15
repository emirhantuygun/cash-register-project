package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CampaignService {
    CampaignResponse getCampaign(Long id);
    List<CampaignResponse> getAllCampaigns();
    Page<CampaignResponse> getAllCampaignsFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String details, Boolean isExpired);
}
