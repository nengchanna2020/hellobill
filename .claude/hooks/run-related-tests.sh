#!/bin/bash
# PostToolUse (Edit, Write): runs only the tests for the file the agent just changed, so feedback takes
# seconds, not the minute or more the whole backend suite takes.
#   frontend/src/lib/money.ts              -> frontend/src/lib/money.test.ts (Vitest, one file)
#   backend/.../invoice/InvoiceCalculator.java -> InvoiceCalculatorTest (JUnit, one class)
#   backend/.../customer/CustomerService.java  -> no CustomerServiceTest, so the tests in its package
# Passed: a one-line note goes back to the agent. Failed: the end of the output goes back, and the
# agent has to deal with it before it moves on.
HOOK=run-related-tests; source "$(dirname "$0")/lib.sh"
FILE=$(field .tool_input.file_path); R=$(rel "$FILE")
note() { jq -n --arg c "$1" '{hookSpecificOutput: {hookEventName: "PostToolUse", additionalContext: $c}}'; exit 0; }
case $R in
  frontend/src/*.ts|frontend/src/*.tsx)
    T=$R; [[ $R == *.test.ts* ]] || T=$(cd "$PROJECT" && ls "${R%.*}".test.ts* 2>/dev/null | head -1)
    if [ -z "$T" ]; then log "no test" "$R"; note "There's no test file next to $R. If you changed behaviour, add one (docs/agent/testing.md)."; fi
    lock; OUT=$(cd "$PROJECT/frontend" && npx vitest run "${T#frontend/}" 2>&1); CODE=$? ;;
  backend/src/main/java/*.java|backend/src/test/java/*.java)
    N=$(basename "$R" .java); [[ $N == *Test ]] || N=${N}Test
    PKG=$(dirname "${R#backend/src/*/java/}" | tr / .)
    if [ -f "$PROJECT/backend/src/test/java/${PKG//.//}/$N.java" ]; then T="$PKG.$N"
    elif ls "$PROJECT/backend/src/test/java/${PKG//.//}/"*Test.java >/dev/null 2>&1; then T="$PKG.*"
    else log "no test" "$R"; note "There are no tests for $R's package. If you changed behaviour, add one (docs/agent/testing.md)."; fi
    lock; OUT=$("$PROJECT/backend/gradlew" -p "$PROJECT/backend" test --tests "$T" 2>&1); CODE=$? ;;
  *) exit 0 ;;
esac
if [ $CODE = 0 ]; then log passed "$T"; note "Related tests passed after your edit to $R: $T."; fi
log failed "$T"
jq -n --arg r "Related tests FAILED after your edit to $R ($T). Fix this before you go on. Change a test only if the test itself is wrong.
$(grep -vE 'JAVA_TOOL_OPTIONS|_JAVA_OPTIONS|^\s*$' <<<"$OUT" | tail -30)" '{decision: "block", reason: $r}'
exit 0
