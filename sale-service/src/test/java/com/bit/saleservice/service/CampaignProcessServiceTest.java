package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignProcessRequest;
import com.bit.saleservice.dto.CampaignProcessResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Product;
import com.bit.saleservice.exception.CampaignNotApplicableException;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.exception.DuplicateCampaignException;
import com.bit.saleservice.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CampaignProcessServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignProcessService campaignProcessService;

    private Campaign campaign1;
    private Campaign campaign2;
    private Campaign campaign3;

    @BeforeEach
    public void setUp() {
        campaign1 = new Campaign();
        campaign1.setId(1L);
        campaign1.setExpiration(new Date(System.currentTimeMillis() + 10000));

        campaign2 = new Campaign();
        campaign2.setId(2L);
        campaign2.setExpiration(new Date(System.currentTimeMillis() + 10000));

        campaign3 = new Campaign();
        campaign3.setId(3L);
        campaign3.setExpiration(new Date(System.currentTimeMillis() + 10000));
    }

    @Test
    public void testGetCampaigns_WithValidIds_ReturnsCampaigns() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign1));
        when(campaignRepository.findById(2L)).thenReturn(Optional.of(campaign2));
        when(campaignRepository.findById(3L)).thenReturn(Optional.of(campaign3));

        List<Campaign> campaigns = campaignProcessService.getCampaigns(ids);

        assertEquals(3, campaigns.size());
        assertTrue(campaigns.contains(campaign1));
        assertTrue(campaigns.contains(campaign2));
        assertTrue(campaigns.contains(campaign3));
    }

    @Test
    public void testProcessCampaigns_WithDuplicateIds_ThrowsDuplicateCampaignException() {
        List<Long> ids = Arrays.asList(1L, 2L, 2L);
        CampaignProcessRequest request = new CampaignProcessRequest();
        request.setCampaignIds(ids);

        assertThrows(DuplicateCampaignException.class, () -> campaignProcessService.processCampaigns(request));
    }

    @Test
    public void testProcessCampaigns_WithCampaign1_AppliesDiscount() {
        List<Long> ids = List.of(1L);
        CampaignProcessRequest request = new CampaignProcessRequest();
        request.setCampaignIds(ids);
        request.setTotal(BigDecimal.valueOf(250));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign1));

        CampaignProcessResponse response = campaignProcessService.processCampaigns(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.getTotal());
    }

    @Test
    public void testProcessCampaigns_WithCampaign2_AppliesBuy2Get1Free() {
        List<Long> ids = List.of(2L);
        CampaignProcessRequest request = new CampaignProcessRequest();
        request.setCampaignIds(ids);

        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(100));
        product.setQuantity(3);
        product.setTotalPrice(BigDecimal.valueOf(300));
        request.setProducts(Collections.singletonList(product));
        request.setTotal(BigDecimal.valueOf(300));

        when(campaignRepository.findById(2L)).thenReturn(Optional.of(campaign2));

        CampaignProcessResponse response = campaignProcessService.processCampaigns(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(200), response.getTotal());
    }

    @Test
    public void testProcessCampaigns_WithCampaign3_Applies20PercentOff() {
        List<Long> ids = List.of(3L);
        CampaignProcessRequest request = new CampaignProcessRequest();
        request.setCampaignIds(ids);
        request.setTotal(BigDecimal.valueOf(100));

        when(campaignRepository.findById(3L)).thenReturn(Optional.of(campaign3));

        CampaignProcessResponse response = campaignProcessService.processCampaigns(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(80.0), response.getTotal());
    }

    @Test
    public void testCampaign_1_WithTotalLessThanLimit_ThrowsCampaignNotApplicableException() {
        CampaignProcessResponse response = CampaignProcessResponse.builder().total(BigDecimal.valueOf(150)).build();

        assertThrows(CampaignNotApplicableException.class, () -> campaignProcessService.campaign_1(response));
    }

    @Test
    public void testCampaign_1_WithTotalGreaterThanOrEqualToLimit_AppliesDiscount() {
        CampaignProcessResponse response = CampaignProcessResponse.builder().total(BigDecimal.valueOf(250)).build();

        campaignProcessService.campaign_1(response);

        assertEquals(BigDecimal.valueOf(200), response.getTotal());
    }

    @Test
    public void testIsCampaignValidAndNotExpired_WithValidCampaign_DoesNotThrowException() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign1));

        assertDoesNotThrow(() -> campaignProcessService.isCampaignValidAndNotExpired(1L));
    }

    @Test
    public void testIsCampaignValidAndNotExpired_WithExpiredCampaign_ThrowsCampaignNotApplicableException() {
        Campaign expiredCampaign = new Campaign();
        expiredCampaign.setId(4L);
        expiredCampaign.setExpiration(new Date(System.currentTimeMillis() - 10000));
        when(campaignRepository.findById(4L)).thenReturn(Optional.of(expiredCampaign));

        assertThrows(CampaignNotApplicableException.class, () -> campaignProcessService.isCampaignValidAndNotExpired(4L));
    }

    @Test
    public void testIsCampaignValidAndNotExpired_WithNonExistentCampaign_ThrowsCampaignNotFoundException() {
        when(campaignRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CampaignNotFoundException.class, () -> campaignProcessService.isCampaignValidAndNotExpired(5L));
    }
}
