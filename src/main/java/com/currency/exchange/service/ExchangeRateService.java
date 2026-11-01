package com.currency.exchange.service;

import com.currency.exchange.dto.ConversionResponse;
import com.currency.exchange.dto.CustomCurrencyResponse;
import com.currency.exchange.dto.RateDetails;
import com.currency.exchange.dto.RatesResponse;
import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.CustomCurrency;
import com.currency.exchange.model.RateSnapshot;
import com.currency.exchange.model.RateQuote;
import com.currency.exchange.repository.CustomCurrencyRepository;
import com.currency.exchange.repository.RateSnapshotRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {
    private final RateSnapshotRepository repository;
    private final CustomCurrencyRepository customCurrencies;
    private final RatesProviderClient provider;
    private final Clock clock;
    private final Duration cacheDuration;

    public ExchangeRateService(RateSnapshotRepository repository, CustomCurrencyRepository customCurrencies,
            RatesProviderClient provider, Clock clock,
            @Value("${exchange.cache-duration}") Duration cacheDuration) {
        if (cacheDuration.isNegative() || cacheDuration.isZero()) {
            throw new IllegalArgumentException("The rate cache duration must be positive.");
        }
        this.repository = repository;
        this.customCurrencies = customCurrencies;
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
        CustomCurrency sourceCustom = findCustom(source);
        CustomCurrency targetCustom = source.equals(target) ? sourceCustom : findCustom(target);
        RateSnapshot snapshot = needsMarketRate(source) || needsMarketRate(target) ? loadRates() : null;
        RateDetails sourceQuote = resolve(source, sourceCustom, snapshot);
        RateDetails targetQuote = resolve(target, targetCustom, snapshot);
        BigDecimal sourceRate = sourceQuote.unitsPerUsd();
        BigDecimal targetRate = targetQuote.unitsPerUsd();

        // Both rates mean units per USD: divide into USD, then multiply into the target.
        BigDecimal usdAmount = amount.divide(sourceRate, MathContext.DECIMAL128);
        BigDecimal exchangeRate = targetRate.divide(sourceRate, MathContext.DECIMAL128);
        BigDecimal result = amount.multiply(targetRate, MathContext.DECIMAL128)
                .divide(sourceRate, MathContext.DECIMAL128)
                .setScale(targetCustom == null ? decimalPlaces(target) : targetCustom.getDecimalPlaces(),
                        RoundingMode.HALF_UP);
        return new ConversionResponse(source, target, amount, exchangeRate, result,
                sourceQuote.rateDate(), targetQuote.rateDate(), snapshot == null ? null : snapshot.getFetchedAt(),
                "USD", usdAmount, sourceQuote, targetQuote);
    }

    public RatesResponse getRates(String base) {
        String currency = normalize(base);
        CustomCurrency customBase = findCustom(currency);
        RateSnapshot snapshot = loadRates();
        RateDetails baseQuote = resolve(currency, customBase, snapshot);
        Map<String, RateDetails> quotes = new TreeMap<>();
        snapshot.getRates().forEach((code, value) -> quotes.put(code, marketQuote(snapshot, code)));
        customCurrencies.findAll().stream().filter(this::isActive).forEach(value ->
                quotes.put(value.getCode(), manualQuote(value)));
        quotes.put(currency, baseQuote);
        Map<String, RatesResponse.Rate> rates = new TreeMap<>();
        quotes.forEach((code, quote) -> rates.put(code, new RatesResponse.Rate(
                quote.unitsPerUsd().divide(baseQuote.unitsPerUsd(), MathContext.DECIMAL128),
                baseQuote.rateDate(), quote.rateDate(), baseQuote, quote)));
        return new RatesResponse(currency, rates, snapshot.getFetchedAt());
    }

    public List<String> getCurrencies() {
        TreeSet<String> codes = new TreeSet<>(loadRates().getRates().keySet());
        customCurrencies.findAll().stream().filter(this::isActive).forEach(value -> codes.add(value.getCode()));
        return List.copyOf(codes);
    }

    public List<CustomCurrencyResponse> getCustomCurrencies() {
        return customCurrencies.findAll().stream().sorted(Comparator.comparing(CustomCurrency::getCode))
                .map(value -> new CustomCurrencyResponse(value.getCode(), value.getName(), value.getUnitsPerUsd(),
                        value.getDecimalPlaces(), value.getRateDate(), value.getValidUntil(),
                        value.getUpdatedAt(), isActive(value), value.isPublicSubmission() ? "USER_ENTERED" : "MANUAL")).toList();
    }

    private CustomCurrency findCustom(String code) {
        if (code.length() == 3) {
            return null;
        }
        CustomCurrency currency = customCurrencies.findById(code).orElseThrow(this::unsupported);
        if (!isActive(currency)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The configured rate for " + code + " is outside its validity dates. Ask the operator to update it.");
        }
        return currency;
    }

    private boolean isActive(CustomCurrency currency) {
        LocalDate today = LocalDate.now(clock);
        return !currency.getRateDate().isAfter(today) && !currency.getValidUntil().isBefore(today);
    }

    private boolean needsMarketRate(String code) {
        return code.length() == 3 && !"USD".equals(code);
    }

    private RateDetails resolve(String code, CustomCurrency custom, RateSnapshot snapshot) {
        if (custom != null) {
            return manualQuote(custom);
        }
        if ("USD".equals(code) && snapshot == null) {
            return new RateDetails("USD_REFERENCE", BigDecimal.ONE, null, null, null);
        }
        return marketQuote(snapshot, code);
    }

    private RateDetails manualQuote(CustomCurrency currency) {
        return new RateDetails(currency.isPublicSubmission() ? "USER_ENTERED" : "MANUAL", currency.getUnitsPerUsd(), currency.getRateDate(),
                currency.getUpdatedAt(), currency.getValidUntil());
    }

    private RateDetails marketQuote(RateSnapshot snapshot, String code) {
        RateQuote quote = snapshot.getRates().get(code);
        if (quote == null) {
            throw unsupported();
        }
        return new RateDetails("USD".equals(code) ? "USD_REFERENCE" : "FRANKFURTER", quote.getRate(),
                quote.getRateDate(), snapshot.getFetchedAt(), null);
    }

    private ApiException unsupported() {
        return new ApiException(HttpStatus.BAD_REQUEST,
                "Unsupported currency. Use /api/currencies for available codes and /api/custom-currencies for configured rates.");
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
        if (code == null || !code.trim().matches("(?i)[a-z][a-z0-9_]{2,15}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Use a three-letter market code or a 4-16 character configured code, such as GAME_CREDITS.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
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
