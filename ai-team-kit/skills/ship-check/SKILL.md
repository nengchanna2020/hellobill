---
name: ship-check
description: Runs every check in .claude/checks.json, plus a look at git, and says whether the branch is ready to ship. Run it yourself with /ai-team-kit:ship-check before you push.
disable-model-invocation: true
---

# Ship check

1. Read `.claude/checks.json`. For each entry, run its `run` command from the repo root, one at a time,
   and keep the real result lines. No file? Say so: this project hasn't told the kit its checks.
2. `git status --short` (anything not committed?)
3. `git log --oneline origin/main..HEAD 2>/dev/null || git log --oneline -5` (what would be pushed)

Don't fix anything. Answer with a table, one row per check plus "Uncommitted files" and "Commits to
push", each ✅ with the real numbers (like "42 tests passed") or ❌ with what failed.

**Ready to ship: yes** only if every row is green and nothing is uncommitted. Otherwise **no**,
and the first thing to fix.
