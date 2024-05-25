package com.bit.saleservice.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MixedPayment {

    private BigDecimal cashAmount;
    private BigDecimal creditCardAmount;
}
