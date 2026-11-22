`formatDate` in `frontend/src/lib/dates.ts` shows "Invalid Date" when it gets a date it can't read, like "2026-13-45" or "soon". Make it show "–" instead, the same as for a missing date.
