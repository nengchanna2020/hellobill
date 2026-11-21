#!/bin/bash
# SessionStart: gives the agent a few facts it would otherwise spend tool calls finding out: the branch,
# the last commit, uncommitted files, and the open GitHub issues (if the gh CLI is signed in).
# It also notes where the session started, so require-green-tests.sh knows what changed since.
HOOK=session-context; source "$(dirname "$0")/lib.sh"
cd "$PROJECT" || exit 0
git rev-parse HEAD > "$STATE/$SESSION.start" 2>/dev/null; rm -f "${STATE:?}/${SESSION:?}.tries"
CTX="Branch: $(git branch --show-current). Last commit: $(git log -1 --format='%h %s (%cr)').
Uncommitted files: $(git status --porcelain | wc -l | tr -d ' ')."
if command -v gh >/dev/null && gh auth status >/dev/null 2>&1; then
  CTX="$CTX
Open GitHub issues:
$(gh issue list --state open --limit 10 2>/dev/null || echo '(could not read them)')"
else
  CTX="$CTX
GitHub issues: not available here (the gh CLI isn't signed in)."
fi
CTX="$CTX
Hooks are on in this repo: after every edit a formatter and that file's tests run, and the checks must pass before you finish."
log ok "$(git branch --show-current)"
jq -n --arg c "$CTX" '{hookSpecificOutput: {hookEventName: "SessionStart", additionalContext: $c}}'
