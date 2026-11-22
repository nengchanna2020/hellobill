# Which pattern? A cheat sheet

Start with the simplest one that works. One agent plus a reviewer covers most everyday work.

| The job | Pattern | In the kit |
|---|---|---|
| A change you can say in one sentence | one agent, then a review | just ask, then the `reviewer` agent |
| A bug | test first, then the fix, then a review | `/fix <bug>` |
| A feature that touches the API and the web app | a spec, then the team in order | `/spec`, a person approves, `/feature NNN` |
| Two parts that share no files | builders in parallel, each in its own git worktree | `/feature` does it when tasks say `parallel: yes` |
| A big read (a folder, a log) | one helper reads, a short result comes back | "use a subagent to investigate" |
| Many tries at a hard problem | several attempts, one checker picks | not in the kit: costs a lot |

Checks between steps, not trust: the spec is approved, the checks are green, the reviewer quotes its
evidence. A person approves the spec and merges the pull request.

When parallel work breaks, it's usually two builders changing the same file. The fix is in the
planner's instructions (no file in two tasks), not in more agents.

The checks must run where the agents run. If the sandbox can't run your tests, `/feature` stops at
the green check and asks you to run them. That's the right answer, but it costs a round trip each time.
