package com.bit.saleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * This class represents a response object for a sale product.
 * It contains details about a product sold in a sale.
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
