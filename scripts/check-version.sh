#!/usr/bin/env bash
# Fails if any hard-coded version reference has drifted from build.gradle.kts.
# Run locally or in CI after a bump (scripts/set-version.sh).
set -euo pipefail
cd "$(dirname "$0")/.."

v="$(sed -nE 's/^ *version = "([^"]+)".*/\1/p' build.gradle.kts | head -1)"
[ -n "$v" ] || { echo "could not read version from build.gradle.kts" >&2; exit 1; }

fail=0
want() { # file  grep-pattern (extended regex)
  if ! grep -Eq "$2" "$1"; then echo "✗ $1 — expected version $v"; fail=1; fi
}

want .claude-plugin/plugin.json            "\"version\": *\"$v\""
want .claude-plugin/marketplace.json       "\"version\": *\"$v\""
want gemini-extension.json                 "\"version\": *\"$v\""
want init/compose-doctor.init.gradle.kts   "dev\.composedoctor\.gradle\.plugin:$v"
want site/index.html                       "version <span[^>]*>\"$v\""

if [ "$fail" -eq 0 ]; then
  echo "✓ all version references match build.gradle.kts ($v)"
else
  echo
  echo "Version drift. Run: scripts/set-version.sh $v" >&2
  exit 1
fi
