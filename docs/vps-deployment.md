# Existing VPS demo release — November 1, 2026

The custom-currency API and public documentation were deployed to the existing Dhawr sites. This was an upgrade of the VPS deployment, not an Azure deployment.

- Documentation: https://docs.dhawr.com/
- API: https://api.dhawr.com/api/custom-currencies
- Readiness: https://api.dhawr.com/actuator/health/readiness
- Application/page source: `6f516e4b7738a2ae6a106934fdfe13fdeff8381b`.
- JAR SHA-256: `852dc1c99007e9cc252e844c0455e333c6326c68a536a57e010478d39c1c9540`.

## Existing hosting setup

aaPanel manages Apache. The API virtual host forwards requests to the Dhawr Java service on loopback port 8080. The documentation virtual host serves the static page. Apache routing, certificates, DNS and the documentation site's `.htaccess` and `.user.ini` were left unchanged.

The API uses the existing Java 17 runtime and dedicated project database credentials. Its `dhawr.service` launch is bounded to a 384 MiB Java heap, with a 192 MiB metaspace limit. These are JVM limits, not a total process-memory guarantee. Only this service was restarted.

## Database upgrade

The existing project database was backed up with aaPanel's PostgreSQL 18 tools. The system PostgreSQL 14 backup client could not read that server version; no package or database-server upgrade was performed.

The backup was actually restored into a temporary project verification database. Migrations V1 and V2, readiness, manual-to-USD conversion and a live-provider EUR conversion passed there. The temporary database was then removed.

The live upgrade places new tables in the separate `currency_bridge` schema. The legacy table and its row were preserved. Flyway and Hibernate must both use the same schema. The launch passes these explicitly:

```text
--spring.flyway.default-schema=currency_bridge
--spring.jpa.properties.hibernate.default_schema=currency_bridge
```

A loopback candidate was checked before the public service switched to the new JAR. The candidate was stopped after verification.

## Public page

The original frontend source was unavailable. The public rendering code was recovered from the existing deployed bundle, while its prebuilt runtime and stylesheet were preserved. The editable page is in `web/app.js`; `node web/build.mjs` creates hashed static assets. This is recovered rendering code, not a reconstruction of the original JSX project.

The page reads `/api/rates?base=USD`, labels rates as units per USD, identifies manual and Frankfurter sources, and shows rate dates. It handles failures without presenting fabricated fallback data. Examples use the new conversion and catalog routes. The old `/ismail` editor route explains the database-managed workflow and links to the operator guide.

New hashed assets were uploaded before the index was switched. Previous assets and the previous index were retained for rollback. The deployed CSS matched the original stylesheet byte for byte.

## Verification

- 63 automated backend tests passed with zero failures, errors or skips.
- The PostgreSQL 18 backup restored successfully, and both migrations passed with Java 17.
- Nine candidate API checks passed, including manual and market conversion, reverse conversion, catalogs, unsupported codes, retired legacy routing and GET CORS.
- Eleven public HTTPS checks passed after cutover: readiness, manual conversion, market provenance, both catalogs, unsupported-code handling, frontend-origin access, current page and application bytes, unchanged CSS and SPA routing.
- The local browser displayed actual candidate rate cards and the updated snippets, handled an API failure without fallback quotes, and opened the replacement operator page.

The demo uses `GAME_CREDITS` at 500 units per USD, valid through December 1, 2026. It is fictional data with a manual expiry. No refresh job was added. The provider's fetched timestamp and publication date are returned separately from the operator's valuation dates.

## Rollback

Private copies of the previous JAR, service definition, database backup and page index are retained on the API site. A rollback switches the JAR and index back and removes only the Dhawr service override, then restarts only `dhawr.service`. New tables are additive in a separate schema; rolling back code does not require deleting them or restoring the entire live database.

Do not replace another site's configuration, restart shared services, or use an older system backup client against aaPanel's database. No other hosted application was included in this deployment.
