package com.bit.productservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductStockCheckRequest {
    private Long id;
    private int requestedQuantity;
}
