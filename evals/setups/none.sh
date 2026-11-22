#!/bin/bash
# Setup "none": HelloBill with no kit at all. No CLAUDE.md, no .claude/ (rules, hooks, skills, agents),
# no .mcp.json, no specs/. The app code and its tests are the same as in the other setups.
cd "$1" && rm -rf .claude CLAUDE.md backend/CLAUDE.md frontend/CLAUDE.md .mcp.json specs docs/agent kit-tests
