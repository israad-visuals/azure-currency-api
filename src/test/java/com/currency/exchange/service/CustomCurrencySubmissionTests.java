package com.currency.exchange.service;

import com.currency.exchange.dto.CreateCustomCurrencyRequest;
import com.currency.exchange.exception.ApiException;
import com.currency.exchange.repository.CustomCurrencyRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomCurrencySubmissionTests {
    @Test
    void limitsSubmissionsAndAllowsAnotherWindow() {
        var repository = mock(CustomCurrencyRepository.class);
        var entities = mock(EntityManager.class);
        var clock = mock(Clock.class);
        var now = Instant.parse("2026-11-01T12:00:00Z");
        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        var service = new CustomCurrencySubmissionService(repository, entities, clock);
        for (int i = 0; i < 10; i++) {
            service.create(new CreateCustomCurrencyRequest("POINTS_" + i, "Points", new BigDecimal("100")));
        }
        ApiException error = assertThrows(ApiException.class, () -> service.create(
                new CreateCustomCurrencyRequest("MORE_POINTS", "More points", new BigDecimal("100"))));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, error.getStatus());
        when(clock.instant()).thenReturn(now.plusSeconds(60));
        assertEquals("MORE_POINTS", service.create(
                new CreateCustomCurrencyRequest("MORE_POINTS", "More points", new BigDecimal("100"))).code());
    }

    @Test
    void boundsTheDemoCatalogWithoutWriting() {
        var repository = mock(CustomCurrencyRepository.class);
        var entities = mock(EntityManager.class);
        when(repository.count()).thenReturn(500L);
        var service = new CustomCurrencySubmissionService(repository, entities,
                Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC));
        ApiException error = assertThrows(ApiException.class, () -> service.create(
                new CreateCustomCurrencyRequest("MY_POINTS", "My points", new BigDecimal("100"))));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatus());
        verifyNoInteractions(entities);
    }
}
