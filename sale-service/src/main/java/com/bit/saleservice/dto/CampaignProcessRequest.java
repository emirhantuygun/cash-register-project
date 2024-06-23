package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * This class represents a request for processing a list of campaigns and products.
 * It contains the necessary data to perform the campaign processing operation.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignProcessRequest {

    private List<Long> campaignIds;
    private List<Product> products;
    private BigDecimal total;
}
