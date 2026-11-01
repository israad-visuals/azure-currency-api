package com.currency.exchange.dto;

import java.math.BigDecimal;

public record CreateCustomCurrencyRequest(String code, String name, BigDecimal unitsPerUsd) {
}
