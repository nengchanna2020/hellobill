---
name: pull-request
description: Writes the title and description of a pull request the HelloBill way, from the branch's commits and diff. Use when asked to write, draft or open a pull request or PR, or to describe a change for review.
---

# Write a pull request

Read what changed first: `git log --oneline main..HEAD` and `git diff --stat main...HEAD`
(on a fresh clone with only `main`, use the commits from this session).

**Title**: under 60 characters, starts with a verb, says what a user gets. "Add discount codes
to invoices", not "Discount code changes" or "Fix stuff".

**Description**, with exactly these headings:

```markdown
## What
One or two sentences a non-developer understands.

## Why
The problem or request it answers. Link the issue if there is one ("Closes #12").

## How
The main pieces, one line each: endpoints, migrations, pages. Name the files.

## How I checked
The commands you ran and what they printed (test counts, not "tests pass").
Screenshots for any page that changed (the responsive-layout-check skill takes them).

## Risks
What could go wrong in production, and how to undo it. "None" is allowed, but say why.
```

Keep it under 40 lines. Write it to `.claude/state/pr.md` and show it to the user. Only open the
pull request on GitHub if the user asks you to (`gh pr create --title "..." --body-file .claude/state/pr.md`).
