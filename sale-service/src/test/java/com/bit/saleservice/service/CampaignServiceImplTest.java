package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    public void testGetCampaign_WhenCampaignExists_ReturnsCampaignResponse() {
        // Arrange
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setName("Test Campaign");
        campaign.setDetails("Details");
        campaign.setExpiration(new Date());

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        // Act
        CampaignResponse response = campaignService.getCampaign(campaignId);

        // Assert
        assertNotNull(response);
        assertEquals(campaignId, response.getId());
        assertEquals("Test Campaign", response.getName());
        verify(campaignRepository).findById(campaignId);
    }

    @Test
    public void testGetCampaign_WhenCampaignDoesNotExist_ThrowsCampaignNotFoundException() {
        // Arrange
        Long campaignId = 1L;
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CampaignNotFoundException.class, () -> campaignService.getCampaign(campaignId));
        verify(campaignRepository).findById(campaignId);
    }

    @Test
    public void testGetAllCampaigns_ReturnsListOfCampaignResponses() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(
            new Campaign(1L, "Campaign 1", "Details 1", new Date(), false, Collections.emptyList()),
            new Campaign(2L, "Campaign 2", "Details 2", new Date(), false, Collections.emptyList())
        );
        
        when(campaignRepository.findAll()).thenReturn(campaigns);

        // Act
        List<CampaignResponse> responses = campaignService.getAllCampaigns();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(campaignRepository).findAll();
    }

    @Test
    public void testGetAllCampaignsFilteredAndSorted_ReturnsPagedCampaignResponses() {
        // Arrange
        List<Campaign> campaigns = Arrays.asList(
            new Campaign(1L, "Campaign 1", "Details 1", new Date(), false, Collections.emptyList()),
            new Campaign(2L, "Campaign 2", "Details 2", new Date(), false, Collections.emptyList())
        );
        Page<Campaign> campaignPage = new PageImpl<>(campaigns);

        when(campaignRepository.findAll((Specification<Campaign>) any(), any(Pageable.class))).thenReturn(campaignPage);

        // Act
        Page<CampaignResponse> responsePage = campaignService.getAllCampaignsFilteredAndSorted(
            0, 10, "name", "ASC", "Campaign", "Details", null);

        // Assert
        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        verify(campaignRepository).findAll((Specification<Campaign>) any(), any(Pageable.class));
    }
}
