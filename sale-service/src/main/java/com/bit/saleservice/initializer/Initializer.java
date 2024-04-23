package com.bit.saleservice.initializer;

import com.bit.saleservice.dto.CampaignProcessRequest;
import com.bit.saleservice.dto.CampaignProcessResponse;
import com.bit.saleservice.dto.CampaignProcessResult;
import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Payment;
import com.bit.saleservice.entity.Product;
import com.bit.saleservice.entity.Sale;
import com.bit.saleservice.repository.CampaignRepository;
import com.bit.saleservice.repository.ProductRepository;
import com.bit.saleservice.repository.SaleRepository;
import com.bit.saleservice.service.CampaignProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final CampaignRepository campaignRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CampaignProcessService campaignProcessService;


    @Override
    @Transactional
    public void run(String... args) {
        initializeCampaigns();
        initializeSales();
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

    private void initializeSales() {

        for (int i = 1; i <= 20; i++) {
            String cashier = String.format("Cashier %d", i);
            Date date = new Date(System.currentTimeMillis() + 86400000 * i);

            int paymentId = i % 4;
            Payment paymentMethod = Payment.values()[paymentId];

            Product product = Product.builder()
                    .name("Product " + i)
                    .barcodeNumber("0123456789" + i)
                    .quantity(i)
                    .price(BigDecimal.valueOf(i * 10))
                    .totalPrice(BigDecimal.valueOf(i * 10).multiply(BigDecimal.valueOf(i)))
                    .build();

            BigDecimal total = product.getTotalPrice();

            Sale sale = Sale.builder()
                    .cashier(cashier)
                    .date(date)
                    .paymentMethod(paymentMethod)
                    .campaigns(Collections.emptyList())
                    .total(total)
                    .totalWithCampaign(total)
                    .build();

            saleRepository.save(sale);
            product.setSale(sale);
            productRepository.save(product);
        }
    }

}
