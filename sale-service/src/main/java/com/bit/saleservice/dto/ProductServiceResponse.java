package com.bit.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductServiceResponse {

    private Long id;
    private String name;
    private String description;
    private String barcodeNumber;
    private BigDecimal price;
}