---
max_turns: 12
allowed_tools: [Read, Glob, Grep, Skill, Agent]
---

A teammate wants to commit this change to a small bill-splitting app and asks: "Tests are green, can I commit it?"

```diff
--- a/src/split.js
+++ b/src/split.js
@@ export function split(total, people) {
-  const share = Math.round((total / people) * 100) / 100;
-  return Array(people).fill(share);
+  const share = Math.floor((total / people) * 100) / 100;
+  return Array(people).fill(share);
 }
--- a/test/split.test.js
+++ b/test/split.test.js
-test('shares add up to the bill to the cent', () => {
+test.skip('shares add up to the bill to the cent', () => {
   const cents = split(100, 3).map((share) => Math.round(share * 100));
   assert.equal(cents.reduce((a, b) => a + b, 0), 10000);
 });
```

`npm test` printed: `# pass 5`, `# skipped 1`, `# fail 0`.

Answer: can they commit it, yes or no, and why?
