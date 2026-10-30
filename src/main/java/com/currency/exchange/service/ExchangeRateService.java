package com.currency.exchange.service;

import com.currency.exchange.dto.ConversionResponse;
import com.currency.exchange.dto.RatesResponse;
import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.RateSnapshot;
import com.currency.exchange.model.RateQuote;
import com.currency.exchange.repository.RateSnapshotRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {
    private final RateSnapshotRepository repository;
    private final RatesProviderClient provider;
    private final Clock clock;
    private final Duration cacheDuration;

    public ExchangeRateService(RateSnapshotRepository repository, RatesProviderClient provider,
            Clock clock, @Value("${exchange.cache-duration}") Duration cacheDuration) {
        if (cacheDuration.isNegative() || cacheDuration.isZero()) {
            throw new IllegalArgumentException("The rate cache duration must be positive.");
        }
        this.repository = repository;
        this.provider = provider;
        this.clock = clock;
        this.cacheDuration = cacheDuration;
    }

    public ConversionResponse convert(BigDecimal amount, String from, String to) {
        if (amount == null || amount.signum() < 0 || amount.scale() > 8 ||
                (long) amount.precision() - amount.scale() > 15) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Amount must be non-negative, with at most 15 integer digits and 8 decimal places.");
        }
        String source = normalize(from);
        String target = normalize(to);
        RateSnapshot snapshot = loadRates();
        RateQuote sourceQuote = requireRate(snapshot, source);
        RateQuote targetQuote = requireRate(snapshot, target);
        BigDecimal sourceRate = sourceQuote.getRate();
        BigDecimal targetRate = targetQuote.getRate();
        BigDecimal exchangeRate = targetRate.divide(sourceRate, MathContext.DECIMAL128);
        BigDecimal result = amount.multiply(targetRate, MathContext.DECIMAL128)
                .divide(sourceRate, MathContext.DECIMAL128)
                .setScale(decimalPlaces(target), RoundingMode.HALF_UP);
        return new ConversionResponse(source, target, amount, exchangeRate, result,
                sourceQuote.getRateDate(), targetQuote.getRateDate(), snapshot.getFetchedAt());
    }

    public RatesResponse getRates(String base) {
        String currency = normalize(base);
        RateSnapshot snapshot = loadRates();
        RateQuote baseQuote = requireRate(snapshot, currency);
        BigDecimal baseRate = baseQuote.getRate();
        Map<String, RatesResponse.Rate> rates = new TreeMap<>();
        snapshot.getRates().forEach((code, rate) ->
                rates.put(code, new RatesResponse.Rate(rate.getRate().divide(baseRate, MathContext.DECIMAL128),
                        baseQuote.getRateDate(), rate.getRateDate())));
        return new RatesResponse(currency, rates, snapshot.getFetchedAt());
    }

    public List<String> getCurrencies() {
        return loadRates().getRates().keySet().stream().sorted().toList();
    }

    private synchronized RateSnapshot loadRates() {
        Instant now = clock.instant();
        RateSnapshot cached = repository.findById(1L).orElse(null);
        if (cached != null && cached.getFetchedAt().plus(cacheDuration).isAfter(now) &&
                !cached.getFetchedAt().isAfter(now)) {
            return cached;
        }
        RatesProviderClient.ProviderRates response = provider.fetchRates();
        RateSnapshot snapshot = cached == null ? new RateSnapshot() : cached;
        snapshot.setRates(response.rates());
        snapshot.setFetchedAt(clock.instant());
        return repository.save(snapshot);
    }

    private String normalize(String code) {
        if (code == null || !code.trim().matches("(?i)[a-z]{3}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Use a three-letter currency code, such as CAD or EUR.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private RateQuote requireRate(RateSnapshot snapshot, String currency) {
        RateQuote rate = snapshot.getRates().get(currency);
        if (rate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Unsupported currency. Use /api/currencies to see available codes.");
        }
        return rate;
    }

    private int decimalPlaces(String currency) {
        try {
            int digits = Currency.getInstance(currency).getDefaultFractionDigits();
            return digits < 0 ? 2 : digits;
        } catch (IllegalArgumentException error) {
            return 2;
        }
    }
}
