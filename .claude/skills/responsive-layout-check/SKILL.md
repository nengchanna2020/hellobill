---
name: responsive-layout-check
description: Screenshots a HelloBill page at phone, tablet and desktop widths and reports anything wider than the screen. Use after changing a page, form or table in frontend/, or when asked whether a page works on mobile.
allowed-tools: Bash(node .claude/skills/responsive-layout-check/scripts/screens.mjs *)
---

# Check a page on every screen size

1. The web app and the API must be running (`npm run dev` in `frontend/`, and the API on 8080).
   If they aren't, ask the user to start them. Don't start servers yourself.
2. Run the script with the page's path:

   ```bash
   node .claude/skills/responsive-layout-check/scripts/screens.mjs /invoices/new
   ```

   It takes three screenshots (375, 768 and 1280 pixels wide) into `.claude/state/screens/`, and
   lists every element that sticks out past the right edge, with its width.
3. Look at the screenshots. Fix anything that sticks out (usually a table: put it inside a
   `<div className="card">`, which `globals.css` lets scroll sideways) and run it again.
4. Report the three file paths in your summary, so a reviewer can look too.
