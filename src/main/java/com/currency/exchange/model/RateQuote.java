package com.currency.exchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Embeddable
public class RateQuote {
    @Column(name = "rate", nullable = false, precision = 38, scale = 18)
    private BigDecimal rate;
    private LocalDate rateDate;

    public RateQuote() {
    }

    public RateQuote(BigDecimal rate, LocalDate rateDate) {
        this.rate = rate;
        this.rateDate = rateDate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }
}
