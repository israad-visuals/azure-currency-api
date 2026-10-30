# Local setup and operations

[Project overview](../README.md) · [Architecture](architecture.md) · [Verification results](verification.md)

## Start the containers

Requirements: Docker with Compose, OpenSSL, and Python 3 for the HTTP smoke checks. The Docker build supplies Maven and Java 17.

From the repository root:

```sh
./scripts/setup-local.sh
docker compose up --build -d --wait
python3 scripts/smoke-test.py
```

If Compose is installed as the standalone command, use `docker-compose` in place of `docker compose`. With Colima, start the intended profile and select its Docker context explicitly before running these commands.

The API listens at `http://127.0.0.1:8088`. Set `API_PORT` before the Compose and smoke commands if that port is occupied. PostgreSQL stays on the internal container network.

The first build runs the automated tests. PostgreSQL initializes the application role, then Flyway applies the cache migration when the API starts. The first currency request fetches the rate table over the internet.

```sh
curl 'http://127.0.0.1:8088/api/convert?from=CAD&to=EUR&amount=100'
curl http://127.0.0.1:8088/actuator/health/readiness
```

## Credentials and storage

`scripts/setup-local.sh` generates two random database passwords under `.local/secrets/`. Running it again preserves existing files. Keep those files with the database volume: changing a password file does not change an existing PostgreSQL role's password.

The parent directories have owner-only access. Secret files are readable by the container users through their mounts. The application imports its password through Spring's `configtree:/run/secrets/` support; the administrator password is not supplied to the API.

The `database` named volume preserves PostgreSQL data when containers are replaced or stopped. Git ignores `.local/`, and the Docker build excludes it.

## Check health and logs

```sh
docker compose ps
docker compose logs --tail 50 api
curl http://127.0.0.1:8088/actuator/health/liveness
curl http://127.0.0.1:8088/actuator/health/readiness
```

Both probes return `{"status":"UP"}` during normal operation. Readiness also requires PostgreSQL. The smoke script checks conversion results against the returned rate table, input handling, cache reuse and health endpoint exposure.

To capture the current smoke output locally:

```sh
mkdir -p .local/evidence
python3 scripts/smoke-test.py > .local/evidence/smoke-results.json
```

## Database outage and recovery

Use only this local Compose project for the following exercise:

```sh
docker compose stop db
curl -i http://127.0.0.1:8088/actuator/health/liveness
curl -i http://127.0.0.1:8088/actuator/health/readiness
docker compose start db
```

Liveness should remain `200`; readiness should return `503` while the database is stopped. Wait for PostgreSQL to become healthy, then repeat readiness and the smoke check. The connection pool can take a short time to reconnect.

## Stop the environment

```sh
docker compose down
```

This removes the project containers and network while retaining the database volume. `docker compose down --volumes` also deletes the local database; use it only when deliberately resetting this environment. Stop a dedicated Colima profile afterward if it is no longer needed.

## Run without Docker

Use Java 17 or later and a separate PostgreSQL database named `currency_exchange`. Configure these variables in the terminal or IDE:

| Variable | Purpose |
| --- | --- |
| `DB_URL` | JDBC URL; defaults to `jdbc:postgresql://localhost:5432/currency_exchange`. |
| `DB_USERNAME` | Database application role. |
| `DB_PASSWORD` | Its password. Required unless a secret configuration source provides it. |
| `RATE_CACHE_DURATION` | Cache duration; defaults to `PT1H`. |
| `DB_POOL_SIZE` | Maximum database connections; defaults to `5`. |
| `PORT` | Application port; defaults to `8080`. |

The application role needs permission to create tables in its own schema so Flyway can apply migrations. Use a new database for this version; the migration does not import the previous manually entered rate table. Spring Boot does not automatically load `.env` files.

```sh
./mvnw verify
./mvnw spring-boot:run
```

The Maven tests use synthetic provider responses and an isolated H2 database. They do not require local database credentials or live provider access.
