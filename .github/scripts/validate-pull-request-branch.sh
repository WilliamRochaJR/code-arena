#!/usr/bin/env bash

set -euo pipefail

base_ref="${1:-}"
head_ref="${2:-}"

if [[ -z "$base_ref" || -z "$head_ref" ]]; then
  echo "::error::Base and head branches are required."
  exit 1
fi

if [[ "$base_ref" != "main" ]]; then
  echo "Branch policy accepted: $head_ref -> $base_ref"
  exit 0
fi

case "$head_ref" in
  release/* | hotfix/*)
    echo "Branch policy accepted: $head_ref -> $base_ref"
    ;;
  *)
    echo "::error::Pull requests to main must come from release/* or hotfix/*. Received: $head_ref -> $base_ref"
    exit 1
    ;;
esac
