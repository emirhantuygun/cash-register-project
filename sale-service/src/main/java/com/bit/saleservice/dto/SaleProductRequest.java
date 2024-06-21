package com.bit.saleservice.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleProductRequest {

    private Long id;

    @PositiveOrZero(message = "Quantity must be a positive number!")
    private Integer quantity;
}
