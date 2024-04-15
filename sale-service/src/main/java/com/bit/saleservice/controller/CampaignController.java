package com.bit.saleservice.controller;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.repository.CampaignRepository;
import com.bit.saleservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/campaigns")
public class CampaignController {

    private final Logger logger = LogManager.getLogger(SaleServiceApplication.class);
    private final CampaignService campaignService;

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable("id") Long id) {
        logger.info("Received request to fetch campaign with ID: {}", id);
        CampaignResponse campaignResponse = campaignService.getCampaign(id);

        logger.info("Returning campaign response: {}", campaignResponse);
        return new ResponseEntity<>(campaignResponse, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        logger.info("Received request to fetch all campaigns");
        List<CampaignResponse> campaignResponses = campaignService.getAllCampaigns();

        logger.info("Returning {} campaign responses", campaignResponses.size());
        return new ResponseEntity<>(campaignResponses, HttpStatus.OK);
    }
}
