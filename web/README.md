# Public API documentation

The existing docs.dhawr.com page is preserved here. The original React/Vite source was unavailable, so the public application was recovered from the deployed JavaScript. This is recovered rendering code, not the original JSX source.

- `app.js`: editable page content, examples, routing and live API reads.
- `vendor/runtime.js`: preserved prebuilt React, router, animation, icons and highlighting runtime. Keep its embedded license notices. No dependency upgrades are included in this recovery.
- `public/assets/site.css`: unchanged stylesheet from the existing site.
- `build.mjs`: combines the runtime and application, then hashes the asset filenames for cache-safe deployment.

Run `node web/build.mjs` from the repository root. Preview with `python3 -m http.server 4173 --directory web/dist` and open http://localhost:4173. The live API base is the `fq` constant at the top of `app.js`.

The frontend requests `/api/rates?base=USD`, handles non-200 responses and shows no fabricated fallback quotes. The cards label units per USD and identify manual valuations versus Frankfurter market rates. Snippets use `/api/convert`, `/api/currencies` and `/api/custom-currencies`. Response examples are excerpts; GAME_CREDITS is fictional demo data.

The public Add currency form posts a new code, name and units per USD to `/api/custom-currencies`. A successful save refreshes the live card for that code. Validation and duplicate errors are shown beside the form; uncertain network results are not automatically retried. Native required fields and labels support keyboard and mobile entry.

The former `/ismail` browser editor links to the operator SQL guide for changes to existing codes. Anonymous visitors may only add new codes. New entries are labeled USER_ENTERED, use two decimal places and expire after 30 days; existing operator data keeps its MANUAL source.

Deploy the contents of `web/dist` to the existing static site, preserving aaPanel's `.htaccess` and `.user.ini`. Upload hashed assets first and switch `index.html` last. Keep previous assets and the previous index for rollback.
