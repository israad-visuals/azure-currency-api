package com.currency.exchange.controller;

import com.currency.exchange.dto.ConversionResponse;
import com.currency.exchange.dto.CreateCustomCurrencyRequest;
import com.currency.exchange.service.CustomCurrencySubmissionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityExistsException;
import org.hibernate.exception.ConstraintViolationException;
import com.currency.exchange.exception.ApiException;
import com.currency.exchange.dto.CustomCurrencyResponse;
import com.currency.exchange.dto.RatesResponse;
import com.currency.exchange.service.ExchangeRateService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExchangeRateController {
    private final ExchangeRateService service;

    private final CustomCurrencySubmissionService submissions;

    public ExchangeRateController(ExchangeRateService service, CustomCurrencySubmissionService submissions) {
        this.service = service;
        this.submissions = submissions;
    }

    @GetMapping("/convert")
    public ConversionResponse convert(@RequestParam BigDecimal amount,
                                      @RequestParam String from, @RequestParam String to) {
        return service.convert(amount, from, to);
    }

    @GetMapping("/rates")
    public RatesResponse rates(@RequestParam(defaultValue = "USD") String base) {
        return service.getRates(base);
    }

    @GetMapping("/currencies")
    public List<String> currencies() {
        return service.getCurrencies();
    }

    @PostMapping("/custom-currencies")
    public ResponseEntity<CustomCurrencyResponse> createCurrency(@RequestBody CreateCustomCurrencyRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(submissions.create(request));
        } catch (DataIntegrityViolationException | EntityExistsException | ConstraintViolationException error) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "That currency code could not be added. It may already exist; choose a different code.");
        }
    }

    @GetMapping("/custom-currencies")
    public List<CustomCurrencyResponse> customCurrencies() {
        return service.getCustomCurrencies();
    }
}
