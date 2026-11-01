package com.currency.exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public record RatesResponse(String base, Map<String, Rate> rates, Instant fetchedAt) {
    public record Rate(BigDecimal rate, LocalDate baseRateDate, LocalDate quoteRateDate,
                       RateDetails baseQuote, RateDetails quote) {
    }
}
