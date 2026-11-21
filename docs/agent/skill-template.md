# Skill template

Copy this into `.claude/skills/<name>/SKILL.md`. The folder name is the skill's name: lower case,
with hyphens.

```markdown
---
name: <name>
description: <What it does, in the words a developer would type>. Use when <the requests it is for>.
# Optional:
# disable-model-invocation: true     only when you type /<name> (for jobs with side effects)
# user-invocable: false              background know-how: the agent loads it, you can't type it
# allowed-tools: Bash(bash .claude/skills/<name>/scripts/check.sh)   tools it may use without asking
---

# <Title>

1. The step that matters most, first.
2. The next step. Link the details instead of pasting them: "copy the shape of
   `templates/Thing.java`", "the full list is in `reference.md`".
3. How to check the result: a command and what it prints when it's right.
```

Rules that keep a skill good:

- **The description does the work.** The agent sees only names and descriptions until a task
  matches. Say what it does and when to use it, main use first, in words people really type.
  "Backend helpers." never loads; "Adds a REST endpoint ... Use when asked to add an endpoint" does.
- **Keep `SKILL.md` short**: under 500 lines, the must-follow steps first.
- **Details in other files** (`templates/`, `reference.md`, `examples/`). They're read only when needed.
- **Repeatable steps in `scripts/`.** A script runs; its text never fills the context. Give it a
  narrow allow rule, not `Bash(*)`.
- **A rule that must never be skipped is a hook, not a skill.** A skill is advice the agent loads.
- **Test it**: the same prompt in fresh sessions with and without the skill. Check two things
  separately: did it load when it should (and not when it shouldn't), and was the result better?
