#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
validator="$script_dir/validate-pull-request-branch.sh"

assert_allowed() {
  local base_ref="$1"
  local head_ref="$2"

  if ! bash "$validator" "$base_ref" "$head_ref" >/dev/null 2>&1; then
    echo "Expected to allow: $head_ref -> $base_ref"
    exit 1
  fi
}

assert_blocked() {
  local base_ref="$1"
  local head_ref="$2"

  if bash "$validator" "$base_ref" "$head_ref" >/dev/null 2>&1; then
    echo "Expected to block: $head_ref -> $base_ref"
    exit 1
  fi
}

assert_allowed main release/1.0.0
assert_allowed main hotfix/1.0.1-security-fix
assert_allowed develop feature/003-api-bootstrap
assert_allowed develop docs/update-guide
assert_allowed develop chore/update-tooling
assert_allowed develop fix/quiz-validation

assert_blocked main feature/003-api-bootstrap
assert_blocked main docs/update-guide
assert_blocked main chore/update-tooling
assert_blocked main fix/quiz-validation
assert_blocked main develop

if bash "$validator" main >/dev/null 2>&1; then
  echo "Expected to reject missing arguments"
  exit 1
fi

echo "Pull request branch policy tests passed."
