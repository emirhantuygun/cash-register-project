package com.bit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * This class represents a product response object.
 * It contains details about a product in a sale.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleProductResponse {

    private Long id;
    private Long productId;
    private String name;
    private String barcodeNumber;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private Long saleId;
}
