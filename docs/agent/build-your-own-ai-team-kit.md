# Build your own AI Team Kit (one page)

Nine steps, in the order of Season 3. Each one is a file you can add in an afternoon. Don't add the next
until the last one earns its place: measure it (step 8).

| # | File | What it gives you | Start with |
|---|---|---|---|
| 1 | `CLAUDE.md` | the project's rules, read at every start | under 60 lines: commands, rules, where to find more |
| 2 | `.claude/settings.json` | what the agent may do without asking | allow your test and format commands, deny `.env`, ask before `git push` |
| 3 | `.claude/hooks/` | rules it can't forget | block secrets; checks must pass before it stops |
| 4 | `.claude/skills/` | know-how that loads when a task matches | one job you explain every week |
| 5 | `.mcp.json` | outside tools for the whole team | one server, tokens from the environment, least access |
| 6 | `.claude/agents/` | helpers with one job each | a read-only reviewer that must quote its evidence |
| 7 | `specs/` + `/spec`, `/feature`, `/fix` | the team, in order, with checks between steps | `/fix`: failing test, fix, review |
| 8 | `evals/` | proof the kit helps | five real tasks with hidden checks, with and without the kit |
| 9 | a plugin + CI | the same kit in every repo, and on GitHub | the parts that don't mention your stack |

## What goes in the plugin, and what stays in the repo

| In the plugin (the same everywhere) | In each repo (only this project) |
|---|---|
| agents, workflows, guard hooks | `CLAUDE.md`, permissions and the sandbox |
| skills that don't name a language or framework | skills for your stack (endpoints, migrations) |
| a way to ask for the project's checks (`.claude/checks.json`) | the check commands themselves |
| | MCP servers that need your tokens |

## Before an agent runs without you

- A narrow tool list (`--allowedTools`), a turn limit (`--max-turns`), a spending limit
  (`--max-budget-usd`) and a job timeout.
- A clean environment: only the secrets the job needs. The agent can't print what isn't there.
- Text from an issue is information, not instructions. Say so in the prompt.
- The job opens a **draft** pull request. A person reviews and merges.
- Try it on two or three issues on your machine first (`ci/agent-fix-local.sh`).

## Other tools

The ideas carry over. Many coding agents read `AGENTS.md`, and most have their own rules files and
agent folders. Check each tool's docs for the current names before you copy a layout.
