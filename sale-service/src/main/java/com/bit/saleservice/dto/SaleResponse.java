package com.bit.saleservice.dto;

import com.bit.saleservice.entity.Campaign;
import com.bit.saleservice.entity.Payment;
import com.bit.saleservice.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private Long id;
    private String cashier;
    private Date date;
    private String paymentMethod;
    private List<String> campaignNames;
    private List<ProductResponse> products;
    private BigDecimal cash;
    private BigDecimal change;
    private BigDecimal total;
    private BigDecimal totalWithCampaign;
}
