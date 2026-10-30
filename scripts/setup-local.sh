#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
umask 077
mkdir -p .local/secrets
chmod 700 .local .local/secrets
for name in db_admin_password db_app_password; do
    if [ ! -f ".local/secrets/$name" ]; then
        openssl rand -hex 32 > ".local/secrets/$name"
        chmod 444 ".local/secrets/$name"
    fi
done
printf '%s\n' 'Local database credentials are ready in .local/secrets.'
