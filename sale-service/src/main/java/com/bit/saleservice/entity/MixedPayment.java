package com.bit.saleservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Represents a mixed payment entity that consists of cash and credit card amounts.
 * This class is designed to be used as an embedded entity in JPA-based applications.
 *
 * @author Emirhan Tuygun
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MixedPayment {

    @Column(name = "cash_for_mixed")
    @PositiveOrZero(message = "Cash must be a positive number!")
    private BigDecimal cashAmount;

    @Column(name = "credit_card_for_mixed")
    @PositiveOrZero(message = "Cash must be a positive number!")
    private BigDecimal creditCardAmount;
}
