---
name: db-migration
description: Writes a Flyway database migration for HelloBill's Postgres and checks it on a scratch copy of the schema before anything else uses it. Use when a change needs a new table, column, index or constraint, or asks to change the database schema.
---

# Write a database migration

1. **Next number**: `ls backend/src/main/resources/db/migration/`. The new file is
   `V<highest + 1>__what_it_does.sql`, lower case with underscores. Never edit an old file:
   Flyway has already run it on every laptop and server.
2. **Write it** with these rules:
   - Money: `NUMERIC(12, 2)`. Codes and names: `VARCHAR(n)` with a sensible `n`.
   - A new `NOT NULL` column on a table that has rows needs a `DEFAULT`, or it fails on real data.
   - Every foreign key gets an index: `CREATE INDEX things_customer_idx ON things (customer_id);`
   - Unique business rules (a code, a number) get a `UNIQUE` constraint, not just a check in Java.
   - One change per file. No `DROP` of anything with data in it: ask the user first.
3. **Check it** before you touch any Java:

   ```bash
   bash .claude/skills/db-migration/scripts/check-migration.sh
   ```

   It runs every migration, in order, on an empty scratch database inside the dev Postgres
   container, prints the tables your newest file touched, and drops the scratch database. It never
   touches the real dev database. Fix the SQL until it prints `MIGRATION OK`.
4. **Then** update the JPA entity to match (Hibernate only validates the schema, it never changes it),
   and run `backend/gradlew -p backend spotlessApply test`.
