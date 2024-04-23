package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequest {

    private String cashier;
    private String paymentMethod;
    private List<Long> campaignIds;
    private List<ProductRequest> products;
}
