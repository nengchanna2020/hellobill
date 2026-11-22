#!/bin/bash
# Setup "bloat": the kit plus three "helpful" skills with vague descriptions and one subagent that says
# "use proactively for every task". This is the mistake the episode measures; don't copy it.
cp -r "$(dirname "$0")/bloat/claude/." "$1/.claude/"
