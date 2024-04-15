package com.bit.saleservice.initializer;

import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeCampaigns();
    }

    private void initializeCampaigns() {

        long hour = 3600000L;
        long day = 86400000L;
        long week = 604800000L;

        Campaign campaign_1 = Campaign.builder()
                .name("Spend $200, Save $50")
                .details("Spend $200 or more at our market and save $50 on your total purchase.")
                .expiration(new Date(System.currentTimeMillis() + hour))
                .build();

        Campaign campaign_2 = Campaign.builder()
                .name("Buy 2, Get 1 Free")
                .details("Customers who purchase two eligible items from our store will receive a third item of equal or lesser value at no additional cost.")
                .expiration(new Date(System.currentTimeMillis() + day))
                .build();

        Campaign campaign_3 = Campaign.builder()
                .name("20% Off Your Entire Purchase")
                .details("This campaign offers a discount of 20% off the total price of the entire purchase.")
                .expiration(new Date(System.currentTimeMillis() + week))
                .build();

        campaignRepository.save(campaign_1);
        campaignRepository.save(campaign_2);
        campaignRepository.save(campaign_3);
    }

}
