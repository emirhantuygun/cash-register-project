package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignResponse;

import java.util.List;

public interface CampaignService {
    CampaignResponse getCampaign(Long id);

    List<CampaignResponse> getAllCampaigns();
}
