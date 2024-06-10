package com.bit.productservice.wrapper;

import lombok.Data;

@Data
public class ProductStockCheckRequest {
    private Long id;
    private int requestedQuantity;
}
