# Shared by every hook in this folder (sourced, not run). Claude Code sends each hook one JSON object
# on stdin: the session id, the event, and for tool events the tool's name and input.
# Needs bash and jq.
INPUT=$(cat)
PROJECT=${CLAUDE_PROJECT_DIR:-$(pwd)}
STATE=$PROJECT/.claude/state          # git-ignored: what the hooks remember during a session
mkdir -p "$STATE"
SESSION=$(jq -r '.session_id // "none"' <<<"$INPUT")
field() { jq -r "$1 // empty" <<<"$INPUT"; }
rel() { local p=${1#"$PROJECT"/}; echo "${p#./}"; }
# One line per hook run in .claude/hooks.log (git-ignored), so you can see what fired and why.
log() { jq -cn --arg hook "$HOOK" --arg result "$1" --arg what "$2" '{t: (now | floor), hook: $hook, result: $result, what: $what}' >> "$PROJECT/.claude/hooks.log"; }
# Gradle and npm in two hooks at once trip over each other, so they take turns.
lock() { until mkdir "$STATE/lock" 2>/dev/null; do sleep 0.3; done; trap 'rmdir "$STATE/lock" 2>/dev/null' EXIT; }
