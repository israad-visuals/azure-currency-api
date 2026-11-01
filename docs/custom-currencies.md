# Custom currencies through USD

[Project overview](../README.md) · [Application design](architecture.md) · [Local setup](local-development.md)

The API distinguishes who supplied each valuation:

| Source | Who maintains it | Meaning |
| --- | --- | --- |
| `USER_ENTERED` | A visitor submits a new currency through the form or API. | Unverified units per USD, valid for 30 days. |
| `MANUAL` | An operator updates PostgreSQL. | Configured custom units per US$1. |
| `FRANKFURTER` | The external provider supplies reference rates. | Market currency units per US$1. |
| `USD_REFERENCE` | The calculation uses USD itself. | Always 1 USD per USD. |

Use this for game credits, community units or other locally managed currencies whose valuation must be configured. A configured price is the operator's input, not a market price verified by the API. Ordinary market-to-market conversion remains supported.

## Add a currency in the browser

Open https://docs.dhawr.com/#add-currency and enter a new currency code, display name and the number of units equal to US$1. The application stores it in PostgreSQL and refreshes the live card. It can immediately be used as a source or target in `/api/convert`.

The same operation is available as JSON:

```sh
curl --fail-with-body -X POST 'https://api.dhawr.com/api/custom-currencies' \
  -H 'Content-Type: application/json' \
  -d '{"code":"MY_CREDITS","name":"My game credits","unitsPerUsd":500}'
```

A successful request returns `201`. The server assigns the current UTC date, a 30-day expiry, two target decimal places and `USER_ENTERED` provenance. Codes are normalized to uppercase. Invalid values return `400`; an existing code returns `409` without modifying the original record. Updates and deletes remain operator-only SQL operations. Visitor submissions are unverified reference values.

This small demo permits ten successful additions per minute per process, returning `429` when busy, and stops accepting entries at 500 stored codes. This is a bounded single-process demo limit, not distributed abuse protection. Expired rows remain in the catalog; no scheduled cleanup or automatic renewal is configured.

## Store the rate in the right direction

For a fictional currency called `GAME_CREDITS`, suppose **500 credits = US$1**. Store `units_per_usd = 500`, not `0.002`.

```text
USD amount = source amount / source units per USD
Result     = USD amount * target units per USD
```

With an illustrative `0.90 EUR per USD`, 1,000 credits become US$2 and then €1.80. Actual market conversions use the provider's dated rate, which can differ from this example. The API does not round the intermediate USD amount to cents; it rounds the final result only.

Custom codes use 4–16 uppercase letters, digits or underscores, starting with a letter. Examples: `GAME_CREDITS`, `COMMUNITY_UNITS`, `LOCAL_UNIT`. Three-letter codes are reserved for the provider and USD, preventing a custom entry from replacing a market currency.

## Add or update the fictional example locally

Start the API and PostgreSQL using the local setup guide. Flyway applies V1, V2 and V3; existing provider-cache data is preserved. The custom table initially contains no rows. V3 labels public submissions separately from existing operator data. The example is an optional operator action, not an automatic production seed.

Open an authenticated SQL session:

```sh
docker compose exec db psql -h 127.0.0.1 -U currency_app -d currency_exchange --password
```

At the prompt, enter the application password from your private local secret file. Paste [the example SQL](../scripts/custom-currency-example.sql), which creates or updates this fictional valuation:

| Column | Example | Purpose |
| --- | --- | --- |
| `code` | `GAME_CREDITS` | Code used in API requests. |
| `name` | `Fictional game credits` | Display name. |
| `units_per_usd` | `500` | Number of custom units equal to US$1. |
| `decimal_places` | `0` | Round results into these credits to whole units. |
| `rate_date` | Today's date | First date this configured valuation applies. |
| `valid_until` | 30 days from today | Last date it may be used, inclusive. |
| `updated_at` | Current timestamp | When the operator recorded this value. |

The database enforces positive rates, decimal places between 0 and 8, valid custom codes and an end date no earlier than the rate date. Validity is evaluated using the API's UTC date. An expired or future-dated rate returns `503` rather than silently producing a conversion.

To change the fictional valuation later, run an explicit update in that SQL session:

```sql
UPDATE custom_currencies
SET units_per_usd = 1000,
    rate_date = CURRENT_DATE,
    valid_until = CURRENT_DATE + 30,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'GAME_CREDITS';
```

Each conversion reads custom rates directly from PostgreSQL, so changes take effect on the next request. The separately cached provider rates are not overwritten or forced to refresh.

## Try the endpoints

```sh
curl 'http://127.0.0.1:8088/api/convert?from=GAME_CREDITS&to=EUR&amount=1000'
curl 'http://127.0.0.1:8088/api/convert?from=USD&to=GAME_CREDITS&amount=2'
curl 'http://127.0.0.1:8088/api/custom-currencies'
curl 'http://127.0.0.1:8088/api/currencies'
curl 'http://127.0.0.1:8088/api/rates?base=GAME_CREDITS'
```

Conversion responses include `bridgeCurrency`, `usdAmount`, `fromQuote` and `toQuote`. Each quote shows its source, units per USD, rate date, recorded timestamp and validity end date when applicable. Existing amount, exchange-rate and rate-date fields remain available.

`/api/custom-currencies` lists all configured entries with an `active` flag, including expired ones, without calling the external provider. `/api/currencies` combines provider codes with active custom codes. `/api/rates` returns rates relative to either kind of base; these two combined listings require a valid provider snapshot.

Custom-to-USD and custom-to-custom conversions work without a market snapshot, with `fetchedAt: null`. A conversion involving another market currency requires a valid provider snapshot: it reuses the one-hour cache or refreshes it over HTTPS. A failed refresh returns `503`; an unknown code returns `400`.

The public API is read-only. There is no unauthenticated endpoint to create, change or delete valuations.

## Use it on Azure

The currently pinned `portal-v1` image predates custom currencies. Build a new Linux AMD64 image from this source, publish it to the image registry and use its digest in `container_image` before expecting this feature on Azure. The Terraform guide's setup/login/API sequence still applies; the new application runs V2 automatically.

To edit valuations using the existing temporary PostgreSQL client, first let the new API apply V2. Then temporarily switch `deployment_stage` to `setup` and apply a reviewed plan. In the setup client's portal console, use the printed database connection command with `user=currency_app` and enter the application password. Run the operator SQL, then switch back to `api` and apply a reviewed plan. The database rows persist; the demo is unavailable during that maintenance step. Check the app's outbound IP allowlist when revisions change.

No new Azure service is needed for this feature; it uses the existing database and API. Additional activity can still consume compute, database, log and network allowances.

Provider reference: [Frankfurter documentation](https://frankfurter.dev/) describes its USD-based queries, dated rates, caching and coverage. Availability is limited to the feed and the custom values you configure; this API does not claim coverage of every currency in the world.
