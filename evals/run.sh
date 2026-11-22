#!/bin/bash
# Runs the test set: every task in evals/tasks, for one setup, each in a fresh copy of the repo.
#   bash evals/run.sh SETUP [TASK ...]      SETUP: none, kit or bloat (evals/setups/SETUP.sh)
#   MODEL=sonnet bash evals/run.sh kit        # pin the model, so setups are compared on the same one
#   SETTINGS=evals/tasks/07-late-fee-given-test/locked-test-settings.json bash evals/run.sh kit 07-late-fee-given-test
# For each task: copy the committed repo, apply the setup, remove evals/ (the agent never sees the tasks or
# the checks), add the task's given/ files, run Claude Code headless (claude -p) with the task, then copy the
# hidden check tests over the result and run check.sh. Results: evals/results/<date>-<setup>/results.csv,
# the transcripts (*.jsonl, not committed) and each check's output.
set -u
ROOT=$(git rev-parse --show-toplevel); EV=$ROOT/evals; SETUP=${1:?usage: run.sh SETUP [TASK ...]}; shift
TASKS=${*:-$(ls "$EV/tasks")}
OUT=$EV/results/${RESULTS_NAME:-$(date +%Y%m%d)-$SETUP}; mkdir -p "$OUT"
S=$(realpath "${SETTINGS:-$EV/eval-settings.json}")
W=${EVAL_WORK:-/tmp/hellobill-eval}/hellobill            # the same path every time, so it's trusted once
[ -f "$OUT/results.csv" ] || echo "task,passed,seconds,cost_usd,turns,first_call_tokens,skills,agents,test_lines" > "$OUT/results.csv"
for t in $TASKS; do
  rm -rf "$W"; mkdir -p "$W"
  git -C "$ROOT" archive HEAD | tar -x -C "$W"
  [ -d "$ROOT/frontend/node_modules" ] && cp -r "$ROOT/frontend/node_modules" "$W/frontend/"
  bash "$EV/setups/$SETUP.sh" "$W"
  rm -rf "$W/evals"
  [ -d "$EV/tasks/$t/given" ] && cp -r "$EV/tasks/$t/given/." "$W/"
  (cd "$W" && git init -q && git add -A && git commit -qm "eval start: $t ($SETUP)")
  start=$(date +%s)
  (cd "$W" && timeout "${TMO:-1500}" claude -p "$(cat "$EV/tasks/$t/task.md")" --settings "$S" ${MODEL:+--model "$MODEL"} \
     --output-format stream-json --verbose < /dev/null > "$OUT/$t.jsonl" 2>&1)
  secs=$(( $(date +%s) - start ))
  # Test lines added and removed, against the start (the agent may have committed): "+40 -0" is good,
  # lines removed from tests that were already there are worth a look.
  tests=$(cd "$W" && git add -A >/dev/null 2>&1; git diff --cached --numstat "$(git rev-list --max-parents=0 HEAD)" | awk '$3 ~ /src\/test\// || $3 ~ /\.test\.tsx?$/ {a+=$1; d+=$2} END {printf "+%d -%d", a, d}')
  cp -r "$EV/tasks/$t/check/." "$W/"
  (cd "$W" && bash "$EV/tasks/$t/check.sh") > "$OUT/$t.check" 2>&1 && passed=yes || passed=no
  python3 "$EV/report.py" row "$OUT/$t.jsonl" "$t" "$passed" "$secs" "$tests" >> "$OUT/results.csv"
  tail -1 "$OUT/results.csv"
done
python3 "$EV/report.py" table "$OUT/results.csv" > "$OUT/results.md"
