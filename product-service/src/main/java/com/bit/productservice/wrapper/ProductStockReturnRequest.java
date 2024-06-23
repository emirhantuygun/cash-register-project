package com.bit.productservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * This class represents a request for returning product stock.
 * It contains the product ID and the quantity to be returned.
 *
 * @author Emirhan Tuygun
 */
@Data
@AllArgsConstructor
public class ProductStockReturnRequest {

    @NotNull
    private Long id;

    @Min(1)
    private int returnedQuantity;
}
