#!/bin/bash
# Stop: runs when the agent wants to finish. If it changed code in frontend/ or backend/ during this
# session, the same checks CI runs must pass first. If they fail, the agent goes back to work with the
# output. The way out: after 3 failed tries it lets the agent stop and tells you, so a check the agent
# can't fix can't keep it busy (and spending) forever.
HOOK=require-green-tests; source "$(dirname "$0")/lib.sh"
MAX_TRIES=3
TRIES_FILE="${STATE:?}/${SESSION:?}.tries"
cd "$PROJECT" || exit 0
START=$(cat "$STATE/$SESSION.start" 2>/dev/null || git rev-parse HEAD)
CHANGED=$( (git diff --name-only "$START"; git ls-files --others --exclude-standard) | sort -u)
DIRS=$(grep -oE '^(frontend|backend)/' <<<"$CHANGED" | sort -u | tr -d / | xargs)
if [ -z "$DIRS" ]; then log "no code changes" ""; exit 0; fi
# The same files as the last time the checks passed? Then there's nothing new to check.
TREE=$(GIT_INDEX_FILE="$STATE/index" bash -c 'git read-tree HEAD && git add -A && git write-tree')
if [ -f "$STATE/green-$TREE" ]; then log "already green" "$DIRS"; exit 0; fi
lock; OUT=""; FAIL=""
for d in $DIRS; do
  case $d in
    backend)  o=$(backend/gradlew -p backend spotlessCheck test 2>&1) || FAIL="$FAIL backend" ;;
    frontend) o=$(cd frontend && npm run -s format:check 2>&1 && npm run -s typecheck 2>&1 && npx vitest run 2>&1) || FAIL="$FAIL frontend" ;;
  esac
  OUT="$OUT
== $d ==
$(grep -vE 'JAVA_TOOL_OPTIONS|_JAVA_OPTIONS|^\s*$' <<<"$o" | tail -25)"
done
if [ -z "$FAIL" ]; then
  touch "$STATE/green-$TREE"; rm -f "$TRIES_FILE"; log passed "$DIRS"
  jq -n --arg m "require-green-tests: the checks passed ($DIRS)." '{systemMessage: $m}'; exit 0
fi
TRIES=$(( $(cat "$TRIES_FILE" 2>/dev/null || echo 0) + 1 )); echo $TRIES > "$TRIES_FILE"
if [ $TRIES -gt $MAX_TRIES ]; then
  log "gave up" "${FAIL# } after $MAX_TRIES tries"
  jq -n --arg m "require-green-tests: the checks still fail (${FAIL# }) after $MAX_TRIES tries, so I let the agent stop. Please look at it yourself." '{systemMessage: $m}'; exit 0
fi
log failed "${FAIL# } (try $TRIES of $MAX_TRIES)"
jq -n --arg r "You're not done: the checks fail (${FAIL# }), try $TRIES of $MAX_TRIES. Fix the cause, then finish. Don't change a test just to make it pass.
$OUT" '{decision: "block", reason: $r}'
exit 0
