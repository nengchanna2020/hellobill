---
name: fix
description: Fixes a bug test-first with the agent team: a failing test, then the fix, then a review. Type /fix <the bug>.
disable-model-invocation: true
---

# /fix: test first, then the fix

1. **Reproduce.** Ask the `tester` agent to write one test that shows the bug with real values, and
   to run it. **Check: it fails, for the reason in the bug report.** If it passes, stop: tell me the
   bug didn't reproduce, and how you tried.
2. **Fix.** Ask the `builder` agent for the smallest change that makes that test pass, without
   changing the test. **Check: green**, every check for the changed folders.
3. **Review.** Ask the `reviewer` agent to check the fix and the test against the bug report.
   **Check:** verdict "approve", with evidence.
4. Commit once, with a subject like "Fix ...", and tell me: the test, the cause, the fix, the checks.
