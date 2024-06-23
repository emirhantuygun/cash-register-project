package com.bit.saleservice.controller;

import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for handling campaign related operations.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Retrieves a campaign by its unique identifier.
     *
     * @param id The unique identifier of the campaign to retrieve.
     * @return A ResponseEntity containing the retrieved campaign data or an error status if the campaign is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable("id") Long id) {
        log.trace("Entering getCampaign method in CampaignController with id: {}", id);

        CampaignResponse campaignResponse = campaignService.getCampaign(id);
        log.info("Successfully retrieved campaign with id: {}", id);

        log.trace("Exiting getCampaign method in CampaignController with id: {}", id);
        return new ResponseEntity<>(campaignResponse, HttpStatus.OK);
    }

    /**
     * Retrieves all campaigns from the system.
     *
     * @return A ResponseEntity containing a list of all campaigns or an error status if any issues occur.
     */
    @GetMapping()
    public ResponseEntity<List<CampaignResponse>> getAllCampaigns() {
        log.trace("Entering getAllCampaigns method in CampaignController");

        List<CampaignResponse> campaignResponses = campaignService.getAllCampaigns();
        log.info("Successfully retrieved all campaigns");

        log.trace("Exiting getAllCampaigns method in CampaignController");
        return new ResponseEntity<>(campaignResponses, HttpStatus.OK);
    }

    /**
     * Retrieves all campaigns from the system, applying filtering and sorting.
     *
     * @param page The page number to retrieve (default is 0).
     * @param size The number of campaigns per page (default is 10).
     * @param sortBy The field to sort by (default is "id").
     * @param direction The sorting direction (default is "ASC").
     * @param name The name of the campaign to filter by (optional).
     * @param details The details of the campaign to filter by (optional).
     * @param isExpired The expiration status of the campaign to filter by (optional).
     * @return A ResponseEntity containing a paginated list of filtered and sorted campaigns or an error status if any issues occur.*
     */
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
        log.trace("Entering getAllCampaignsFilteredAndSorted method in CampaignController " +
                        "with page: {}, size: {}, sortBy: {}, direction: {}, name: {}, details: {}, isExpired: {}",
                page, size, sortBy, direction, name, details, isExpired);

        Page<CampaignResponse> campaignResponses = campaignService.getAllCampaignsFilteredAndSorted(page, size, sortBy, direction, name, details, isExpired);
        log.info("Successfully retrieved filtered and sorted campaigns");

        log.trace("Exiting getAllCampaignsFilteredAndSorted method in CampaignController");
        return new ResponseEntity<>(campaignResponses, HttpStatus.OK);
    }
}
