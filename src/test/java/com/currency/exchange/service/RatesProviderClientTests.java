package com.currency.exchange.service;

import com.currency.exchange.exception.ApiException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RatesProviderClientTests {
    private MockRestServiceServer server;
    private RatesProviderClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new RatesProviderClient(builder, URI.create("https://provider.example/v2"));
    }

    @Test
    void preservesDecimalPrecisionAndEachQuoteDate() {
        server.expect(requestTo("https://provider.example/v2/rates?base=USD"))
                .andRespond(withSuccess("""
                        [{"base":"USD","quote":"EUR","date":"2026-10-29",
                          "rate":0.123456789012345678,"extra":"ignored"},
                         {"base":"USD","quote":"CAD","date":"2026-10-28","rate":1.25}]
                        """, MediaType.APPLICATION_JSON));
        var response = client.fetchRates();
        assertEquals(new BigDecimal("0.123456789012345678"), response.rates().get("EUR").getRate());
        assertEquals(LocalDate.parse("2026-10-28"), response.rates().get("CAD").getRateDate());
        assertEquals(BigDecimal.ONE, response.rates().get("USD").getRate());
        assertNull(response.rates().get("USD").getRateDate());
        server.verify();
    }

    @Test
    void acceptsTheBaseCurrencyWhenReturnedByTheProvider() {
        server.expect(requestTo("https://provider.example/v2/rates?base=USD"))
                .andRespond(withSuccess("""
                        [{"base":"USD","quote":"USD","date":"2026-10-30","rate":1.0},
                         {"base":"USD","quote":"CAD","date":"2026-10-29","rate":1.25}]
                        """, MediaType.APPLICATION_JSON));
        var response = client.fetchRates();
        assertEquals(0, BigDecimal.ONE.compareTo(response.rates().get("USD").getRate()));
        assertEquals(LocalDate.parse("2026-10-30"), response.rates().get("USD").getRateDate());
        assertEquals(2, response.rates().size());
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "[]", "{}", "not-json",
            "[{\"base\":\"USD\",\"quote\":\"USD\",\"date\":\"2026-10-30\",\"rate\":2}]",
            "[{\"base\":\"USD\",\"quote\":\"USD\",\"date\":\"2026-10-30\",\"rate\":1},{\"base\":\"USD\",\"quote\":\"USD\",\"date\":\"2026-10-30\",\"rate\":1}]",
            "[{\"base\":\"EUR\",\"quote\":\"CAD\",\"date\":\"2026-10-29\",\"rate\":1}]",
            "[{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"2026-10-29\",\"rate\":0}]",
            "[{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"2026-10-29\",\"rate\":-1}]",
            "[{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"invalid\",\"rate\":1}]",
            "[{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"2026-10-29\",\"rate\":null}]",
            "[{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"2026-10-29\",\"rate\":1},{\"base\":\"USD\",\"quote\":\"CAD\",\"date\":\"2026-10-28\",\"rate\":2}]"
    })
    void rejectsInvalidProviderResponses(String body) {
        server.expect(requestTo("https://provider.example/v2/rates?base=USD"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        ApiException error = assertThrows(ApiException.class, client::fetchRates);
        assertEquals(503, error.getStatus().value());
        server.verify();
    }

    @Test
    void hidesProviderErrorDetails() {
        server.expect(requestTo("https://provider.example/v2/rates?base=USD"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("Internal provider details"));
        ApiException error = assertThrows(ApiException.class, client::fetchRates);
        assertEquals(503, error.getStatus().value());
        assertFalse(error.getMessage().contains("Internal provider details"));
    }

    @Test
    void requiresHttps() {
        assertThrows(IllegalArgumentException.class, () -> new RatesProviderClient(
                RestClient.builder(), URI.create("http://provider.example")));
    }
}
