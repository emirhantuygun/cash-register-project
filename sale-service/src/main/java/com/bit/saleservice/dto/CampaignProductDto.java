package com.bit.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignProductDto {

    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
