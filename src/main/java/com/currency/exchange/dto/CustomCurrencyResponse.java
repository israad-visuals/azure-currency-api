package com.currency.exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CustomCurrencyResponse(String code, String name, BigDecimal unitsPerUsd,
                                     int decimalPlaces, LocalDate rateDate,
                                     LocalDate validUntil, Instant updatedAt, boolean active) {
}
