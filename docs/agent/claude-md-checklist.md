# What belongs in CLAUDE.md: a checklist

Use it when you write a new CLAUDE.md, and again every few weeks.

**Keep a line when the agent can't guess it**
- [ ] Commands it would get wrong or not run: the checks CI runs, one test, how to start the app.
- [ ] Rules that aren't visible in the code: "never `double` for money", "never edit an old migration".
- [ ] What "done" means for your team: which checks, what kind of test, when to commit.
- [ ] Dangerous things to ask about first: "`docker compose down -v` deletes the database".

**Cut a line when**
- [ ] It could find it by reading the code, and it would actually go and look. (The test from the Claude Code docs.)
- [ ] The README or the code already says it, and the agent reads them anyway.
- [ ] It's only true for one folder: move it to that folder's `CLAUDE.md`, which loads only when the agent works there.
- [ ] It's long background: move it to `docs/agent/` and link to it. A plain link is read only when needed.
  An `@import` is loaded every time, at launch.

**Check**
- [ ] Under 200 lines per file (the docs' advice). Ours: 22 lines at the root.
- [ ] Run `/context` or `/memory` and see which files actually loaded.
- [ ] No rule is in two places. If two files disagree, Claude may pick either one.
- [ ] No "IMPORTANT" or capitals to make a rule stick. A rule that must never bend goes in a
      permission rule (Episode 2) or a hook (Episode 3).
- [ ] Personal taste goes in `~/.claude/CLAUDE.md` or `CLAUDE.local.md`, not the team's file.
