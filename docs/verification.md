# Local verification and recovery checks

[Project overview](../README.md) · [Architecture](architecture.md) · [Local setup](local-development.md)

I ran the API and PostgreSQL in a separate local Docker environment on October 30, 2026. The application image used Java 17 on Linux ARM64, with PostgreSQL 16.15. The runtime checks used the real rate feed and a new local database.

## Custom-currency feature — November 1, 2026

The current source passed **63 automated tests** using synthetic provider responses and H2. These cover both existing market conversions and the new configured-currency flow, including reverse conversion, custom-to-custom conversion, operator updates, expiry, separate code namespaces and rejected public write requests.

A separately built application was then run against a temporary PostgreSQL 16.15 database on the dedicated local Docker context, with fresh synthetic credentials and no persistent volume. The API bound only to localhost and used the live Frankfurter feed. **All 10 runtime checks passed:**

- Flyway applied V1 and V2 on PostgreSQL, with the restricted application role.
- 1,000 fictional credits at 500 per USD converted to US$2.
- Custom-to-EUR conversion matched the live USD-to-EUR rate and labeled both sources.
- An operator SQL update changed the next conversion without restarting the app.
- USD-to-custom conversion worked in reverse.
- Expired configured rates returned `503`; their catalog entry showed `active: false`.
- PostgreSQL rejected nonpositive valuations, invalid decimal places and a custom row using `USD`.
- Database readiness remained `UP`.

The API process and temporary PostgreSQL container were removed afterward. These are local checks, not an Azure deployment or a published image.

Evidence: [automated test summary](evidence/custom-currency-test-summary.json), [PostgreSQL/live-provider runtime results](evidence/custom-currency-runtime-results.json).

## Original market-only results — October 30, 2026

| Check | Observed result |
| --- | --- |
| Maven verification inside the image build. | 42 tests passed; zero failures, errors or skipped tests. |
| HTTP smoke script against the running API. | All 14 checks passed, including conversion, rounding, validation, cache reuse and health exposure. |
| Live provider integration. | The API stored 170 currency entries in the snapshot during this run. |
| Database initialization. | Flyway migration version 1 applied successfully; Hibernate schema validation completed. |
| Application database role. | No superuser, database creation or role creation permissions. |
| Container access. | API ran as UID 10001 with a read-only filesystem, all Linux capabilities dropped and no-new-privileges enabled. |
| Network exposure. | API bound to `127.0.0.1:8088`; PostgreSQL had no published host port. |
| Credential handling. | No database password value in the API container environment; generated password files were ignored by Git. |
| Application restart. | Cached rate data and its fetch timestamp remained unchanged. Readiness returned after 5.48 seconds. |
| Database outage. | Liveness returned `200 UP`; readiness returned `503 DOWN`. |
| Database recovery. | Readiness returned to `200` after 1.08 seconds; the cached snapshot was preserved. All 14 smoke checks passed again. |

The timings are observations from one local run, measured from the restart/start command until a successful readiness response. Currency coverage reflects that feed response rather than a fixed supported count.

## Provider integration issue and fix

The first live conversion returned `503` even though the provider was reachable. I inspected the response and found a USD-to-USD row with a value of one. The parser had already inserted its own USD reference entry, so it treated the provider's valid row as a duplicate.

I changed the parser to accept one valid USD reference row and preserve its publication date. It still rejects duplicate rows and a USD reference rate other than one. If the provider omits USD, the API supplies the reference value with a null date. Three additional automated cases cover these responses, bringing the suite from 39 to 42 tests.

After rebuilding the image, the live conversion and the complete smoke script passed. This check caught an integration detail that the original synthetic responses did not include.

## Evidence

- [HTTP smoke results](evidence/local-smoke-results.json)
- [Container, database and recovery observations](evidence/local-runtime-results.json)
- [Repeatable HTTP smoke script](../scripts/smoke-test.py)
- [Automated build summary](evidence/local-build-summary.json)

The automated suite uses synthetic provider responses and H2. The container checks add real PostgreSQL and live-provider coverage. These results describe the local environment; the Azure hosting milestone has its own deployment and verification work.
