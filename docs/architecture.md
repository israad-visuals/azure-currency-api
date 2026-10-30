# Architecture and design decisions

[Project overview](../README.md) · [Local setup](local-development.md) · [Verification results](verification.md)

I separated the API, database and rate provider so each part has a clear responsibility. The API handles validation and calculations, PostgreSQL stores the shared rate snapshot, and Frankfurter supplies the reference rates.

## Request flow

1. A client sends the amount, source currency and target currency to `/api/convert`.
2. The API validates the input and reads the rate snapshot from PostgreSQL.
3. If the snapshot is missing or more than one hour old, the API requests a new USD-based table from Frankfurter over HTTPS.
4. The API divides the target rate by the source rate, calculates the result and rounds it to the target currency's decimal places.
5. The response includes the converted amount, both rate publication dates and the snapshot fetch time.

The application supports the currencies returned by the provider's feed. It calculates reference amounts and does not move money or execute trades.

## Why I chose each component

| Requirement | Choice | Reason |
| --- | --- | --- |
| Convert between different currency pairs. | One USD-based rate table with cross-rate calculations. | Avoids keeping a separate conversion rule for each pair. |
| Keep decimal calculations consistent. | `BigDecimal` and currency-specific rounding. | Avoids binary floating-point errors and handles currencies such as JPY correctly. |
| Avoid a provider request for every conversion. | One-hour PostgreSQL cache. | Reuses the same snapshot across requests and application restarts. |
| Keep database setup repeatable. | Flyway migration and Hibernate schema validation. | Creates the cache tables from versioned SQL and detects schema mismatches. |
| Run the same application package in different environments. | Multi-stage Docker build with Java 17. | Runs the tests during the build and puts only the application and runtime in the final image. |
| Limit the application's access. | Non-root container and a separate database application role. | The API cannot create database users or databases, and its container filesystem is read-only. |
| Keep credentials out of source code and images. | Local secret files mounted through Compose. | Generates separate application and administrator passwords without putting their values in the Compose file or application environment. |
| Distinguish an application failure from a database outage. | Separate liveness and readiness endpoints. | Liveness checks the application; readiness also checks PostgreSQL. |
| Keep local work inexpensive and contained. | One API container and one PostgreSQL container. | Provides a complete local environment without allocating Azure resources. |

## Connections and credentials

The local API is published only on `127.0.0.1:8088`. PostgreSQL has no published host port; the API reaches it through the Compose network. Outbound HTTPS is used for rate retrieval.

The API receives only the application database password. The database administrator password is mounted only into PostgreSQL. The application role owns its cache tables and has schema creation permission for Flyway, with no superuser, role creation or database creation permissions.

Compose secrets are local files, not an encrypted vault. The setup script keeps their parent directories private and Git ignores `.local/`. The Docker build context includes only `pom.xml` and `src/`, excluding local credentials and Git history.

## Cache and failure behaviour

The cache is valid for one hour after retrieval; that is separate from each rate's publication date. Both dates remain visible in responses. When the provider includes the USD reference row, its rate must equal one. If it omits that row, the API supplies the reference value with a null publication date.

Provider connection and read timeouts are three and five seconds. If a refresh fails, the API returns a short `503` response instead of presenting an expired snapshot as current. A healthy cached snapshot can be reused without calling the provider.

Only health endpoints are exposed through Actuator, with internal component details hidden. Database failure makes readiness return `503` while liveness remains available. This gives a hosting platform separate signals for traffic routing and process health.

Cache refresh is synchronized within one application process. This local configuration uses one API instance; it does not implement a distributed refresh lock.
