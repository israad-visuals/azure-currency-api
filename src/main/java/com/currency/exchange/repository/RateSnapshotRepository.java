package com.currency.exchange.repository;

import com.currency.exchange.model.RateSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateSnapshotRepository extends JpaRepository<RateSnapshot, Long> {
}
