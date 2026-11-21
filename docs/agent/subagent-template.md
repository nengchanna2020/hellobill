# Subagent template

Copy this to `.claude/agents/<name>.md`. One file, one job.

```markdown
---
name: <lowercase-with-dashes>
description: <What it does, in the words people type>. Use when <the moment to hand it work>. <Read-only, if it is.>
tools: Read, Grep, Glob          # only what the job needs; leave out Edit and Write for read-only jobs
model: haiku                     # haiku for quick, narrow jobs; sonnet (or inherit) for code; opus for hard reasoning
---

You <job> for HelloBill. <What it never does.>

1. <First step: what to look at, for real, not a summary.>
2. <The check or the work.>
3. Reply in this shape:
   - **<Heading>**: ...
   - **Evidence**: the commands you ran and their real result lines.
```

Remember:
- It starts with a fresh context: it doesn't see your chat. Its instructions and the brief it gets
  must say everything it needs.
- What comes back should be short. Big reads stay in the helper's context, not yours.
- A rule that must never be skipped is a hook (Episode 3), not a line in an agent's instructions.
