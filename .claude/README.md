# AI Team Kit

This folder turns Claude Code into a small team that works on HelloBill. It's built one part per
episode in Hello Coder Season 3, "Build Your AI Dev Team". Everything here is plain files:
Markdown, JSON and a few short shell scripts. No agent code.

| Part | Where | Episode |
|---|---|---|
| Project instructions | `CLAUDE.md`, `frontend/CLAUDE.md`, `backend/CLAUDE.md`, `docs/agent/` | 1 (done) |
| Permissions and the sandbox | `.claude/settings.json`, `docs/agent/settings-explained.md` | 2 (done) |
| Hooks: rules the agent can't skip | `.claude/hooks/`, `docs/agent/hooks-testing.md` | 3 (done) |
| Skills: our know-how, loaded when needed | `.claude/skills/` | 4 |
| Outside tools for the whole team (MCP) | `.mcp.json` | 5 |
| Subagents: a planner, builder, tester, reviewer and security checker | `.claude/agents/` | 6 |
| One-word workflows, with a spec first | `specs/`, `.claude/skills/` | 7 |
| Testing the kit itself | `evals/` | 8 |
| Sharing it as a plugin, and running it on GitHub | `ai-team-kit/`, `.github/workflows/` | 9 |

**Shared (in git):** this folder (with `settings.json`), every `CLAUDE.md`, `docs/agent/`, and later `.mcp.json`.
**Just for you (never committed):** `~/.claude/CLAUDE.md`, `CLAUDE.local.md` and
`.claude/settings.local.json`. The last two are in `.gitignore`.

The empty folders below hold a `.gitkeep` file, because git doesn't keep empty folders.

## Permissions (Episode 2)

`settings.json` is the team's permission policy. Claude Code checks every tool call against it,
deny rules first, then ask, then allow, and the sandbox limits what shell commands can reach.
Settings files are strict JSON, so there are no comments in it: `docs/agent/settings-explained.md`
goes through it line by line, and `docs/agent/permission-rules-cheatsheet.md` has the rule syntax.

Your own exceptions go in `.claude/settings.local.json` (never committed). It can add allow rules,
but it can't undo the team's deny or ask rules: a deny from any file wins.

The first time you open the repo in Claude Code, it asks you to trust the folder. The team's
allow rules only apply after you say yes. The deny and ask rules apply straight away.

## Hooks (Episode 3)

`hooks/` holds small shell scripts, and `settings.json` says when Claude Code runs each one. A
hook isn't advice like `CLAUDE.md`: it runs every time, whatever the agent thinks.

| Script | When | What it does |
|---|---|---|
| `session-context.sh` | the session starts | tells the agent the branch, last commit and open issues |
| `block-secrets.sh` | before Edit, Write or Bash | says no to secrets in files and to the danger list |
| `format-on-edit.sh` | after every edit | Prettier or Spotless on that one file |
| `run-related-tests.sh` | after every edit | that file's tests only, and the failures go back to the agent |
| `require-green-tests.sh` | the agent wants to stop | CI's checks must pass, or back to work (3 tries at most) |

They need `bash` and `jq`. Every run writes one line to `.claude/hooks.log` (git-ignored), and
`.claude/state/` (git-ignored) is what they remember during a session. The deny rules in
`settings.json` stop the agent from editing this folder. To test a hook without the agent, see
`docs/agent/hooks-testing.md`, or run `bash kit-tests/hooks/test-hooks.sh`.
