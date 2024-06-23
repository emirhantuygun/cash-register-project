package com.bit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * This class represents a mixed payment, which includes both cash and credit card amounts.
 *
 * @author Emirhan Tuygun
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MixedPayment {

    private BigDecimal cashAmount;
    private BigDecimal creditCardAmount;
}
