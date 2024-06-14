package com.bit.productservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class ProductStockReturnRequest {

    @NotNull
    private Long id;

    @Min(1)
    private int returnedQuantity;
}
