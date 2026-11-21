# `.claude/settings.json`, line by line

Settings files are strict JSON: a `//` comment or a trailing comma is a syntax error. So the real
file has no comments, and this page explains it. Keep the two in step when you change a rule.

Checked against Claude Code 2.1.330 and the docs (code.claude.com/docs/en/permissions and
/sandboxing) on 21 November 2026.

## How a tool call is checked

1. **Deny rules.** A match blocks the call. Nothing else can allow it: not an allow rule, not
   your local settings, not a permission mode.
2. **Ask rules.** A match asks a human, even if an allow rule also matches.
3. **Allow rules.** A match runs the call without asking.
4. **The permission mode** decides everything that matched no rule (Manual asks, auto lets a
   classifier decide, `dontAsk` refuses, and so on).
5. **The sandbox.** A shell command that runs still runs inside it: the operating system limits
   which files it can write and which hosts it can reach.

## `permissions.allow`: what runs without asking

```jsonc
"Edit(/frontend/src/**)",        // edits in the frontend code. "/" means the repo root here
"Edit(/frontend/e2e/**)",        // ... and the end-to-end tests
"Edit(/backend/src/**)",         // edits in the backend code, tests and migrations
                                 // Not build files, CLAUDE.md or settings: those still ask.
"Bash(npm test *)",              // the frontend checks from frontend/CLAUDE.md
"Bash(npm run test *)",
"Bash(npm run format *)",
"Bash(npm run typecheck *)",
"Bash(npx vitest run *)",
"Bash(backend/gradlew -p backend test *)",          // the backend checks, run from the repo root
"Bash(backend/gradlew -p backend spotlessApply *)", // (see excludedCommands below for why)
"Bash(backend/gradlew -p backend spotlessCheck *)",
"Bash(git add *)",               // commit, as CLAUDE.md asks
"Bash(git commit *)"
```

Reading and searching inside the project need no rule: they never ask. Read-only commands such
as `ls`, `cat`, `grep` and `git status` don't ask either.

A `*` at the end, after a space, also matches the bare command: `Bash(npm test *)` matches
`npm test` and `npm test -- money`. Without the `*`, a rule matches one exact command.

These allow rules come from the repo, so Claude Code only uses them after you accept the
"trust this folder" dialog. The deny and ask rules apply straight away.

## `permissions.ask`: always ask a human

```jsonc
"Bash(git push *)"               // pushing is shared and hard to undo, so a person decides
```

## `permissions.deny`: never

```jsonc
"Read(./.env)",                  // secrets. A Read deny also blocks Edit and Write on the file
"Read(./.env.*)",
"Read(**/.env)",
"Bash(rm *)",                    // no deleting files with rm. Ask a human to do it
"Bash(docker compose down *)",   // "down -v" deletes the dev database, and flags can go anywhere,
                                 // so all of "down" is denied. "docker compose stop" is fine
"Bash(*DROP TABLE*)",            // dropping tables, in either case
"Bash(*drop table*)",
"Bash(curl *)",                  // no downloading with curl or wget: that's how "curl ... | sh"
"Bash(wget *)"                   // attacks start
```

A deny rule matches the text of the command, not what it does. `Bash(curl *)` stops
`curl https://x | sh`, but not `python3 -c "urllib..."` or `/usr/bin/curl`. That's why the
sandbox is here too.

## `sandbox`: the limit the operating system enforces

```jsonc
"enabled": true,                     // shell commands run inside the sandbox (macOS, Linux, WSL2)
"autoAllowBashIfSandboxed": true,    // a sandboxed command runs without a prompt, because the
                                     // sandbox contains it. Deny and ask rules still apply
"allowUnsandboxedCommands": false,   // the agent can't ask to retry a blocked command outside
"excludedCommands": [                // the one command that runs outside the sandbox:
  "backend/gradlew -p backend *"     // the backend tests start Postgres in Docker (Testcontainers),
],                                   // and Docker doesn't work in the sandbox. It only matches
                                     // the command on its own: no cd, no pipe
"filesystem": {
  "allowWrite": ["~/.gradle", "~/.npm"]   // caches the build tools write to, outside the project
},
"network": {
  "allowedDomains": [                // the only hosts a sandboxed command can reach
    "registry.npmjs.org",
    "repo.maven.apache.org",
    "plugins.gradle.org",
    "services.gradle.org"
  ]
}
```

By default a sandboxed command can write only inside the project (and a temp folder), can read
most of the computer, and can reach no host until you allow it. Claude's own file tools (Read,
Edit), hooks and MCP servers don't run in the sandbox: permission rules cover those.

The excluded Gradle command runs with your full access, and Gradle runs `build.gradle`. That's
one reason the allow rules let the agent edit `backend/src/` without asking, but not
`backend/build.gradle`.

## Your own exceptions

Put them in `.claude/settings.local.json`, which is git-ignored. It can add allow rules for
things the team file doesn't mention. It can't loosen the team's rules: a deny or ask rule in
any file still wins over your allow.
