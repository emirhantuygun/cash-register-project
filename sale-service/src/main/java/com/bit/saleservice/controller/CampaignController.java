package com.bit.saleservice.controller;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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

    @GetMapping("/filteredAndSorted")
    public ResponseEntity<Page<CampaignResponse>> getAllCampaignsFilteredAndSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) Boolean isExpired
    ) {
        logger.info("Received request to fetch all campaigns with filters and sorting: page={}, size={}, sortBy={}, direction={}, name={}, details={}, isExpired={}",
                page, size, sortBy, direction, name, details, isExpired);
        Page<CampaignResponse> campaignResponses = campaignService.getAllCampaignsFilteredAndSorted(page, size, sortBy, direction, name, details, isExpired);

        logger.info("Returning {} campaign responses filtered and sorted", campaignResponses.getTotalElements());
        return new ResponseEntity<>(campaignResponses, HttpStatus.OK);
    }
}
