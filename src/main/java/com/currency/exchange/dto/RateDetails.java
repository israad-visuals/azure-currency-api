package com.currency.exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RateDetails(String source, BigDecimal unitsPerUsd, LocalDate rateDate,
                          Instant recordedAt, LocalDate validUntil) {
}
