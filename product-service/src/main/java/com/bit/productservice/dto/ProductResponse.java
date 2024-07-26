package com.bit.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This class represents a response object for product details.
 * It contains various attributes related to a product.
 *
 * @author Emirhan Tuygun
 */
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
