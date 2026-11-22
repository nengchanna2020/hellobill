---
name: feature
description: Builds an approved spec with the agent team, step by step, with a check between each step. Type /ai-team-kit:feature NNN.
disable-model-invocation: true
---

# /feature NNN: the team, in order

Work from `specs/NNN-*/`. Every step reads the same spec. Stop at the first check that fails and tell me
why; don't skip ahead.

1. **Check: approved.** Read `spec.md`. If the Status line isn't `Status: approved`, stop and say so.
   Never change the Status line yourself.
2. **Plan.** Ask the `ai-team-kit:planner` agent for a plan and tasks from the spec (and `specs/constitution.md`, if there is one).
   Tasks must not share files. Mark the ones that can run in parallel. Write its answer to
   `plan.md` and `tasks.md` in the spec folder (templates: `${CLAUDE_SKILL_DIR}/../spec/templates/`).
3. **Build.** For each task, hand it to a `ai-team-kit:builder` agent with the spec path, the task, and its files.
   Tasks marked parallel go to separate builders at the same time, each in its own git worktree
   (`isolation: worktree`). A worktree starts from the last commit, so it doesn't have the new
   `plan.md` and `tasks.md`: give each builder their full paths in this checkout.
   Then bring their changes into this checkout and run every check in `.claude/checks.json`.
   - **Check: green.** All checks for every changed folder pass. If not, send the failure back to
     the builder that owns the file, once. Still red: stop.
4. **Test.** Ask the `ai-team-kit:tester` agent to write a test for each acceptance criterion that isn't covered,
   and to try to break the change. **Check: green** again.
5. **Review.** Ask the `ai-team-kit:reviewer` agent to check the change against the spec (and the constitution, if there is one).
   **Check:** verdict "approve", with evidence. If "changes needed", fix once and review again.
6. **Hand over.** Commit once. Then write the pull request title and description in your answer
   (the `ai-team-kit:pull-request` skill's format), with each acceptance criterion and the test that covers it.
   Don't push and don't open the pull request: a person does that.
