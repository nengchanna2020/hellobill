---
name: spec
description: Turns a feature idea or issue into a spec with acceptance criteria, after asking a few short questions. Type /ai-team-kit:spec <idea>.
disable-model-invocation: true
---

# /spec: write a spec before anyone builds

1. Read `specs/constitution.md` if the project has one, and the code the idea touches (only what you need).
2. **Interview first.** If anything that changes the behaviour is unclear, ask me up to 5 short,
   numbered questions, each with your suggested answer, and stop. Don't write the spec yet.
3. When the questions are answered (or there were none), create `specs/NNN-short-name/spec.md` from
   the template `${CLAUDE_SKILL_DIR}/templates/spec.md`. NNN is the next free number, starting at 001.
   - Acceptance criteria: testable, with real values.
   - Interfaces: exact names (endpoints, functions, fields), so parts can be built in parallel.
   - Leave `Status: draft`. Only a person approves a spec.
4. Reply with the spec's path, the acceptance criteria in one line each, and: "Read it, and change the
   Status line to approved when you're happy. Then type /ai-team-kit:feature NNN."
