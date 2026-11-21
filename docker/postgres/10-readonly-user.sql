-- A login that can read every table and change nothing. The db MCP server uses it.
-- Docker runs this file when the database is first created. For a database that already
-- exists, run: bash scripts/db-readonly-user.sh
DO $$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'hellobill_readonly') THEN
    CREATE ROLE hellobill_readonly LOGIN PASSWORD 'readonly';
  END IF;
END $$;
GRANT CONNECT ON DATABASE hellobill TO hellobill_readonly;
GRANT USAGE ON SCHEMA public TO hellobill_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO hellobill_readonly;
ALTER DEFAULT PRIVILEGES FOR ROLE hellobill IN SCHEMA public GRANT SELECT ON TABLES TO hellobill_readonly;
ALTER ROLE hellobill_readonly SET default_transaction_read_only = on;
