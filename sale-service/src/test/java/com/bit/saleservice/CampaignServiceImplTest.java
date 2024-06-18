package com.bit.saleservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.repository.CampaignRepository;
import com.bit.saleservice.service.CampaignServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

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

//    @Test
//    public void testMapToCampaignResponse_MapsCampaignToCampaignResponse() {
//        Campaign campaign = new Campaign();
//        campaign.setId(1L);
//        campaign.setName("Test Campaign");
//        campaign.setDetails("Details");
//        campaign.setExpiration(new Date());
//        Sale sale1 = new Sale();
//        sale1.setId(1L);
//        Sale sale2 = new Sale();
//        sale2.setId(2L);
//        campaign.setSales(Arrays.asList(sale1, sale2));
//
//        CampaignResponse response = campaignService.mapToCampaignResponse(campaign);
//
//        assertNotNull(response);
//        assertEquals(1L, response.getId());
//        assertEquals("Test Campaign", response.getName());
//        assertEquals("Details", response.getDetails());
//        assertEquals(2, response.getSaleIds().size());
//    }
}
