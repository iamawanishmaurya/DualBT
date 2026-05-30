#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ -n "${GIT_DIR:-}" ]]; then
  GIT_DIR_PATH="$GIT_DIR"
elif [[ -d /tmp/DualBT.git ]]; then
  GIT_DIR_PATH="/tmp/DualBT.git"
elif [[ -d "$ROOT_DIR/.git" ]]; then
  GIT_DIR_PATH="$ROOT_DIR/.git"
else
  GIT_DIR_PATH="/tmp/DualBT.git"
fi
GIT_WORK_TREE_PATH="${GIT_WORK_TREE:-$ROOT_DIR}"
REMOTE_NAME="${REMOTE_NAME:-origin}"
BRANCH_NAME="${1:-implementation/dualbt-v0.1.0}"

git_cmd=(git --git-dir="$GIT_DIR_PATH" --work-tree="$GIT_WORK_TREE_PATH")

if [[ ! -d "$GIT_DIR_PATH" ]]; then
  cat >&2 <<EOF
Git metadata directory not found: $GIT_DIR_PATH

Recreate the external metadata directory first, restore a local .git directory,
or run this script in the same environment where /tmp/DualBT.git already exists.
EOF
  exit 2
fi

if ! getent hosts github.com >/dev/null 2>&1; then
  cat >&2 <<'EOF'
github.com does not resolve from this shell.

Fix DNS/network access first, then rerun this script. This script intentionally
uses plain git only and does not call gh.
EOF
  exit 3
fi

if ! timeout 20 curl --connect-timeout 10 --max-time 20 -fsSIL https://github.com/ >/dev/null; then
  cat >&2 <<'EOF'
github.com resolves, but HTTPS access to GitHub is not reachable from this shell.

Fix outbound network/proxy/firewall access first, then rerun this script. If your
network requires a proxy, configure HTTPS_PROXY and git http.proxy before retrying.
EOF
  exit 4
fi

"${git_cmd[@]}" status --short --branch
"${git_cmd[@]}" push -u "$REMOTE_NAME" "$BRANCH_NAME"
"${git_cmd[@]}" push "$REMOTE_NAME" --tags
"${git_cmd[@]}" status --short --branch
