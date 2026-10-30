CREATE TABLE rate_snapshots (
    id BIGINT PRIMARY KEY,
    fetched_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE snapshot_rates (
    snapshot_id BIGINT NOT NULL REFERENCES rate_snapshots(id),
    currency_code VARCHAR(3) NOT NULL,
    rate NUMERIC(38, 18) NOT NULL CHECK (rate > 0),
    rate_date DATE,
    PRIMARY KEY (snapshot_id, currency_code)
);
