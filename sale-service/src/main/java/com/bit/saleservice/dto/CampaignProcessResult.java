package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class represents the result of processing a list of campaigns and products.
 * It contains the list of applied campaigns, the list of products, and the total price after applying the campaigns.
 *
 * @author Emirhan Tuygun
 */
@Getter
@AllArgsConstructor
public class CampaignProcessResult {

    List<Campaign> campaigns;
    List<Product> products;
    BigDecimal totalWithCampaign;
}
