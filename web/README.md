# Public API documentation

The existing docs.dhawr.com page is preserved here. The original React/Vite source was unavailable, so the public application was recovered from the deployed JavaScript. This is recovered rendering code, not the original JSX source.

- `app.js`: editable page content, examples, routing and live API reads.
- `vendor/runtime.js`: preserved prebuilt React, router, animation, icons and highlighting runtime. Keep its embedded license notices. No dependency upgrades are included in this recovery.
- `public/assets/site.css`: unchanged stylesheet from the existing site.
- `build.mjs`: combines the runtime and application, then hashes the asset filenames for cache-safe deployment.

Run `node web/build.mjs` from the repository root. Preview with `python3 -m http.server 4173 --directory web/dist` and open http://localhost:4173. The live API base is the `fq` constant at the top of `app.js`.

The frontend requests `/api/rates?base=USD`, handles non-200 responses and shows no fabricated fallback quotes. The cards label units per USD and identify manual valuations versus Frankfurter market rates. Snippets use `/api/convert`, `/api/currencies` and `/api/custom-currencies`. Response examples are excerpts; GAME_CREDITS is fictional demo data.

The former `/ismail` browser editor is retired because the new API has no public write endpoint. It links to the operator SQL guide instead. Manual values and validity dates are maintained in PostgreSQL.

Deploy the contents of `web/dist` to the existing static site, preserving aaPanel's `.htaccess` and `.user.ini`. Upload hashed assets first and switch `index.html` last. Keep previous assets and the previous index for rollback.
