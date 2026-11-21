#!/bin/bash
# Adds the read-only database login (hellobill_readonly) to the dev Postgres in docker compose.
# Safe to run again. Run from the repo root after `docker compose up -d`.
docker compose exec -T postgres psql -U hellobill -d hellobill -v ON_ERROR_STOP=1 -q < docker/postgres/10-readonly-user.sql && echo "hellobill_readonly is ready (read only)"
