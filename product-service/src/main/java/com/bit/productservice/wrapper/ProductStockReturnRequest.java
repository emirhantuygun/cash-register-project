package com.bit.productservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ProductStockReturnRequest {

    @NotNull
    private Long id;

    @Min(1)
    private int returnedQuantity;
}
