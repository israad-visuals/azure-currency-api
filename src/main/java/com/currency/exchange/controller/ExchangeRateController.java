package com.currency.exchange.controller;

import com.currency.exchange.dto.ConversionResponse;
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

    public ExchangeRateController(ExchangeRateService service) {
        this.service = service;
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
}
