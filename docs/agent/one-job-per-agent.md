# One job per agent: a checklist

Before you add a subagent:

- [ ] **Is it a real job?** Something you hand to a teammate with a sticky note: plan, build, test,
      review, check security. Not "helpers".
- [ ] **Does the description say when?** "Use before committing", "Use when asked to plan". The main
      session picks a helper from its description alone.
- [ ] **Least tools.** A reviewer or a planner doesn't need Edit or Write. A tester writes tests, not code.
- [ ] **The right model.** Quick, narrow jobs on haiku; writing code on the session's model.
- [ ] **Evidence, not summaries.** Anything that approves must quote what it ran: test counts,
      skipped tests, the real result lines.
- [ ] **Only real problems.** Tell reviewers to flag bugs and missed requirements, not style. Reviewers
      over-report, and that pushes you into over-engineering.
- [ ] **Do you need it?** One agent is often enough. Add a helper when a job needs different tools,
      a different model, or a clean context.
