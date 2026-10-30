package com.currency.exchange.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "rate_snapshots")
public class RateSnapshot {
    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Instant fetchedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "snapshot_rates", joinColumns = @JoinColumn(name = "snapshot_id"))
    @MapKeyColumn(name = "currency_code", length = 3)
    private Map<String, RateQuote> rates = new HashMap<>();

    public Long getId() {
        return id;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public Map<String, RateQuote> getRates() {
        return rates;
    }

    public void setRates(Map<String, RateQuote> rates) {
        this.rates = new HashMap<>(rates);
    }
}
