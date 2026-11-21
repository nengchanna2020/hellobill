# Permission rules cheat sheet

For `.claude/settings.json` (team, in git), `.claude/settings.local.json` (just you, this
project) and `~/.claude/settings.json` (just you, every project). Claude Code 2.1.330, checked
against the docs on 21 November 2026. Run `/permissions` to see every rule and the file it
comes from.

## The order

**deny → ask → allow → permission mode → sandbox.** The first rule that matches decides. A more
specific rule doesn't win: a broad deny beats a narrow allow. A deny in any file beats an allow
in any other file.

## Rule shapes

| Rule | Matches | Doesn't match |
|---|---|---|
| `Bash` | every shell command | |
| `Bash(npm test)` | `npm test` exactly | `npm test -- money` |
| `Bash(npm test *)` | `npm test`, `npm test -- money` | `npm testx` |
| `Bash(npm run *)` | `npm run build`, `npm run test --watch` | `npm install` |
| `Bash(git log *)` | `git log`, `git log --oneline` | `git push` |
| `Bash(git *)` | every git command, including `git push` | |
| `Read(./.env)` | reading `.env` here (also blocks Edit/Write on it) | |
| `Read(**/.env)` | any `.env` under the project | |
| `Edit(/backend/src/**)` | edits under `<repo>/backend/src/` | `backend/build.gradle` |
| `WebFetch(domain:github.com)` | the WebFetch tool on github.com | `curl` in Bash |
| `mcp__github` | every tool from the `github` MCP server | |

- Put the `*` after the subcommand: `Bash(git log *)`, not `Bash(git * main)`.
- `/path` starts at the project (in project settings), `//path` is an absolute path, `~/path`
  is your home folder.
- Use `Edit(...)` for file changes (not `Write(...)`), and `Read(...)` for reading and searching.

## What a Bash rule can't see

- Compound commands are split: `Bash(npm test *)` doesn't allow `npm test && rm -rf build`. A
  deny or ask rule matches any part of the command.
- It matches the text, not the program: `Bash(curl *)` doesn't stop `/usr/bin/curl`,
  `sh -c 'curl ...'` or a Python script that downloads. Use the sandbox for that.
- Read and Edit rules cover Claude's file tools, not a script that opens files by itself.

## Modes (`--permission-mode` or `defaultMode`)

| Mode | Runs without asking | Use it for |
|---|---|---|
| `default` (Manual) | reads | unfamiliar or sensitive work |
| `acceptEdits` | reads, file edits, `mkdir`, `mv`, `cp`... | iterating on code you're reviewing |
| `plan` | reads only, no edits | exploring before a change |
| `auto` | everything, a classifier checks each action | long tasks; the terminal default in 2.1.283+ |
| `dontAsk` | only what your allow rules list | CI and scripts |
| `bypassPermissions` | everything | throwaway containers and VMs only |

Deny rules apply in every mode, even `bypassPermissions`.

## The sandbox (`/sandbox`, or `"sandbox": {"enabled": true}`)

- Shell commands only. Writes: the project and a temp folder. Reads: most of the computer.
  Network: no hosts until you list them in `sandbox.network.allowedDomains`.
- `autoAllowBashIfSandboxed: true` runs sandboxed commands without a prompt.
- `allowUnsandboxedCommands: false` stops the agent retrying outside the sandbox.
- `excludedCommands` lets a tool that can't work inside it (Docker) out. Keep it narrow.
- On Linux and WSL2 it needs `bubblewrap` and `socat`. Native Windows: no sandbox, use WSL2.
