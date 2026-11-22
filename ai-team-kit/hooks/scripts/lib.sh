# Shared by every hook in this plugin (sourced, not run). Claude Code sends each hook one JSON object
# on stdin: the session id, the event, and for tool events the tool's name and input.
# Needs bash, jq and git.
INPUT=$(cat)
PROJECT=${CLAUDE_PROJECT_DIR:-$(pwd)}
# What the hooks remember goes in the plugin's own data folder, one folder per project, so the kit
# never adds files to your repo. ${CLAUDE_PLUGIN_DATA} survives plugin updates.
STATE=${CLAUDE_PLUGIN_DATA:-$HOME/.cache/ai-team-kit}/state/$(printf '%s' "$PROJECT" | cksum | cut -d' ' -f1)
mkdir -p "$STATE"
SESSION=$(jq -r '.session_id // "none"' <<<"$INPUT")
field() { jq -r "$1 // empty" <<<"$INPUT"; }
rel() { local p=${1#"$PROJECT"/}; echo "${p#./}"; }
# One line per hook run in hooks.log, so you can see what fired and why.
log() { jq -cn --arg hook "$HOOK" --arg result "$1" --arg what "$2" '{t: (now | floor), hook: $hook, result: $result, what: $what}' >> "$STATE/hooks.log"; }
lock() { until mkdir "$STATE/lock" 2>/dev/null; do sleep 0.3; done; trap 'rmdir "$STATE/lock" 2>/dev/null' EXIT; }
# The project tells the kit its checks in .claude/checks.json: {"checks": [{"name", "when", "run"}]}.
# "when" is a path prefix ("" means any change). The kit has no idea how your project builds; this file does.
CHECKS=$PROJECT/.claude/checks.json
