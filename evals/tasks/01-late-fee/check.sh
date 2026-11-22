#!/bin/bash
# The hidden check: only the check tests (in com.hellobill.evalcheck), on the agent's code.
backend/gradlew -q -p backend test --tests 'com.hellobill.evalcheck.*' > build-check.log 2>&1; rc=$?
grep -E 'tests completed|FAILED|error:|expected|Expect' build-check.log | grep -v 'JAVA_' | head -12
[ $rc = 0 ] && echo "check passed" || echo "check failed (exit code $rc)"
exit $rc
