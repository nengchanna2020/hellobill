#!/bin/bash
# PreToolUse (Edit, Write, Bash): while a spec is open, no code until a person approves it.
#  1. An edit outside specs/ and docs/ while the newest spec in specs/ isn't "Status: approved".
#     With no spec folder at all, nothing is blocked: everyday work doesn't need a spec.
#  2. Any edit or command that would approve a spec. Only a person changes the Status line.
HOOK=require-approved-spec; source "$(dirname "$0")/lib.sh"
deny() { log blocked "$2"; jq -n --arg r "$1" '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $r}}'; exit 0; }
SPEC=$(ls -d "$PROJECT"/specs/[0-9][0-9][0-9]-*/spec.md 2>/dev/null | sort | tail -1)
case $(field .tool_name) in
  Edit|Write|MultiEdit)
    FILE=$(rel "$(field .tool_input.file_path)")
    TEXT=$(jq -r '[.tool_input.content, .tool_input.new_string, (.tool_input.edits[]?.new_string)] | map(select(. != null)) | join("\n")' <<<"$INPUT")
    case $FILE in
      *specs/*/spec.md) grep -qiE '^ *Status: *approved' <<<"$TEXT" && deny "Only a person approves a spec. Leave the Status line as it is, and ask the user to read $FILE and approve it." "$FILE" ;;
      specs/*|docs/*|*.md) ;;
      *)
        if [ -n "$SPEC" ] && ! grep -qiE '^ *Status: *(approved|done)' "$SPEC"; then
          deny "$(rel "$SPEC") isn't approved yet, so code can't change. Ask the user to read the spec and change its Status line to approved. Then build it with /ai-team-kit:feature." "$FILE"
        fi ;;
    esac ;;
  Bash)
    CMD=$(field .tool_input.command)
    grep -qiE 'status: *approved' <<<"$CMD" && grep -qi 'spec' <<<"$CMD" && deny "Only a person approves a spec. Ask the user to change the Status line." "$CMD" ;;
esac
log ok "$(field .tool_name)"
exit 0
