package com.bit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MixedPayment {

    private BigDecimal cashAmount;
    private BigDecimal creditCardAmount;
}
