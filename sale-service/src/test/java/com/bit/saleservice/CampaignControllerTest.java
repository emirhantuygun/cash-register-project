package com.bit.saleservice;

import com.bit.saleservice.controller.CampaignController;
import com.bit.saleservice.dto.CampaignResponse;
import com.bit.saleservice.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CampaignControllerTest {

    @InjectMocks
    private CampaignController campaignController;

    @Mock
    private CampaignService campaignService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(campaignController).build();
    }

    @Test
    public void testGetCampaign_ReturnsCampaignResponse() throws Exception {
        Long id = 1L;
        CampaignResponse campaignResponse = new CampaignResponse();
        
        when(campaignService.getCampaign(id)).thenReturn(campaignResponse);

        mockMvc.perform(get("/campaigns/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(campaignResponse.getId()))
                .andExpect(jsonPath("$.name").value(campaignResponse.getName()));

        verify(campaignService, times(1)).getCampaign(id);
    }

    @Test
    public void testGetAllCampaigns_ReturnsListOfCampaignResponse() throws Exception {
        List<CampaignResponse> campaignResponseList = Arrays.asList(new CampaignResponse(), new CampaignResponse());
        
        when(campaignService.getAllCampaigns()).thenReturn(campaignResponseList);

        mockMvc.perform(get("/campaigns"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(campaignService, times(1)).getAllCampaigns();
    }

    @Test
    public void testGetAllCampaignsFilteredAndSorted_ReturnsPageOfCampaignResponse() throws Exception {
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

        mockMvc.perform(get("/campaigns/filteredAndSorted")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .param("sortBy", sortBy)
                .param("direction", direction)
                .param("name", name)
                .param("details", details)
                .param("isExpired", String.valueOf(isExpired)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(campaignResponsePage.getTotalElements()));

        verify(campaignService, times(1)).getAllCampaignsFilteredAndSorted(page, size, sortBy, direction, name, details, isExpired);
    }
}
