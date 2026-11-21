# Is this MCP server safe to add?

Ask these before you put a server in `.mcp.json`. Every teammate will run it, with their access.

- [ ] **Who made it?** The service's own company (GitHub, Microsoft's Playwright), or someone you
      can check? Is it maintained, or archived?
- [ ] **What does it run?** A pinned version (`@playwright/mcp@0.0.86`), not "latest", so an update
      can't change it under you.
- [ ] **What can it reach?** Give it the least access that does the job: a read-only token, a
      read-only database login, one repo, not all of them.
- [ ] **Where do secrets come from?** `${VAR}` from each person's environment. Never a token in the file.
- [ ] **Which tools does it add?** Run `/mcp` and read the list. Allow the reading tools by name,
      ask for the ones that write, deny the ones that can't be undone.
- [ ] **What will it read into the context?** Issues, web pages and rows can hold instructions
      written by strangers. Could one of them make the agent do something your rules don't stop?
- [ ] **Do you need it?** If a CLI like `gh` or `psql` does the job and works on every laptop, the
      docs say a CLI is more context-efficient than a server: it adds no tool definitions. Turn off
      servers a task doesn't need (`/mcp`).
