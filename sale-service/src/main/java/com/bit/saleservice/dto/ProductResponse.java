package com.bit.saleservice.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String barcodeNumber;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private Long saleId;
}
