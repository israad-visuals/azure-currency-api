package com.currency.exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ConversionResponse(String from, String to, BigDecimal amount,
                                 BigDecimal exchangeRate, BigDecimal convertedAmount,
                                 LocalDate fromRateDate, LocalDate toRateDate, Instant fetchedAt) {
}
