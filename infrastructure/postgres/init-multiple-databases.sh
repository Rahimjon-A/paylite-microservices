#!/bin/bash

set -e
set -u

create_db_and_user() {
    local db_name="$1"
    local db_user="$2"
    local db_password="$3"

    echo "========================================"
    echo "Processing database: $db_name"
    echo "Processing user:     $db_user"
    echo "========================================"

    # ------------------------------------------------------------
    # Create role if it does not exist
    # ------------------------------------------------------------

    psql \
        -v ON_ERROR_STOP=1 \
        --username "$POSTGRES_USER" \
        --dbname "postgres" \
        --set=db_user="$db_user" \
        --set=db_password="$db_password" <<'EOSQL'

SELECT format(
    'CREATE ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE',
    :'db_user',
    :'db_password'
)
WHERE NOT EXISTS (
    SELECT FROM pg_catalog.pg_roles
    WHERE rolname = :'db_user'
)\gexec

EOSQL

    # ------------------------------------------------------------
    # Always make sure password and permissions are correct
    # ------------------------------------------------------------

    psql \
        -v ON_ERROR_STOP=1 \
        --username "$POSTGRES_USER" \
        --dbname "postgres" \
        --set=db_user="$db_user" \
        --set=db_password="$db_password" <<'EOSQL'

SELECT format(
    'ALTER ROLE %I WITH LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE',
    :'db_user',
    :'db_password'
)\gexec

EOSQL

    # ------------------------------------------------------------
    # Create database if it does not exist
    # ------------------------------------------------------------

    psql \
        -v ON_ERROR_STOP=1 \
        --username "$POSTGRES_USER" \
        --dbname "postgres" \
        --set=db_name="$db_name" \
        --set=db_user="$db_user" <<'EOSQL'

SELECT format(
    'CREATE DATABASE %I OWNER %I',
    :'db_name',
    :'db_user'
)
WHERE NOT EXISTS (
    SELECT FROM pg_catalog.pg_database
    WHERE datname = :'db_name'
)\gexec

EOSQL

    # ------------------------------------------------------------
    # Make sure the correct user owns the database
    # ------------------------------------------------------------

    psql \
        -v ON_ERROR_STOP=1 \
        --username "$POSTGRES_USER" \
        --dbname "postgres" \
        --set=db_name="$db_name" \
        --set=db_user="$db_user" <<'EOSQL'

SELECT format(
    'ALTER DATABASE %I OWNER TO %I',
    :'db_name',
    :'db_user'
)\gexec

SELECT format(
    'GRANT ALL PRIVILEGES ON DATABASE %I TO %I',
    :'db_name',
    :'db_user'
)\gexec

EOSQL

    echo "Database '$db_name' is ready."
    echo "User '$db_user' is ready."
    echo
}


# ============================================================
# PAYLITE
# ============================================================

create_db_and_user \
    "paylite" \
    "paylite" \
    "paylite"


# ============================================================
# CARD BANK
# ============================================================

create_db_and_user \
    "card_bank" \
    "card_bank" \
    "card_bank"


# ============================================================
# UZCARD
# ============================================================

create_db_and_user \
    "uzcard" \
    "uzcard" \
    "uzcard"


# ============================================================
# HUMO
# ============================================================

create_db_and_user \
    "humo" \
    "humo" \
    "humo"


echo "========================================"
echo "All PayLite databases are ready."
echo "========================================"