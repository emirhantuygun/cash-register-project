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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required and should not be blank!")
    private String name;
    private String description;
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be a positive number!")
    @Digits(integer = 10, fraction = 2, message = "Price must be a valid decimal number!")
    private BigDecimal price;
}
