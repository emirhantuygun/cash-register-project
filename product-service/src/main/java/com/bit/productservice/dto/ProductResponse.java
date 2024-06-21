package com.bit.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String barcodeNumber;
    private Integer stockQuantity;
    private BigDecimal price;
}
