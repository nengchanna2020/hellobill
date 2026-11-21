# Testing a hook

A hook is a small program, so test it like one: give it an input and check what it prints. You
don't need the agent for that, and it costs nothing.

## 1. Send it the JSON Claude Code would send

Every hook gets one JSON object on stdin. For a tool event it has the session id, the tool's
name and the tool's input:

```bash
export CLAUDE_PROJECT_DIR=$PWD
echo '{"session_id":"t1","tool_name":"Bash","tool_input":{"command":"psql -c \"DROP SCHEMA public\""}}' \
  | .claude/hooks/block-secrets.sh
```

It prints a `deny` decision with the reason the agent will read. An empty answer means "go ahead".
`kit-tests/hooks/test-hooks.sh` runs thirteen of these at once.

## 2. Know the three ways a hook answers

| The hook | What Claude Code does |
|---|---|
| exits 0 and prints nothing | carries on |
| exits 0 and prints JSON | reads it: `permissionDecision` (PreToolUse), `decision: "block"` with a `reason` (PostToolUse, Stop), `additionalContext` (adds a note for the agent), `systemMessage` (shows a line to you) |
| exits 2 | blocks, and what it wrote to stderr goes to the agent |

Any other exit code is an error: Claude Code shows it, and carries on.

## 3. Read the log

Every hook in the kit writes one line to `.claude/hooks.log`:

```json
{"t":1795286031,"hook":"require-green-tests","result":"failed","what":"frontend (try 1 of 3)"}
```

In Claude Code, `/hooks` lists every hook that's wired up, and `claude --debug` shows each run.

## 4. Watch for the two classic mistakes

- **A Stop hook with no way out.** If the agent can't make the check pass, it can never finish,
  and it spends tokens trying. Ours lets it stop after 3 tries and tells you.
- **A slow hook after every edit.** The whole backend suite takes over a minute. Run only the
  tests for the file that changed, and leave the full run for the Stop hook.

## 5. Remember who runs it

Hooks run on your computer, with your access, outside the sandbox. Read a hook before you turn
it on, the same as any script from the internet.
