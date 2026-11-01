# Architecture and design decisions

[Project overview](../README.md) · [Local setup](local-development.md) · [Verification results](verification.md)

I separated the API, database and rate provider so each part has a clear responsibility. The API handles validation and USD-bridge calculations. PostgreSQL stores operator-managed and visitor-submitted valuations separately from the provider cache, and Frankfurter supplies market reference rates.

## Request flow

1. A client sends an amount, source code and target code to `/api/convert`.
2. For a configured code, the API reads its valuation from `custom_currencies` and checks its validity dates in UTC. Three-letter codes are reserved for the provider and USD; custom codes use 4–16 uppercase letters, digits or underscores, starting with a letter.
3. If a market rate is needed, the API reads the USD-based provider snapshot. A missing or expired snapshot is refreshed over HTTPS. Custom-to-USD and custom-to-custom conversions need no provider request.
4. Both sources use the same direction: **units per US$1**. The calculation is `amount / source_units_per_usd * target_units_per_usd`.
5. The API rounds only the final amount, using market currency minor units or the custom currency's configured decimal places. The response includes the USD amount and both rate sources, dates, recorded timestamps and manual expiry dates.

Configured valuations are supplied by operators or visitors; their accuracy is not verified against an external market. Provider coverage determines which market targets are available. The API calculates reference amounts and does not move money or execute trades.

See the [custom currency guide](custom-currencies.md) for SQL and fictional examples.

## Why I chose each component

| Requirement | Choice | Reason |
| --- | --- | --- |
| Convert units absent from the market feed. | Separate custom valuations with units per USD. | An operator can add a currency without changing code or overwriting provider data. |
| Convert between different currency pairs. | USD as a shared bridge. | Avoids keeping a separate conversion rule for each pair. |
| Keep decimal calculations consistent. | `BigDecimal` and currency-specific rounding. | Avoids binary floating-point errors and handles currencies such as JPY correctly. |
| Avoid a provider request for every conversion. | One-hour PostgreSQL cache. | Reuses the same snapshot across requests and application restarts. |
| Keep database setup repeatable. | Flyway migrations and Hibernate schema validation. | V1 creates the provider cache; V2 adds custom valuations; V3 identifies visitor submissions. |
| Run the same application package in different environments. | Multi-stage Docker build with Java 17. | Runs the tests during the build and puts only the application and runtime in the final image. |
| Limit the application's access. | Non-root container and a separate database application role. | The API cannot create database users or databases, and its container filesystem is read-only. |
| Keep credentials out of source code and images. | Local secret files mounted through Compose. | Generates separate application and administrator passwords without putting their values in the Compose file or application environment. |
| Distinguish an application failure from a database outage. | Separate liveness and readiness endpoints. | Liveness checks the application; readiness also checks PostgreSQL. |
| Keep local work inexpensive and contained. | One API container and one PostgreSQL container. | Provides a complete local environment without allocating Azure resources. |

## Connections and credentials

The local API is published only on `127.0.0.1:8088`. PostgreSQL has no published host port; the API reaches it through the Compose network. Outbound HTTPS is used for rate retrieval.

The API receives only the application database password. The database administrator password is mounted only into PostgreSQL. The application role owns its cache tables and has schema creation permission for Flyway, with no superuser, role creation or database creation permissions.

Compose secrets are local files, not an encrypted vault. The setup script keeps their parent directories private and Git ignores `.local/`. The Docker build context includes only `pom.xml` and `src/`, excluding local credentials and Git history.

## Operator-managed data

`custom_currencies` stores the code, display name, `units_per_usd`, target decimal places, `rate_date`, `valid_until` and `updated_at`. Database constraints reject nonpositive rates, invalid precision, malformed custom codes and expiry before the rate date. The separate namespace prevents a manual value from replacing a provider's three-letter currency code.

An operator updates rows in an authenticated SQL session. Each conversion reads the relevant manual rows again, so an update takes effect without waiting for the provider cache to expire. The public create-only endpoint accepts new codes. It never merges submitted values into existing rows: JPA persist plus the database primary key prevents duplicate-code overwrites, including concurrent requests. V3 defaults existing rows to operator data; visitor submissions are marked USER_ENTERED and expire after 30 days. Public updates and deletes are unavailable. Expired or future-dated manual rates return `503`; they remain visible as inactive in the custom catalog. The active currency list omits them. Unknown codes return `400`.

## Cache and failure behaviour

The cache is valid for one hour after retrieval; that is separate from each rate's publication date. Both dates remain visible in responses. When the provider includes the USD reference row, its rate must equal one. If it omits that row, the API supplies the reference value with a null publication date.

Provider connection and read timeouts are three and five seconds. If a refresh fails, the API returns a short `503` response instead of presenting an expired snapshot as current. A healthy cached snapshot can be reused without calling the provider. A market conversion fails if a required refresh fails, even if the manual side is valid. Custom-to-USD and custom-to-custom calculations still work when the market provider is unavailable. Their `fetchedAt` is null because no provider snapshot was consulted.

Only health endpoints are exposed through Actuator, with internal component details hidden. Database failure makes readiness return `503` while liveness remains available. This gives a hosting platform separate signals for traffic routing and process health.

Cache refresh is synchronized within one application process. This local configuration uses one API instance; it does not implement a distributed refresh lock.
