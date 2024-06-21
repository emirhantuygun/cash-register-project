package com.bit.saleservice.dto;

import com.bit.saleservice.entity.MixedPayment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
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
public class SaleRequest {

    @NotBlank(message = "Cashier is required and should not be blank!")
    private String cashier;

    @NotBlank(message = "Payment Method is required and should not be blank!")
    private String paymentMethod;
    private List<Long> campaignIds;
    private List<SaleProductRequest> products;

    @PositiveOrZero(message = "Cash must be a positive number!")
    private BigDecimal cash;
    private MixedPayment mixedPayment;
}
