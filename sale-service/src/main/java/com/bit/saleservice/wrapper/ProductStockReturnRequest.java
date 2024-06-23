package com.bit.saleservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * This class represents a request for returning a product's stock.
 * It contains the product's ID and the quantity of stock to be returned.
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
