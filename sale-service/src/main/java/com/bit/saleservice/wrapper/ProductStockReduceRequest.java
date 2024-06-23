package com.bit.saleservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * This class represents a request to reduce the stock of a product.
 * It contains the product ID and the requested quantity to reduce.
 *
 * @author Emirhan Tuygun
 */
@Data
@AllArgsConstructor
public class ProductStockReduceRequest {

    @NotNull
    private Long id;

    @Min(1)
    private int requestedQuantity;
}
