// Screenshots one HelloBill page at three widths and lists elements wider than the screen.
// Usage (repo root): node .claude/skills/responsive-layout-check/scripts/screens.mjs /invoices/new
import { createRequire } from "node:module";
import { mkdirSync } from "node:fs";
const require = createRequire(new URL("../../../../frontend/package.json", import.meta.url));
const { chromium } = require("@playwright/test");
const path = process.argv[2] || "/";
const base = process.env.BASE_URL || "http://localhost:3000";
const out = ".claude/state/screens";
mkdirSync(out, { recursive: true });
const browser = await chromium.launch({ executablePath: process.env.PLAYWRIGHT_CHROMIUM_PATH || undefined });
let problems = 0;
for (const [name, width] of [["phone", 375], ["tablet", 768], ["desktop", 1280]]) {
  const page = await browser.newPage({ viewport: { width, height: 800 } });
  await page.goto(base + path, { waitUntil: "networkidle" });
  const file = `${out}/${path.replace(/\W+/g, "-").replace(/^-|-$/g, "") || "home"}-${width}.png`;
  await page.screenshot({ path: file, fullPage: true });
  const wide = await page.evaluate(() => {
    const vw = document.documentElement.clientWidth;
    return [...document.querySelectorAll("body *")]
      .filter((el) => el.getBoundingClientRect().right > vw + 1 && !el.closest(".card"))
      .filter((el) => !el.parentElement || el.parentElement.getBoundingClientRect().right <= vw + 1)
      .slice(0, 5)
      .map((el) => `${el.tagName.toLowerCase()}${el.className ? "." + String(el.className).split(" ")[0] : ""} is ${Math.round(el.getBoundingClientRect().right)}px wide`);
  });
  problems += wide.length;
  console.log(`${name.padEnd(7)} ${width}px  ${file}  ${wide.length ? "TOO WIDE: " + wide.join("; ") : "fits"}`);
  await page.close();
}
await browser.close();
console.log(problems ? `${problems} element(s) stick out past the screen` : "LAYOUT OK: nothing sticks out at any width");
process.exit(problems ? 1 : 0);
