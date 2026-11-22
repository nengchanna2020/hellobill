#!/bin/bash
# PreToolUse (Edit, Write, Bash): stops two things before they happen, and tells the agent why.
#  1. An edit that writes something that looks like a real secret (an API key, a private key) into any
#     file, or any edit to a secrets file. Permission rules only see a file's name. This reads what's written.
#  2. A shell command on the danger list. A deny rule matches a command's text from the start.
#     This looks anywhere in it, in any case.
# Blocking: print a "deny" decision with a reason. The agent reads the reason and picks another way.
HOOK=block-secrets; source "$(dirname "$0")/lib.sh"
deny() { log blocked "$2"; jq -n --arg r "$1" '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: "deny", permissionDecisionReason: $r}}'; exit 0; }

SECRETS='sk_(live|test)_[0-9A-Za-z]{16,}|AKIA[0-9A-Z]{16}|gh[pousr]_[0-9A-Za-z]{30,}|xox[abprs]-[0-9A-Za-z-]{10,}|-----BEGIN [A-Z ]*PRIVATE KEY-----'
case $(field .tool_name) in
  Edit|Write|MultiEdit)
    FILE=$(rel "$(field .tool_input.file_path)")
    case $FILE in
      *.env.example) ;;
      .env|*/.env|*.env.*|*.pem|*.key|*id_rsa*) deny "$FILE is a secrets file. Agents don't edit those. Tell the user what to put in it, and let them do it." "$FILE" ;;
    esac
    TEXT=$(jq -r '[.tool_input.content, .tool_input.new_string, (.tool_input.edits[]?.new_string)] | map(select(. != null)) | join("\n")' <<<"$INPUT")
    FOUND=$(grep -oE "$SECRETS" <<<"$TEXT" | head -1)
    [ -n "$FOUND" ] && deny "This edit to $FILE contains what looks like a real secret (${FOUND:0:8}...). Secrets never go in the repo, not even test keys. Read it from an environment variable instead, add the name (with no value) to .env.example, and tell the user to put the real value in their own .env." "$FILE: ${FOUND:0:8}..."
    ;;
  Bash)
    CMD=$(field .tool_input.command)
    check() { grep -qiE "$1" <<<"$CMD" && deny "Blocked by the ai-team-kit plugin (block-secrets): $2" "$CMD"; }
    check "$SECRETS"                                    "this command contains what looks like a real secret. Use an environment variable."
    check 'git +push.*(--force|-f( |$)| \+)'            "force-pushing rewrites history other people have. Push normally, or ask the user."
    check 'git .*--no-verify'                           "don't skip git's own hooks. Fix whatever they complain about."
    check 'git +reset +--hard|git +clean +-[a-z]*f'     "this throws away work that isn't committed. Use git stash, or ask the user."
    check '(^|[;&|] *)rm +-[a-z]*r|find .*-delete'      "deleting folders is for a human. Tell the user what to delete and why."
    check 'docker[ -]compose .*down.* -v|docker +volume +rm|docker +system +prune' "this deletes the dev database. Ask the user."
    check '(drop|truncate) +(table|schema|database)'    "dropping data is for a human. Write a migration, or ask the user."
    check '(>|tee |cp |mv |sed -i).*\.claude/(hooks|settings|checks)' "the agent doesn't change its own guards. Tell the user what you'd change."
    check '(curl|wget)[^|]*\| *(sh|bash|zsh|python)'    "never pipe a download into a shell. Download it, show the user, and let them decide."
    ;;
esac
log ok "$(field .tool_name)"
exit 0
