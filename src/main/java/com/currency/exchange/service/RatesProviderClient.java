package com.currency.exchange.service;

import com.currency.exchange.exception.ApiException;
import com.currency.exchange.model.RateQuote;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RatesProviderClient {
    private final RestClient client;

    public RatesProviderClient(RestClient.Builder builder,
            @Value("${exchange.api.base-url}") URI baseUrl) {
        if (!"https".equalsIgnoreCase(baseUrl.getScheme())) {
            throw new IllegalArgumentException("Configure an HTTPS provider URL.");
        }
        this.client = builder.baseUrl(baseUrl.toString()).build();
    }

    public ProviderRates fetchRates() {
        ProviderRate[] response;
        try {
            response = client.get().uri("/rates?base=USD").retrieve().body(ProviderRate[].class);
        } catch (RestClientException error) {
            throw unavailable();
        }
        if (response == null || response.length == 0) {
            throw unavailable();
        }
        Map<String, RateQuote> rates = new HashMap<>();
        rates.put("USD", new RateQuote(BigDecimal.ONE, null));
        for (ProviderRate row : response) {
            if (row == null || !"USD".equals(row.base()) || row.quote() == null ||
                    !row.quote().matches("[A-Z]{3}") || row.rate() == null || row.rate().signum() <= 0 ||
                    row.rate().scale() > 18 || (long) row.rate().precision() - row.rate().scale() > 20 ||
                    row.date() == null) {
                throw unavailable();
            }
            try {
                RateQuote quote = new RateQuote(row.rate(), LocalDate.parse(row.date()));
                if (rates.putIfAbsent(row.quote(), quote) != null) {
                    throw unavailable();
                }
            } catch (DateTimeParseException error) {
                throw unavailable();
            }
        }
        return new ProviderRates(rates);
    }

    private ApiException unavailable() {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "Exchange rates are temporarily unavailable. Please try again later.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProviderRate(String date, String base, String quote, BigDecimal rate) {
    }

    public record ProviderRates(Map<String, RateQuote> rates) {
    }
}
