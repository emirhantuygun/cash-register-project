package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import com.bit.saleservice.service.CampaignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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
public class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetCampaign_WhenCampaignExists_ReturnsCampaignResponse() {
        Long campaignId = 1L;
        Campaign campaign = new Campaign();
        campaign.setId(campaignId);
        campaign.setName("Test Campaign");
        campaign.setDetails("Details");
        campaign.setExpiration(new Date());

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

        CampaignResponse response = campaignService.getCampaign(campaignId);

        assertNotNull(response);
        assertEquals(campaignId, response.getId());
        assertEquals("Test Campaign", response.getName());
        verify(campaignRepository).findById(campaignId);
    }

    @Test
    public void testGetCampaign_WhenCampaignDoesNotExist_ThrowsCampaignNotFoundException() {
        Long campaignId = 1L;
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        assertThrows(CampaignNotFoundException.class, () -> campaignService.getCampaign(campaignId));
        verify(campaignRepository).findById(campaignId);
    }

    @Test
    public void testGetAllCampaigns_ReturnsListOfCampaignResponses() {
        List<Campaign> campaigns = Arrays.asList(
            new Campaign(1L, "Campaign 1", "Details 1", new Date(), false, Collections.emptyList()),
            new Campaign(2L, "Campaign 2", "Details 2", new Date(), false, Collections.emptyList())
        );
        
        when(campaignRepository.findAll()).thenReturn(campaigns);

        List<CampaignResponse> responses = campaignService.getAllCampaigns();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(campaignRepository).findAll();
    }

    @Test
    public void testGetAllCampaignsFilteredAndSorted_ReturnsPagedCampaignResponses() {
        List<Campaign> campaigns = Arrays.asList(
            new Campaign(1L, "Campaign 1", "Details 1", new Date(), false, Collections.emptyList()),
            new Campaign(2L, "Campaign 2", "Details 2", new Date(), false, Collections.emptyList())
        );
        Page<Campaign> campaignPage = new PageImpl<>(campaigns);

        when(campaignRepository.findAll((Specification<Campaign>) any(), any(Pageable.class))).thenReturn(campaignPage);

        Page<CampaignResponse> responsePage = campaignService.getAllCampaignsFilteredAndSorted(
            0, 10, "name", "ASC", "Campaign", "Details", null);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        verify(campaignRepository).findAll((Specification<Campaign>) any(), any(Pageable.class));
    }

}
