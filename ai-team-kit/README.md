# AI Team Kit

The AI Team Kit from Hello Coder Season 3, as a Claude Code plugin. It gives any project a small team of
agents, a few typed workflows, and hooks that keep the agent honest.

## What's in it

| Part | What it does |
|---|---|
| Agents | `planner` (haiku, read-only), `builder`, `tester`, `reviewer` (read-only, must quote its evidence), `security-checker` (haiku, read-only) |
| Skills you type | `/ai-team-kit:spec`, `/ai-team-kit:feature NNN`, `/ai-team-kit:fix <bug>`, `/ai-team-kit:ship-check` |
| Skill it loads | `pull-request`: the title and description in the team's shape |
| Hooks | session facts at the start; blocks secrets and danger commands; no code before an approved spec; the project's checks must pass before the agent stops |

## Install

```bash
claude plugin marketplace add nengchanna2020/hellobill   # or a local path to this folder
claude plugin install ai-team-kit@hello-coder --scope project
```

`--scope project` writes the plugin into `.claude/settings.json`, so everyone who clones the repo is
offered the same kit.

## Tell the kit your checks

The kit doesn't know how your project builds. Add `.claude/checks.json`:

```json
{ "checks": [ { "name": "tests", "when": "", "run": "npm test" } ] }
```

`when` is a path prefix: a check runs only if a file under it changed (`""` means any change). The
agents and the Stop hook run exactly these commands. HelloBill's has two entries, `backend/` and
`frontend/`.

## What stays in your project

Permissions and the sandbox (`.claude/settings.json`), `CLAUDE.md`, MCP servers that need your tokens,
and skills that only make sense for your code. A plugin can't set permissions, and it shouldn't.

Needs bash, jq and git. Hook state goes in the plugin's data folder, not in your repo.
