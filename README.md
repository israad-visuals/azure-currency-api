![Currency Exchange API: Azure hosting and container delivery, with 42 local automated tests, 14 HTTP checks, two architecture views and three API endpoints.](docs/assets/project-header.svg)

A Java REST API that converts between supported currencies and caches exchange rates in PostgreSQL. This project brings together the application, a repeatable container environment and an Azure Container Apps hosting design.

**Explore:** [Project context](#project-context) · [My contribution](#my-contribution) · [Cloud architecture](#cloud-architecture) · [Build and deployment](#build-and-deployment) · [Results](#local-verification) · [Documentation](#documentation)

## Project context

Currency conversion needs consistent decimal calculations, dated rates and clear handling of provider failures. I built the API around a shared rate snapshot, then packaged it with PostgreSQL so database setup and recovery could be checked in a repeatable environment.

For Azure hosting, I chose Container Apps Consumption to reuse the Docker image and scale API compute to zero between visits. The design keeps PostgreSQL private and gives the application scoped access to its database secret.

## My contribution

- **Application:** I added cross-currency conversion, currency-specific rounding, input validation and a one-hour rate cache.
- **Containers and data:** I packaged the API and PostgreSQL with Docker Compose, a versioned database migration and separate application credentials.
- **Azure architecture:** I designed the ingress, subnet layout, private database connection, managed identity, secret access and monitoring.
- **Operations:** I verified the local application, investigated a live-provider parsing issue and checked cache persistence and database-outage recovery.

## Cloud architecture

[![Currency API cloud architecture: managed HTTPS ingress, Container Apps Consumption, private PostgreSQL, Key Vault, monitoring and the external rate provider.](docs/architecture/01-cloud-architecture.png)](https://viewer.diagrams.net/?nav=1&title=Currency%20API%20Azure%20Architecture&page-id=currency-api-cloud#Uhttps%3A%2F%2Fraw.githubusercontent.com%2Fisraad-visuals%2Fazure-currency-api%2Fmain%2Fdocs%2Farchitecture%2FCurrency-API-Azure-Architecture.drawio)

The Azure design routes HTTPS requests through managed ingress to the API. The application reaches PostgreSQL over a private TLS connection and refreshes rates from Frankfurter over HTTPS. Key Vault and managed identity handle secret access; Azure Monitor and Log Analytics provide operational visibility.

[Open full-size architecture](docs/architecture/01-cloud-architecture.png) · [Design decisions](docs/architecture/README.md#cloud-architecture) · [Editable draw.io source](docs/architecture/Currency-API-Azure-Architecture.drawio)

## Build and deployment

[![Currency API delivery design: verify the application, build an AMD64 image, publish to GitHub Container Registry and deploy a pinned revision with OIDC and health checks.](docs/architecture/02-build-deployment.png)](https://viewer.diagrams.net/?nav=1&title=Currency%20API%20Delivery&page-id=currency-api-delivery#Uhttps%3A%2F%2Fraw.githubusercontent.com%2Fisraad-visuals%2Fazure-currency-api%2Fmain%2Fdocs%2Farchitecture%2FCurrency-API-Azure-Architecture.drawio)

I designed the delivery flow around a verified Docker image and an exact image digest. GitHub Actions builds the Linux AMD64 image and publishes it to GitHub Container Registry. The deployment design uses short-lived Azure access through OIDC, separate Terraform steps, revision health checks and a previous working revision for recovery.

[Open full-size delivery flow](docs/architecture/02-build-deployment.png) · [Release design](docs/architecture/README.md#build-and-deployment)

## Local verification

| Check | Recorded result |
| --- | --- |
| Automated verification | **42 tests passed**, with zero failures, errors or skipped tests. |
| Running API and PostgreSQL | **14 HTTP smoke checks passed**, repeated after database recovery. |
| API restart | The cached rates and fetch timestamp were preserved. |
| Database outage | Liveness stayed `200`; readiness changed to `503` and recovered to `200` after restart. |

The live provider returned a valid USD reference row that the parser rejected. I traced the `503` response to that row, corrected the handling and added regression cases before repeating the container checks.

[Read the verification report and captured results](docs/verification.md)

## API

| Endpoint | Purpose |
| --- | --- |
| `GET /api/convert?from=CAD&to=EUR&amount=100` | Convert an amount between supported currencies. |
| `GET /api/rates?base=CAD` | Return the cached rate table for a chosen base. |
| `GET /api/currencies` | List the currency codes in the provider feed. |

Responses include rate publication dates and the snapshot fetch time. Calculations use `BigDecimal` and round to the target currency's decimal places. The API calculates reference amounts; it does not transfer money.

## Documentation

| Guide | What it covers |
| --- | --- |
| [Azure architecture](docs/architecture/README.md) | Service choices, subnet layout, access paths and delivery design. |
| [Application design](docs/architecture.md) | Calculations, caching, credentials and failure handling. |
| [Verification and troubleshooting](docs/verification.md) | Observed results, recovery checks and the provider integration fix. |
| [Local setup and operations](docs/local-development.md) | Container startup, health checks, logs and cleanup. |

The local runtime checks and Azure design are documented separately. The next implementation step is the Azure Terraform configuration, including the complete network cost and resource cleanup controls.
