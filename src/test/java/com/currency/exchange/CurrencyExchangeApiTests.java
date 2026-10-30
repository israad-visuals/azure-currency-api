package com.currency.exchange;

import com.currency.exchange.exception.ApiException;
import com.currency.exchange.repository.RateSnapshotRepository;
import com.currency.exchange.service.RatesProviderClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.currency.exchange.model.RateQuote;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CurrencyExchangeApiTests {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private RateSnapshotRepository repository;
    @MockitoBean
    private RatesProviderClient provider;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        when(provider.fetchRates()).thenReturn(new RatesProviderClient.ProviderRates(
                Map.of("USD", new RateQuote(BigDecimal.ONE, null),
                        "CAD", new RateQuote(new BigDecimal("1.25"), LocalDate.parse("2026-10-29")),
                        "EUR", new RateQuote(new BigDecimal("0.90"), LocalDate.parse("2026-10-28")))));
    }

    @Test
    void convertsThroughTheHttpEndpointAndReusesTheStoredSnapshot() throws Exception {
        mvc.perform(get("/api/convert").param("from", "CAD").param("to", "EUR").param("amount", "100"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.convertedAmount").value(72.0))
                .andExpect(jsonPath("$.from").value("CAD")).andExpect(jsonPath("$.fromRateDate").value("2026-10-29"))
                .andExpect(jsonPath("$.toRateDate").value("2026-10-28"));
        mvc.perform(get("/api/convert").param("from", "EUR").param("to", "CAD").param("amount", "90"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.convertedAmount").value(125.0));
        verify(provider, times(1)).fetchRates();
        assertEquals(1, repository.count());
        assertEquals(3, repository.findById(1L).orElseThrow().getRates().size());
    }

    @Test
    void returnsRatesForAnySupportedBaseAndListsCurrencies() throws Exception {
        mvc.perform(get("/api/rates").param("base", "CAD"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rates.USD.rate").value(0.8));
        mvc.perform(get("/api/currencies"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0]").value("CAD"));
    }

    @Test
    void rejectsBadAndMissingParameters() throws Exception {
        mvc.perform(get("/api/convert").param("from", "USD").param("to", "EUR").param("amount", "-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/convert").param("from", "USD").param("to", "EUR"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/convert").param("from", "USD").param("to", "EUR").param("amount", "NaN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnUnsupportedCurrency() throws Exception {
        mvc.perform(get("/api/convert").param("from", "ZZZ").param("to", "EUR").param("amount", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsAControlledErrorIfTheProviderIsUnavailable() throws Exception {
        when(provider.fetchRates()).thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "Exchange rates are temporarily unavailable."));
        mvc.perform(get("/api/rates"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("Exchange rates are temporarily unavailable."));
    }

    @Test
    void removesManualRateChangesAndLegacyEndpoints() throws Exception {
        mvc.perform(post("/api/rates/update").param("currencyCode", "USD").param("rate", "100"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/rates/all"))
                .andExpect(status().isNotFound());
    }

    @Test
    void allowsPublicReadRequestsButRejectsCrossOriginWrites() throws Exception {
        mvc.perform(options("/api/convert").header("Origin", "https://example.net")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "*"));
        mvc.perform(options("/api/convert").header("Origin", "https://example.net")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
    @Test
    void exposesOnlyStatusForLivenessAndDatabaseReadiness() throws Exception {
        mvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        mvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
        verifyNoInteractions(provider);
    }

    @Test
    void keepsManagementConfigurationAndMetricsPrivate() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isNotFound());
    }

}
