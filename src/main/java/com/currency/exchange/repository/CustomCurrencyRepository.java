package com.currency.exchange.repository;

import com.currency.exchange.model.CustomCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomCurrencyRepository extends JpaRepository<CustomCurrency, String> {
}
