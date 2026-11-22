# Tasks for 001

## T1: backend auto-send
- owner: builder
- files: backend/src/main/resources/db/migration/V7__add_recurring_plan_auto_send.sql (new), backend/src/main/java/com/hellobill/recurring/RecurringPlan.java, RecurringPlanRequest.java, RecurringPlanResponse.java, RecurringBillingService.java, RecurringPlanController.java, AutoSendRequest.java (new), backend/src/test/java/com/hellobill/recurring/RecurringBillingTest.java, docs/api.md
- parallel: yes
- done when: `backend/gradlew -p backend spotlessApply test` ends with BUILD SUCCESSFUL

## T2: frontend auto-send
- owner: builder
- files: frontend/src/lib/types.ts, frontend/src/app/recurring/page.tsx, frontend/src/app/recurring/page.test.tsx (new)
- parallel: yes
- note: read `frontend/node_modules/next/dist/docs/` before coding, per frontend/AGENTS.md.
- done when: `npm --prefix frontend run format && npm --prefix frontend run typecheck && npm --prefix frontend test` all pass
