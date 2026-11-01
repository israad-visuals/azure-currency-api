package com.currency.exchange.service;

import com.currency.exchange.dto.CreateCustomCurrencyRequest;
import com.currency.exchange.dto.CustomCurrencyResponse;
import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.CustomCurrency;
import com.currency.exchange.repository.CustomCurrencyRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomCurrencySubmissionService {
    private final CustomCurrencyRepository repository;
    private final EntityManager entities;
    private final Clock clock;
    private Instant windowStart = Instant.EPOCH;
    private int submissionsInWindow;

    public CustomCurrencySubmissionService(CustomCurrencyRepository repository, EntityManager entities, Clock clock) {
        this.repository = repository;
        this.entities = entities;
        this.clock = clock;
    }

    @Transactional
    public synchronized CustomCurrencyResponse create(CreateCustomCurrencyRequest request) {
        String code = request.code() == null ? "" : request.code().trim().toUpperCase(Locale.ROOT);
        String name = request.name() == null ? "" : request.name().trim();
        BigDecimal units = request.unitsPerUsd();
        if (!code.matches("[A-Z][A-Z0-9_]{3,15}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Use a 4-16 character code starting with a letter: uppercase letters, digits and underscores only.");
        }
        if (name.isEmpty() || name.length() > 100 || name.chars().anyMatch(Character::isISOControl)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Enter a currency name with 1-100 characters.");
        }
        if (units == null || units.signum() <= 0 || units.stripTrailingZeros().scale() > 8 ||
                (long) units.precision() - units.scale() > 15) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Units per USD must be positive, with at most 15 integer digits and 8 decimal places.");
        }
        if (repository.existsById(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "That currency code already exists. Choose a different code.");
        }
        Instant now = clock.instant();
        if (!now.isBefore(windowStart.plusSeconds(60))) {
            windowStart = now;
            submissionsInWindow = 0;
        }
        if (submissionsInWindow >= 10) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Submissions are busy. Please try again in a minute.");
        }
        if (repository.count() >= 500) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "The demo currency catalog is full.");
        }
        LocalDate today = LocalDate.now(clock);
        CustomCurrency currency = new CustomCurrency(code, name, units, (short) 2, today,
                today.plusDays(30), now, true);
        // Persist inserts a new row; save/merge could overwrite a concurrent submission.
        entities.persist(currency);
        entities.flush();
        submissionsInWindow++;
        return new CustomCurrencyResponse(code, name, units, 2, today, today.plusDays(30), now, true,
                "USER_ENTERED");
    }
}
