# Currency Exchange API

A Java REST API that converts between supported currencies, such as CAD, USD, EUR and JPY. Exchange rates come from [Frankfurter](https://frankfurter.dev/) and are cached in PostgreSQL so requests can reuse the same rate table.

## How it works

The provider returns a rate table based on USD. To convert between any two supported currencies, I divide the target currency's rate by the source currency's rate, then multiply by the amount. Calculations use `BigDecimal`; the result is rounded to the target currency's standard decimal places.

The response includes the dates of the source and target rates, plus the time the application fetched the table. Different currencies can have different publication dates. USD is the reference value of 1, so its publication date is null. The cache lasts one hour by default. When it expires, the next request refreshes it. If the provider is unavailable at that point, the API returns `503` instead of silently presenting an expired table as current.

## Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| GET | `/api/convert?from=CAD&to=EUR&amount=100` | Convert an amount between two supported currencies. |
| GET | `/api/rates?base=CAD` | Get the current cached rate table relative to a chosen base. |
| GET | `/api/currencies` | List the currency codes supported by the provider's rate table. |

Currency codes are case-insensitive. Amounts can be zero or positive, with up to 15 integer digits and 8 decimal places. Invalid inputs return `400`. The API calculates equivalent amounts; it does not transfer money.

## Technology

Java 17, Spring Boot 4.1, Spring Data JPA, PostgreSQL and Maven. Tests use synthetic provider responses and an isolated H2 database.

## Local setup

Create a separate PostgreSQL database named `currency_exchange`, then set these environment variables in your terminal or IDE before starting the application:

| Variable | Purpose |
| --- | --- |
| `DB_URL` | JDBC connection URL; defaults to `jdbc:postgresql://localhost:5432/currency_exchange`. |
| `DB_USERNAME` | Database application username. |
| `DB_PASSWORD` | Database application password. |
| `RATE_CACHE_DURATION` | Optional ISO-8601 duration; defaults to `PT1H`. |
| `PORT` | Optional HTTP port; defaults to `8080`. |

Use a new database for this version. The cache uses `rate_snapshots` and `snapshot_rates`; it does not use or migrate the previous manually entered rate table. Automatic schema updates are enabled for local development and can be controlled with `DB_DDL_AUTO`.

```sh
./mvnw spring-boot:run
```

## Checks

```sh
./mvnw verify
```

Tests cover cross-currency calculations, rounding, validation, cache refresh, provider failures and HTTP endpoints. They do not call the live provider or require production credentials.

## Configuration and security

Database credentials are read from environment variables. Frankfurter does not require an API key. Keep local credentials and `.env` files out of Git. A `.env` file is not automatically loaded by Spring Boot; configure the variables in your terminal or IDE.

The API has public read-only endpoints and no manual rate-update endpoint. Provider requests use HTTPS with connection and read timeouts. Cross-origin access is limited to GET requests. Hosting-level request limits and TLS belong in the deployment configuration.

Rates are reference data published by central banks and official sources through Frankfurter. They are not executable trading quotes. The available currencies are listed by `/api/currencies`; coverage is determined by the upstream feed.
