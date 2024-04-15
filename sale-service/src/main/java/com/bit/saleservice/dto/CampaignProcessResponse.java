package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CampaignProcessResponse {

    private List<Product> products;
    private BigDecimal total;
}
