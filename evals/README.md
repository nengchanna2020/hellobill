# evals/: does the kit actually help?

An agent does something a bit different every run, so one good try proves nothing. This folder is a small
test set for the kit itself: real HelloBill tasks, each with a hidden check, run headless on a fresh copy
of the repo, for each setup you want to compare.

```
evals/
  tasks/NN-name/task.md      the prompt, exactly what the agent gets
  tasks/NN-name/check/       hidden check tests, copied in only after the run
  tasks/NN-name/check.sh     runs only those check tests; exit code 0 = passed
  tasks/NN-name/given/       (optional) files the agent starts with, like a test it must pass
  setups/none.sh, kit.sh, bloat.sh   change the fresh copy before the run
  eval-settings.json         the same permissions for every setup: edits, the checks, read-only commands
  run.sh                     the loop
  report.py                  results.csv and results.md
```

Run it from the repo root, on committed code (it copies `HEAD`):

```
bash evals/run.sh none          # no CLAUDE.md, no .claude/, no .mcp.json
bash evals/run.sh kit           # the kit as it is
bash evals/run.sh kit 01-late-fee 04-discount-limit     # only some tasks
MODEL=sonnet bash evals/run.sh kit                      # pin the model: compare setups on the same one
```

Each run writes `evals/results/<date>-<setup>/results.md`: passed or not, time, cost, the size of the
first call (how much the setup puts in the context before any work), which skills and agents loaded, and
whether the agent changed a test that was already there. The transcripts (`*.jsonl`) stay on your machine.

## Rules that keep it honest

- **The agent never sees the checks.** `evals/` is removed from the copy, and the check tests are copied
  in after the run, over anything the agent wrote. Changing a test can't make a check pass.
- **Same tasks, same permissions, only the setup changes.** Compare setups on the same day and version.
- **Several tasks, not one.** A handful of tasks is still a small sample: look for big differences.
- **Run it before you change the kit,** like tests before you change code. Then change one thing, and run
  it again.
- **It costs real money.** Every task is a real Claude Code run. Start with a few tasks.

To try a setup without one skill, turn it off for the run instead of deleting it:
`{"skillOverrides": {"code-quality": "off"}}` in the settings file (`"name-only"` keeps the name but drops
the description). When the kit becomes a plugin (Episode 9), `claude plugin eval` can run cases like these
against it, with and without the plugin.
