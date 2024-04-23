package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
public class CampaignProcessResult {

    List<Campaign> campaigns;
    List<Product> products;
    BigDecimal totalWithCampaign;
}
