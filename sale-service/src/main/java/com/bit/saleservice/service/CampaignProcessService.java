package com.bit.saleservice.service;

import com.bit.saleservice.dto.CampaignProcessRequest;
import com.bit.saleservice.dto.CampaignProcessResponse;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.exception.CampaignNotApplicableException;
import com.bit.saleservice.exception.CampaignNotFoundException;
import com.bit.saleservice.exception.DuplicateCampaignException;
import com.bit.saleservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampaignProcessService {

    private final CampaignRepository campaignRepository;

    public List<Campaign> getCampaigns(List<Long> ids){
        return ids.stream()
                .map(campaignRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public CampaignProcessResponse processCampaigns(CampaignProcessRequest campaignProcessRequest) {

        List<Long> ids = campaignProcessRequest.getCampaignIds();
        boolean hasDuplicates = ids.stream().distinct().count() < ids.size();

        if (hasDuplicates) {
            throw new DuplicateCampaignException("Same campaign cannot be used more than once!");
        }

        CampaignProcessResponse campaignProcessResponse = CampaignProcessResponse.builder()
                .products(campaignProcessRequest.getProducts())
                .total(campaignProcessRequest.getTotal())
                .build();

        for (long id : ids){
            isCampaignValidAndNotExpired(id);

            switch ((int) id) {
                case 1:
                    campaign_1(campaignProcessResponse);
                    break;

                case 2:
                    campaign_2(campaignProcessResponse);
                    break;

                case 3:
                    campaign_3(campaignProcessResponse);
                    break;
            }
        }
        return campaignProcessResponse;
    }

    // Spend $200, Save $50
    public void campaign_1(CampaignProcessResponse campaignProcessResponse) {

        BigDecimal limitTotal = BigDecimal.valueOf(200);
        BigDecimal total = campaignProcessResponse.getTotal();

        if (total.compareTo(limitTotal) < 0) {
            throw new CampaignNotApplicableException("Campaign cannot be applied. Total must be equal to or over 200.");
        }

        BigDecimal newTotal = total.subtract(BigDecimal.valueOf(50));
        campaignProcessResponse.setTotal(newTotal);
    }

    // Buy 2, Get 1 Free
    private void campaign_2(CampaignProcessResponse campaignProcessResponse) {
        boolean isApplicable = false;
        for (var product : campaignProcessResponse.getProducts()) {
            if (product.getQuantity() >= 3) {
                isApplicable = true;
                int freeProducts = product.getQuantity() / 3;

                BigDecimal totalPriceOfFreeProducts = product.getPrice().multiply(BigDecimal.valueOf(freeProducts));

                BigDecimal newTotalPrice = product.getTotalPrice().subtract(totalPriceOfFreeProducts);
                product.setTotalPrice(newTotalPrice);

                BigDecimal newTotal = campaignProcessResponse.getTotal().subtract(totalPriceOfFreeProducts);
                campaignProcessResponse.setTotal(newTotal);
            }
        }
        if (!isApplicable){
            throw new CampaignNotApplicableException("Campaign cannot be applied. Requires a minimum purchase of the same three products to be applied.");
        }
    }

    // 20% Off Your Entire Purchase
    private void campaign_3(CampaignProcessResponse campaignProcessResponse) {
        BigDecimal newTotal = campaignProcessResponse.getTotal().multiply(BigDecimal.valueOf(0.80));
        campaignProcessResponse.setTotal(newTotal);
    }

    private void isCampaignValidAndNotExpired(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException("Campaign not found with id: " + id));

        boolean isExpired = campaign.getExpiration().before(new Date());
        if (isExpired) {
            throw new CampaignNotApplicableException("Campaign expired with name: " + campaign.getName());
        }
    }
}
