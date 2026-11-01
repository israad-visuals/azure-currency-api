CREATE TABLE custom_currencies (
    code VARCHAR(16) PRIMARY KEY CHECK (code ~ '^[A-Z][A-Z0-9_]{3,15}$'),
    name VARCHAR(100) NOT NULL,
    units_per_usd NUMERIC(38, 18) NOT NULL CHECK (units_per_usd > 0),
    decimal_places SMALLINT NOT NULL CHECK (decimal_places BETWEEN 0 AND 8),
    rate_date DATE NOT NULL,
    valid_until DATE NOT NULL CHECK (valid_until >= rate_date),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
