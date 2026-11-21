# Should this be a skill?

Ask these in order. Stop at the first yes.

1. **Must it happen every time, with no exceptions?** (formatting, no secrets, tests before done)
   → a **hook** (`.claude/hooks/`). A skill can be skipped; a hook can't.
2. **Is it one short rule for every change?** (money is `BigDecimal`, never edit an old migration)
   → one line in **`CLAUDE.md`**. If a line wouldn't change what the agent does, cut it.
3. **Is it how to do one kind of job, with steps, templates or a script?** (add an endpoint, write
   a migration, write a PR) → a **skill** the agent loads when a task matches.
4. **Is it a job with side effects that you want to start yourself?** (ship check, release, deploy)
   → a **skill with `disable-model-invocation: true`**, typed as `/name`.
5. **Is it background facts the agent needs sometimes, but nobody would type?** (how the billing
   rules work) → a **skill with `user-invocable: false`**, or a file in `docs/agent/` that
   `CLAUDE.md` points to.
6. **Has it happened only once?** → nothing yet. Write the skill the second or third time.

Before you commit a skill:

- [ ] The description says what it does and when to use it, in the words people type.
- [ ] It loads for the prompts it's for, and not for others (try three of each in fresh sessions).
- [ ] The result is better with it than without it, on the same prompt.
- [ ] `SKILL.md` is under 500 lines, the main steps first, details in linked files.
- [ ] Every script has a narrow allow rule, and you've read every line of it.
