---
name: ship-check
description: Runs every check CI runs, plus a look at git, and says whether the branch is ready to ship. Run it yourself with /ship-check before you push.
disable-model-invocation: true
allowed-tools: Bash(backend/gradlew -p backend *) Bash(npm --prefix frontend run *) Bash(npm --prefix frontend test) Bash(git status *) Bash(git log *) Bash(git diff *)
---

# Ship check

Run each of these from the repo root, one at a time, and keep the real output:

1. `backend/gradlew -p backend spotlessCheck test 2>&1 | tail -15`
2. `npm --prefix frontend run format:check`
3. `npm --prefix frontend run typecheck`
4. `npm --prefix frontend test`
5. `git status --short` (anything not committed?)
6. `git log --oneline origin/main..HEAD 2>/dev/null || git log --oneline -5` (what would be pushed)

Don't fix anything. Answer with this table and one line under it:

| Check | Result |
|---|---|
| Backend format and tests | ✅ 42 tests passed / ❌ what failed |
| Frontend format | ✅ / ❌ |
| Frontend types | ✅ / ❌ |
| Frontend tests | ✅ 17 passed / ❌ |
| Uncommitted files | none / the list |
| Commits to push | the count, and their subjects |

**Ready to ship: yes** only if every row is green and nothing is uncommitted. Otherwise **no**,
and the first thing to fix.
