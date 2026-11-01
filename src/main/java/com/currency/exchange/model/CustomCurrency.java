package com.currency.exchange.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "custom_currencies")
public class CustomCurrency {
    @Id
    @Column(length = 16)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal unitsPerUsd;

    @Column(nullable = false)
    private short decimalPlaces;

    @Column(nullable = false)
    private LocalDate rateDate;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    private Instant updatedAt;

    public CustomCurrency() {
    }

    public CustomCurrency(String code, String name, BigDecimal unitsPerUsd, short decimalPlaces,
            LocalDate rateDate, LocalDate validUntil, Instant updatedAt) {
        this.code = code;
        this.name = name;
        this.unitsPerUsd = unitsPerUsd;
        this.decimalPlaces = decimalPlaces;
        this.rateDate = rateDate;
        this.validUntil = validUntil;
        this.updatedAt = updatedAt;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public BigDecimal getUnitsPerUsd() { return unitsPerUsd; }
    public short getDecimalPlaces() { return decimalPlaces; }
    public LocalDate getRateDate() { return rateDate; }
    public LocalDate getValidUntil() { return validUntil; }
    public Instant getUpdatedAt() { return updatedAt; }
}
