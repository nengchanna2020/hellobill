#!/bin/bash
# Stop, first version: "you can't finish until everything is green". Every check, every time,
# the end-to-end tests too. No way out.
HOOK=require-green-tests; source "$(dirname "$0")/lib.sh"
cd "$PROJECT" || exit 0
lock
OUT=$( (backend/gradlew -p backend spotlessCheck test && cd frontend && npm run -s format:check && npm run -s typecheck && npx vitest run && npm run -s test:e2e) 2>&1 )
if [ $? = 0 ]; then log passed "everything"; exit 0; fi
log failed "everything"
jq -n --arg r "You're not done: the checks fail. Fix them, then finish.
$(grep -vE 'JAVA_TOOL_OPTIONS|_JAVA_OPTIONS|^\s*$' <<<"$OUT" | tail -25)" '{decision: "block", reason: $r}'
