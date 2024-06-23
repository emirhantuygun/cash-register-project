package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

/**
 * This class represents the response of a campaign processing operation.
 * It contains a list of products and the total amount of the campaign.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignProcessResponse {

    private List<Product> products;
    private BigDecimal total;
}
