package com.bit.saleservice.controller;

import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CampaignControllerTest {

    @InjectMocks
    private CampaignController campaignController;

    @Mock
    private CampaignService campaignService;

    @Test
    public void testGetCampaign_ReturnsCampaignResponse() {
        // Arrange
        Long id = 1L;
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(id);
        campaignResponse.setName("CampaignName");

        when(campaignService.getCampaign(id)).thenReturn(campaignResponse);

        // Act
        ResponseEntity<CampaignResponse> response = campaignController.getCampaign(id);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(campaignResponse, response.getBody());
        verify(campaignService, times(1)).getCampaign(id);
    }

    @Test
    public void testGetAllCampaigns_ReturnsListOfCampaignResponse() {
        // Arrange
        List<CampaignResponse> campaignResponseList = Arrays.asList(new CampaignResponse(), new CampaignResponse());
        when(campaignService.getAllCampaigns()).thenReturn(campaignResponseList);

        // Act
        ResponseEntity<List<CampaignResponse>> response = campaignController.getAllCampaigns();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        verify(campaignService, times(1)).getAllCampaigns();
    }

    @Test
    public void testGetAllCampaignsFilteredAndSorted_ReturnsPageOfCampaignResponse() {
        // Arrange
        List<CampaignResponse> campaignResponseList = Arrays.asList(new CampaignResponse(), new CampaignResponse());
        Page<CampaignResponse> campaignResponsePage = new PageImpl<>(campaignResponseList, PageRequest.of(0, 10), 1);

        int page = 0;
        int size = 10;
        String sortBy = "id";
        String direction = "ASC";
        String name = "CampaignName";
        String details = "CampaignDetails";
        Boolean isExpired = false;

        when(campaignService.getAllCampaignsFilteredAndSorted(page, size, sortBy, direction, name, details, isExpired))
                .thenReturn(campaignResponsePage);

        // Act
        ResponseEntity<Page<CampaignResponse>> response = campaignController.getAllCampaignsFilteredAndSorted(
                page, size, sortBy, direction, name, details, isExpired);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getTotalElements());
        verify(campaignService, times(1)).getAllCampaignsFilteredAndSorted(page, size, sortBy, direction, name, details, isExpired);
    }
}
