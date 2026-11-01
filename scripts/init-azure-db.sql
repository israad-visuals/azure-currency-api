\set ON_ERROR_STOP on

CREATE ROLE currency_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;

REVOKE CONNECT ON DATABASE currency_exchange FROM PUBLIC;
GRANT CONNECT ON DATABASE currency_exchange TO currency_app;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO currency_app;

SELECT current_database(),
       has_database_privilege('currency_app', 'currency_exchange', 'CONNECT') AS can_connect,
       has_schema_privilege('currency_app', 'public', 'USAGE') AS can_use_schema,
       has_schema_privilege('currency_app', 'public', 'CREATE') AS can_create_tables;
