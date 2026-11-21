#!/bin/bash
# Runs every Flyway migration, in order, on a new empty database in the dev Postgres container
# (docker compose), then shows the tables the newest migration touched and drops the database.
# The real dev database is never touched. Run from the repo root.
set -u
DIR=backend/src/main/resources/db/migration
DB=migration_check_$$
psql() { docker compose exec -T postgres psql -U hellobill -v ON_ERROR_STOP=1 -q "$@"; }
docker compose exec -T postgres pg_isready -U hellobill > /dev/null || { echo "Postgres isn't running: docker compose up -d"; exit 1; }
psql -d postgres -c "CREATE DATABASE $DB" || exit 1
trap 'psql -d postgres -c "DROP DATABASE IF EXISTS $DB" > /dev/null' EXIT
LAST=""
for f in $(ls $DIR/V*__*.sql | sort -V); do
  if ! psql -d $DB < "$f" > /tmp/$DB.log 2>&1; then
    echo "FAILED: $(basename "$f")"; cat /tmp/$DB.log; exit 1
  fi
  echo "applied $(basename "$f")"; LAST=$f
done
for t in $(grep -oiE '(create table|alter table|create index [a-z_]+ on) +(if not exists +)?[a-z_]+' "$LAST" | awk '{print $NF}' | sort -u); do
  echo; echo "== $t"; psql -d $DB -c "\d $t" | sed -n '2,40p'
done
echo; echo "MIGRATION OK: $(basename "$LAST") on a scratch database"
