@AGENTS.md

# Frontend: Next.js 16, React 19, TypeScript

Run from `frontend/`:

- Checks: `npm run format && npm run typecheck && npm test`
- Start the web app: `npm run dev` (port 3000, needs the API on 8080)

Rules:

- Show money with `formatMoney` (`src/lib/money.ts`) and dates with `formatDate` (`src/lib/dates.ts`).
- The invoice preview in `src/lib/invoice-math.ts` must match the backend's `InvoiceCalculator`.
- `AGENTS.md` is written by `next dev`. Leave it as it is, and commit it.
