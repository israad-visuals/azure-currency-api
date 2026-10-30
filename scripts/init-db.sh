#!/usr/bin/env sh
set -eu
APP_PASSWORD="$(cat /run/secrets/db_app_password)"
export APP_PASSWORD
psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --set=ON_ERROR_STOP=1 <<'SQL'
\getenv app_password APP_PASSWORD
CREATE ROLE currency_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD :'app_password';
REVOKE CONNECT ON DATABASE currency_exchange FROM PUBLIC;
GRANT CONNECT ON DATABASE currency_exchange TO currency_app;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO currency_app;
SQL
