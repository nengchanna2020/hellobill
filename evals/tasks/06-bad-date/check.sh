#!/bin/bash
# The hidden check: only the check tests (in frontend/src/evalcheck), on the agent's code.
cd frontend && npx vitest run src/evalcheck > ../build-check.log 2>&1; rc=$?
grep -E 'Test Files|Tests |FAIL|AssertionError' ../build-check.log | head -12
[ $rc = 0 ] && echo "check passed" || echo "check failed (exit code $rc)"
exit $rc
