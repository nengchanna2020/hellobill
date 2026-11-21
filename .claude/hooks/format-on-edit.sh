#!/bin/bash
# PostToolUse (Edit, Write): formats the file the agent just changed, the same way CI checks it.
# TypeScript, CSS, JSON and Markdown in frontend/: Prettier. Java in backend/: Spotless, on that one file.
# It never blocks. The agent's next read of the file sees the formatted version.
HOOK=format-on-edit; source "$(dirname "$0")/lib.sh"
FILE=$(field .tool_input.file_path); R=$(rel "$FILE")
[ -f "$FILE" ] || exit 0
before=$(cksum < "$FILE")
case $R in
  frontend/*.ts|frontend/*.tsx|frontend/*.js|frontend/*.mjs|frontend/*.css|frontend/*.json|frontend/*.md)
    (cd "$PROJECT/frontend" && npx prettier --write --log-level warn "${R#frontend/}") >/dev/null 2>&1 ;;
  backend/*.java)
    lock; "$PROJECT/backend/gradlew" -q -p "$PROJECT/backend" spotlessApply "-PspotlessIdeHook=$PROJECT/$R" >/dev/null 2>&1 ;;
  *) exit 0 ;;
esac
if [ "$before" = "$(cksum < "$FILE")" ]; then log unchanged "$R"; else log formatted "$R"; fi
exit 0
