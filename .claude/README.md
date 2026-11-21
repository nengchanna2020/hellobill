# AI Team Kit

This folder turns Claude Code into a small team that works on HelloBill. It's built one part per
episode in Hello Coder Season 3, "Build Your AI Dev Team". Everything here is plain files:
Markdown, JSON and a few short shell scripts. No agent code.

| Part | Where | Episode |
|---|---|---|
| Project instructions | `CLAUDE.md`, `frontend/CLAUDE.md`, `backend/CLAUDE.md`, `docs/agent/` | 1 (done) |
| Permissions and the sandbox | `.claude/settings.json` | 2 |
| Hooks: rules the agent can't skip | `.claude/hooks/` | 3 |
| Skills: our know-how, loaded when needed | `.claude/skills/` | 4 |
| Outside tools for the whole team (MCP) | `.mcp.json` | 5 |
| Subagents: a planner, builder, tester, reviewer and security checker | `.claude/agents/` | 6 |
| One-word workflows, with a spec first | `specs/`, `.claude/skills/` | 7 |
| Testing the kit itself | `evals/` | 8 |
| Sharing it as a plugin, and running it on GitHub | `ai-team-kit/`, `.github/workflows/` | 9 |

**Shared (in git):** this folder, every `CLAUDE.md`, `docs/agent/`, and later `.mcp.json`.
**Just for you (never committed):** `~/.claude/CLAUDE.md`, `CLAUDE.local.md` and
`.claude/settings.local.json`. The last two are in `.gitignore`.

The empty folders below hold a `.gitkeep` file, because git doesn't keep empty folders.
