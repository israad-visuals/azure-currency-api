package com.currency.exchange.service;

import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.RateSnapshot;
import com.currency.exchange.model.RateQuote;
import java.time.LocalDate;
import com.currency.exchange.repository.RateSnapshotRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTests {
    private final Instant now = Instant.parse("2026-10-30T12:00:00Z");
    private RateSnapshotRepository repository;
    private RatesProviderClient provider;
    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        repository = mock(RateSnapshotRepository.class);
        provider = mock(RatesProviderClient.class);
        service = new ExchangeRateService(repository, provider,
                Clock.fixed(now, ZoneOffset.UTC), Duration.ofHours(1));
    }

    private RateSnapshot snapshot(Instant fetchedAt) {
        RateSnapshot snapshot = new RateSnapshot();
        snapshot.setFetchedAt(fetchedAt);
        snapshot.setRates(Map.of("USD", new RateQuote(BigDecimal.ONE, null),
                "CAD", quote("1.25"), "EUR", quote("0.90"), "JPY", quote("150"), "KWD", quote("0.307")));
        return snapshot;
    }

    private RateQuote quote(String value) {
        return new RateQuote(new BigDecimal(value), LocalDate.parse("2026-10-29"));
    }

    private void useFreshRates() {
        when(repository.findById(1L)).thenReturn(Optional.of(snapshot(now.minusSeconds(60))));
    }

    @Test
    void convertsBetweenTwoNonUsdCurrencies() {
        useFreshRates();
        var result = service.convert(new BigDecimal("100"), "CAD", "EUR");
        assertEquals(new BigDecimal("72.00"), result.convertedAmount());
        assertEquals(0, result.exchangeRate().compareTo(new BigDecimal("0.72")));
        assertEquals(LocalDate.parse("2026-10-29"), result.fromRateDate());
        verifyNoInteractions(provider);
    }

    @Test
    void convertsInTheReverseDirection() {
        useFreshRates();
        assertEquals(new BigDecimal("125.00"),
                service.convert(new BigDecimal("90"), "EUR", "CAD").convertedAmount());
    }

    @Test
    void roundsToTheTargetCurrencyMinorUnits() {
        useFreshRates();
        assertEquals(new BigDecimal("152"),
                service.convert(new BigDecimal("1.01"), "USD", "JPY").convertedAmount());
        assertEquals(new BigDecimal("0.307"),
                service.convert(BigDecimal.ONE, "USD", "KWD").convertedAmount());
    }

    @Test
    void normalizesCodesAndHandlesSameCurrencyAndZero() {
        useFreshRates();
        var result = service.convert(new BigDecimal("42.12"), " cad ", "cad");
        assertEquals("CAD", result.from());
        assertEquals(new BigDecimal("42.12"), result.convertedAmount());
        assertEquals(new BigDecimal("0.00"),
                service.convert(BigDecimal.ZERO, "USD", "EUR").convertedAmount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0.000000001", "1000000000000000", "1e2147483647"})
    void rejectsInvalidAmountsBeforeAccessingRates(String amount) {
        ApiException error = assertThrows(ApiException.class,
                () -> service.convert(new BigDecimal(amount), "USD", "CAD"));
        assertEquals(400, error.getStatus().value());
        verifyNoInteractions(provider, repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "CA", "CADDD", "12!"})
    void rejectsMalformedCurrencyCodes(String code) {
        assertThrows(ApiException.class, () -> service.convert(BigDecimal.ONE, code, "CAD"));
        verifyNoInteractions(provider, repository);
    }

    @Test
    void rejectsUnsupportedCurrencies() {
        useFreshRates();
        ApiException error = assertThrows(ApiException.class,
                () -> service.convert(BigDecimal.ONE, "USD", "ZZZ"));
        assertEquals(400, error.getStatus().value());
    }

    @Test
    void returnsRatesRelativeToTheRequestedBase() {
        useFreshRates();
        var result = service.getRates("CAD");
        assertEquals("CAD", result.base());
        assertEquals(0, result.rates().get("CAD").rate().compareTo(BigDecimal.ONE));
        assertEquals(0, result.rates().get("USD").rate().compareTo(new BigDecimal("0.8")));
    }

    @Test
    void listsOnlySupportedCodesInOrder() {
        useFreshRates();
        assertEquals(java.util.List.of("CAD", "EUR", "JPY", "KWD", "USD"), service.getCurrencies());
    }

    @Test
    void fetchesAndPersistsRatesOnFirstRequest() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        when(provider.fetchRates()).thenReturn(new RatesProviderClient.ProviderRates(
                snapshot(now).getRates()));
        when(repository.save(any(RateSnapshot.class))).thenAnswer(call -> call.getArgument(0));
        var result = service.convert(BigDecimal.TEN, "USD", "CAD");
        assertEquals(new BigDecimal("12.50"), result.convertedAmount());
        assertEquals(now, result.fetchedAt());
        verify(repository).save(argThat(value -> value.getId() == 1L && value.getRates().size() == 5));
    }

    @Test
    void refreshesAtTheCacheExpiryBoundary() {
        when(repository.findById(1L)).thenReturn(Optional.of(snapshot(now.minusSeconds(3600))));
        when(provider.fetchRates()).thenReturn(new RatesProviderClient.ProviderRates(
                snapshot(now).getRates()));
        when(repository.save(any(RateSnapshot.class))).thenAnswer(call -> call.getArgument(0));
        service.getRates("USD");
        verify(provider).fetchRates();
    }

    @Test
    void doesNotSilentlyUseExpiredRatesWhenTheProviderFails() {
        when(repository.findById(1L)).thenReturn(Optional.of(snapshot(now.minusSeconds(7200))));
        when(provider.fetchRates()).thenThrow(new ApiException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Rates unavailable"));
        assertThrows(ApiException.class, () -> service.getRates("USD"));
        verify(repository, never()).save(any());
    }
}
