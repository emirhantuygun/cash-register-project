package com.bit.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * This class represents a product in a campaign. It contains the price, quantity, and total price of the product.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignProductDto {

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
