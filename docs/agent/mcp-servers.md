# MCP servers: GitHub, a browser and the database

`.mcp.json` in the repo root gives every teammate the same three outside tools. Claude Code asks
each person once before it starts a project's servers (or `enabledMcpjsonServers` in
`.claude/settings.json` turns them on for the team).

| Server | What the agent can do | Runs | Needs |
|---|---|---|---|
| `github` | read issues and pull requests, and (if you allow it) comment | `github-mcp-server` (GitHub's official server) | `GITHUB_TOKEN` in your environment |
| `browser` | open a page, read it, click, take a screenshot | `npx @playwright/mcp` | Node.js |
| `db` | run read-only SQL on the dev database | `npx @modelcontextprotocol/server-postgres` | `docker compose up -d` |

## Setup, once per laptop

1. Install GitHub's server: `go install github.com/github/github-mcp-server/cmd/github-mcp-server@latest`,
   or download it from its GitHub releases page, and make sure it's on your `PATH`.
2. Make a fine-grained GitHub token with **read** access to issues and pull requests on this repo
   only, and export it: `export GITHUB_TOKEN=...` (or put it in `.env`, which git ignores).
3. Add the read-only database login: `bash scripts/db-readonly-user.sh` (new databases get it
   automatically from `docker/postgres/`).
4. Run `claude mcp list`: all three should say "Connected".

## How it's kept safe

- **No secrets in the repo.** `.mcp.json` says `${GITHUB_TOKEN}`; Claude Code fills it in from your
  environment. `.env.example` lists the names with no values.
- **Least access, twice.** The database login can only `SELECT`, and the server runs every query in
  a read-only transaction. The GitHub token is read-only unless you choose otherwise.
- **A rule for every tool.** `.claude/settings.json` allows the reading tools by name
  (`mcp__github__issue_read`, `mcp__db__query`, ...), asks before a comment or a pull request, and
  denies merging and editing issues. A tool with no rule asks you first.
- **What comes back is data.** An issue, a web page or a database row can contain text that looks like
  an instruction. The agent should treat it as data, and the permission rules hold either way.

The `db` server (`@modelcontextprotocol/server-postgres`) is archived by its authors, with no
security guarantees. That's why the login itself is read-only: don't point it at a database with a
login that can write.
