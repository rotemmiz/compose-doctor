#!/usr/bin/env bash
# Single command to bump the release version everywhere it's hard-coded.
# Usage:  scripts/set-version.sh 0.1.1
#
# build.gradle.kts is the conceptual source of truth; the manifests, init script, Gemini
# extension, and the site snippet just mirror it. scripts/check-version.sh guards against drift.
set -euo pipefail

new="${1:-}"
if ! [[ "$new" =~ ^[0-9]+\.[0-9]+\.[0-9]+([-+][0-9A-Za-z.]+)?$ ]]; then
  echo "usage: scripts/set-version.sh <version>   (e.g. 0.1.1; no -SNAPSHOT — the Portal rejects it)" >&2
  exit 1
fi
case "$new" in *SNAPSHOT*) echo "refusing -SNAPSHOT: the Gradle Plugin Portal only accepts final versions" >&2; exit 1;; esac

cd "$(dirname "$0")/.."

# Portable in-place sed (GNU vs BSD/macOS).
sed_i() { if sed --version >/dev/null 2>&1; then sed -i "$@"; else sed -i '' "$@"; fi; }

sed_i -E 's/^( *version = )"[^"]+"/\1"'"$new"'"/'                        build.gradle.kts
sed_i -E 's/("version": *)"[^"]+"/\1"'"$new"'"/g'                        .claude-plugin/plugin.json
sed_i -E 's/("version": *)"[^"]+"/\1"'"$new"'"/g'                        .claude-plugin/marketplace.json
sed_i -E 's/("version": *)"[^"]+"/\1"'"$new"'"/'                         gemini-extension.json
sed_i -E 's/(dev\.composedoctor\.gradle\.plugin:)[0-9][^"]*/\1'"$new"'/' init/compose-doctor.init.gradle.kts
sed_i -E 's/(version <span[^>]*>)"[^"]+"/\1"'"$new"'"/'                  site/index.html

echo "Set version to $new. Changed files:"
git diff --name-only
echo
echo "Next: scripts/check-version.sh && git commit"
