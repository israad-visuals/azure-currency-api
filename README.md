# Currency Exchange API

A Java API for converting between supported currencies, with PostgreSQL rate caching and a container setup prepared for Azure hosting.

| Project at a glance | |
| --- | --- |
| Application | Currency conversion, rate tables and supported currency lookup. |
| Local environment | Java 17 API and PostgreSQL 16 containers. |
| Validation | 42 automated tests and 14 HTTP smoke checks passed. |
| Operations | Database migration, health probes, restart and outage recovery checks. |

[My contribution](#my-contribution) · [Architecture](#architecture) · [Results](#results) · [Documentation](#documentation)

## Project goal

I expanded an earlier exchange-rate API to support conversion between different currency pairs. I also wanted a repeatable environment where I could check database setup, credential handling and application recovery before moving the workload to Azure.

## My contribution

- Reworked the conversion flow to use a shared rate table, decimal calculations and currency-specific rounding.
- Added a PostgreSQL cache and versioned SQL migration so the database setup is part of the project.
- Packaged the API and database with Docker Compose, using a non-root application container and separate database credentials.
- Added health probes and checked how the application behaves during a restart and a database outage.

## Architecture

**Client → Currency API → PostgreSQL cache**

When the cache needs refreshing, the API retrieves rates from [Frankfurter](https://frankfurter.dev/) over HTTPS. Requests then reuse that snapshot for one hour. Each conversion returns the source and target rate dates, plus the snapshot fetch time.

The API is available only on the local loopback address in the Compose setup. PostgreSQL has no published host port. Passwords are generated locally and mounted as secret files; they are excluded from Git and the Docker build.

[Read the architecture and design decisions](docs/architecture.md)

## API endpoints

| Request | Purpose |
| --- | --- |
| `GET /api/convert?from=CAD&to=EUR&amount=100` | Convert an amount. |
| `GET /api/rates?base=CAD` | Return the cached rate table for a chosen base. |
| `GET /api/currencies` | List supported currency codes. |

Coverage follows the provider's rate feed. Amounts must be non-negative, with up to 15 integer digits and 8 decimal places. The API calculates reference amounts; it does not transfer money.

## Results

The local container run passed all 14 HTTP smoke checks against PostgreSQL and the live rate feed. The cached snapshot survived an API restart. Stopping PostgreSQL changed readiness to `503` while liveness stayed `200`; readiness returned to `200` after the database restarted.

The Docker build also passed all 42 automated tests, covering calculations, rounding, input validation, provider responses, caching and HTTP behaviour. Live integration exposed a valid USD reference row that the parser rejected; I corrected the handling and added regression coverage.

[View the verification results and evidence](docs/verification.md)

## Documentation

| Page | Contents |
| --- | --- |
| [Architecture](docs/architecture.md) | Request flow, component choices, access controls and failure behaviour. |
| [Local setup](docs/local-development.md) | Start, verify, troubleshoot and stop the environment. |
| [Verification](docs/verification.md) | Recorded checks, observations and the provider integration fix. |

The next engineering milestone is the Azure hosting configuration: a small container deployment, database connectivity and scoped identity access, with a cost and cleanup plan before provisioning.
