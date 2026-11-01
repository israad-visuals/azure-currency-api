\set ON_ERROR_STOP on

-- Fictional demo valuation: 500 game credits per USD, not a market price.
INSERT INTO custom_currencies (code, name, units_per_usd, decimal_places, rate_date, valid_until, updated_at)
VALUES ('GAME_CREDITS', 'Fictional game credits', 500, 0, CURRENT_DATE, CURRENT_DATE + 30, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    units_per_usd = EXCLUDED.units_per_usd,
    decimal_places = EXCLUDED.decimal_places,
    rate_date = EXCLUDED.rate_date,
    valid_until = EXCLUDED.valid_until,
    updated_at = EXCLUDED.updated_at;

SELECT code, name, units_per_usd, rate_date, valid_until, updated_at FROM custom_currencies;
