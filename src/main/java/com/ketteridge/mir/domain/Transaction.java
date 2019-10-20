package com.ketteridge.mir.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representation of a transaction.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String date;
    private String description;
    private String amount;
    private String currency;

    @JsonIgnore
    public BigDecimal getAmountBD() {
        return new BigDecimal(amount);
    }

    public Transaction spend() {
        this.amount = getAmountBD().negate().toPlainString();
        return this;
    }
}
