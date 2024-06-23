package com.bit.productservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * This class represents a request for creating or updating a product.
 * It contains fields for product name, description, stock quantity, and price.
 * The class is annotated with Lombok annotations to generate boilerplate code.
 *
 * @author Emirhan Tuygun
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required and should not be blank!")
    private String name;
    private String description;

    @PositiveOrZero(message = "Stock Quantity must be a positive number!")
    private Integer stockQuantity;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be a positive number!")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid decimal number!")
    private BigDecimal price;
}
