package com.currency.exchange.service;

import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.CustomCurrency;
import com.currency.exchange.model.RateQuote;
import com.currency.exchange.model.RateSnapshot;
import com.currency.exchange.repository.CustomCurrencyRepository;
import com.currency.exchange.repository.RateSnapshotRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomCurrencyConversionTests {
    private final Instant now = Instant.parse("2026-11-01T12:00:00Z");
    private RateSnapshotRepository snapshots;
    private CustomCurrencyRepository customs;
    private RatesProviderClient provider;
    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        snapshots = mock(RateSnapshotRepository.class);
        customs = mock(CustomCurrencyRepository.class);
        provider = mock(RatesProviderClient.class);
        service = new ExchangeRateService(snapshots, customs, provider,
                Clock.fixed(now, ZoneOffset.UTC), Duration.ofHours(1));
    }

    private CustomCurrency custom(String code, String units, short decimals) {
        return new CustomCurrency(code, "Fictional test units", new BigDecimal(units), decimals,
                LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"), now);
    }

    private void useCredits() {
        when(customs.findById("GAME_CREDITS")).thenReturn(Optional.of(custom("GAME_CREDITS", "500", (short) 0)));
    }

    private void useMarketRates() {
        RateSnapshot snapshot = new RateSnapshot();
        snapshot.setFetchedAt(now.minusSeconds(60));
        snapshot.setRates(Map.of("USD", new RateQuote(BigDecimal.ONE, null),
                "EUR", new RateQuote(new BigDecimal("0.90"), LocalDate.parse("2026-10-30"))));
        when(snapshots.findById(1L)).thenReturn(Optional.of(snapshot));
    }

    @Test
    void bridgesCustomUnitsThroughUsdIntoAMarketCurrency() {
        useCredits();
        useMarketRates();
        var result = service.convert(new BigDecimal("1000"), " game_credits ", "EUR");
        assertEquals(new BigDecimal("1.80"), result.convertedAmount());
        assertEquals(0, new BigDecimal("2").compareTo(result.usdAmount()));
        assertEquals("USD", result.bridgeCurrency());
        assertEquals("MANUAL", result.fromQuote().source());
        assertEquals("FRANKFURTER", result.toQuote().source());
        assertEquals(LocalDate.parse("2026-11-30"), result.fromQuote().validUntil());
        assertEquals(LocalDate.parse("2026-10-30"), result.toRateDate());
    }

    @Test
    void supportsTheReverseDirectionAndCustomRounding() {
        useCredits();
        useMarketRates();
        var result = service.convert(new BigDecimal("1.80"), "EUR", "GAME_CREDITS");
        assertEquals(new BigDecimal("1000"), result.convertedAmount());
        assertEquals("MANUAL", result.toQuote().source());
        assertEquals(new BigDecimal("1"), service.convert(new BigDecimal("0.001"),
                "USD", "GAME_CREDITS").convertedAmount());
    }

    @Test
    void customToUsdDoesNotNeedAnExternalRate() {
        useCredits();
        var result = service.convert(new BigDecimal("1000"), "GAME_CREDITS", "USD");
        assertEquals(new BigDecimal("2.00"), result.convertedAmount());
        assertEquals("USD_REFERENCE", result.toQuote().source());
        assertNull(result.fetchedAt());
        verifyNoInteractions(provider, snapshots);
    }

    @Test
    void supportsCustomToCustomAndSameCustomWithoutTheProvider() {
        useCredits();
        when(customs.findById("COMMUNITY_UNITS")).thenReturn(Optional.of(custom("COMMUNITY_UNITS", "250", (short) 2)));
        assertEquals(new BigDecimal("500.00"), service.convert(new BigDecimal("1000"),
                "GAME_CREDITS", "COMMUNITY_UNITS").convertedAmount());
        assertEquals(new BigDecimal("1000"), service.convert(new BigDecimal("1000"),
                "GAME_CREDITS", "GAME_CREDITS").convertedAmount());
        verifyNoInteractions(provider, snapshots);
    }

    @Test
    void readsOperatorUpdatesWithoutWaitingForTheMarketCacheToExpire() {
        useCredits();
        assertEquals(new BigDecimal("2.00"), service.convert(new BigDecimal("1000"),
                "GAME_CREDITS", "USD").convertedAmount());
        when(customs.findById("GAME_CREDITS")).thenReturn(Optional.of(custom("GAME_CREDITS", "1000", (short) 0)));
        assertEquals(new BigDecimal("1.00"), service.convert(new BigDecimal("1000"),
                "GAME_CREDITS", "USD").convertedAmount());
    }

    @Test
    void sameCurrencyUsesOneValuationEvenIfAnOperatorUpdatesItDuringTheRequest() {
        when(customs.findById("GAME_CREDITS")).thenReturn(
                Optional.of(custom("GAME_CREDITS", "500", (short) 0)),
                Optional.of(custom("GAME_CREDITS", "1000", (short) 0)));
        assertEquals(new BigDecimal("1000"), service.convert(new BigDecimal("1000"),
                "GAME_CREDITS", "GAME_CREDITS").convertedAmount());
        verify(customs, times(1)).findById("GAME_CREDITS");
    }

    @Test
    void aRateTableKeepsItsBaseRateAtOneDuringAnOperatorUpdate() {
        useCredits();
        useMarketRates();
        when(customs.findAll()).thenReturn(List.of(custom("GAME_CREDITS", "1000", (short) 0)));
        assertEquals(0, BigDecimal.ONE.compareTo(service.getRates("GAME_CREDITS")
                .rates().get("GAME_CREDITS").rate()));
    }

    @Test
    void rejectsAnUnconfiguredCodeBeforeCallingTheProvider() {
        var error = assertThrows(ApiException.class, () -> service.convert(BigDecimal.ONE, "UNKNOWN", "EUR"));
        assertEquals(400, error.getStatus().value());
        verifyNoInteractions(provider, snapshots);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-31", "2026-10-01"})
    void rejectsExpiredConfiguredRates(String validUntil) {
        CustomCurrency expired = new CustomCurrency("GAME_CREDITS", "Fictional test units", BigDecimal.TEN,
                (short) 2, LocalDate.parse("2026-10-01"), LocalDate.parse(validUntil), now);
        when(customs.findById("GAME_CREDITS")).thenReturn(Optional.of(expired));
        var error = assertThrows(ApiException.class, () -> service.convert(BigDecimal.ONE, "GAME_CREDITS", "EUR"));
        assertEquals(503, error.getStatus().value());
        verifyNoInteractions(provider, snapshots);
    }

    @Test
    void rejectsAFutureRateButAcceptsItsLastValidDay() {
        when(customs.findById("GAME_CREDITS")).thenReturn(Optional.of(new CustomCurrency("GAME_CREDITS",
                "Fictional test units", BigDecimal.TEN, (short) 2,
                LocalDate.parse("2026-11-02"), LocalDate.parse("2026-11-30"), now)));
        assertEquals(503, assertThrows(ApiException.class, () -> service.convert(BigDecimal.ONE,
                "GAME_CREDITS", "USD")).getStatus().value());
        when(customs.findById("GAME_CREDITS")).thenReturn(Optional.of(new CustomCurrency("GAME_CREDITS",
                "Fictional test units", BigDecimal.TEN, (short) 2,
                LocalDate.parse("2026-10-01"), LocalDate.parse("2026-11-01"), now)));
        assertEquals(new BigDecimal("1.00"), service.convert(BigDecimal.TEN, "GAME_CREDITS", "USD").convertedAmount());
    }

    @Test
    void listsAndQuotesActiveCustomCurrenciesAlongsideMarketCurrencies() {
        useCredits();
        useMarketRates();
        when(customs.findAll()).thenReturn(List.of(custom("GAME_CREDITS", "500", (short) 0)));
        assertEquals(List.of("EUR", "GAME_CREDITS", "USD"), service.getCurrencies());
        var rates = service.getRates("GAME_CREDITS");
        assertEquals(0, new BigDecimal("0.0018").compareTo(rates.rates().get("EUR").rate()));
        assertEquals("MANUAL", rates.rates().get("EUR").baseQuote().source());
        assertEquals(0, BigDecimal.ONE.compareTo(rates.rates().get("GAME_CREDITS").rate()));
    }

    @Test
    void customCatalogShowsExpiredEntriesWithoutClaimingTheyAreActive() {
        when(customs.findAll()).thenReturn(List.of(new CustomCurrency("OLD_UNITS", "Expired test units",
                BigDecimal.TEN, (short) 2, LocalDate.parse("2026-10-01"),
                LocalDate.parse("2026-10-31"), now)));
        assertFalse(service.getCustomCurrencies().get(0).active());
        useMarketRates();
        assertFalse(service.getCurrencies().contains("OLD_UNITS"));
        verifyNoInteractions(provider);
    }

    @Test
    void marketFailureDoesNotProduceAPartialCustomToMarketResult() {
        useCredits();
        when(provider.fetchRates()).thenThrow(new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Rates unavailable"));
        assertEquals(503, assertThrows(ApiException.class, () -> service.convert(BigDecimal.ONE,
                "GAME_CREDITS", "EUR")).getStatus().value());
    }
}
