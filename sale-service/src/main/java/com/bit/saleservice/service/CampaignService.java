package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * This interface defines the contract for the CampaignService.
 * It provides methods for retrieving campaign information.
 *
 * @author Emirhan Tuygun
 */
public interface CampaignService {

    /**
     * Retrieves a single campaign by its unique identifier.
     *
     * @param id The unique identifier of the campaign.
     * @return A CampaignResponse object containing the details of the campaign.
     */
    CampaignResponse getCampaign(Long id);

    /**
     * Retrieves all campaigns.
     *
     * @return A list of CampaignResponse objects containing the details of all campaigns.
     */
    List<CampaignResponse> getAllCampaigns();

    /**
     * Retrieves a paginated list of campaigns, filtered and sorted based on the provided parameters.
     *
     * @param page The page number to retrieve.
     * @param size The number of campaigns per page.
     * @param sortBy The field to sort by.
     * @param direction The sorting direction (asc or desc).
     * @param name The name of the campaign to filter by.
     * @param details The details of the campaign to filter by.
     * @param isExpired A boolean indicating whether to filter expired campaigns.
     * @return A Page object containing the paginated list of CampaignResponse objects.
     */
    Page<CampaignResponse> getAllCampaignsFilteredAndSorted(int page, int size, String sortBy, String direction, String name, String details, Boolean isExpired);
}
