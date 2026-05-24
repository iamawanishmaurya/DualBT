#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WATCHDOG="$ROOT_DIR/scripts/codex-watchdog.sh"
TMP_DIR="$(mktemp -d)"
LOG_FILE="$TMP_DIR/watchdog.log"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

bash -n "$WATCHDOG"

"$WATCHDOG" \
  --interval 1 \
  --count 2 \
  --log "$LOG_FILE" \
  --status-command "printf ok"

line_count="$(wc -l < "$LOG_FILE" | tr -d ' ')"
if [[ "$line_count" != "2" ]]; then
  echo "Expected 2 heartbeat lines, got $line_count" >&2
  exit 1
fi

if ! rg -q 'codex_watchdog heartbeat' "$LOG_FILE"; then
  echo "Missing heartbeat marker" >&2
  exit 1
fi

if ! rg -q 'status=ok' "$LOG_FILE"; then
  echo "Missing successful status field" >&2
  exit 1
fi

FAIL_LOG="$TMP_DIR/watchdog-fail.log"
set +e
"$WATCHDOG" \
  --once \
  --log "$FAIL_LOG" \
  --status-command "exit 7"
fail_exit="$?"
set -e

if [[ "$fail_exit" != "7" ]]; then
  echo "Expected failing status command to return 7, got $fail_exit" >&2
  exit 1
fi

if ! rg -q 'status=fail exit=7' "$FAIL_LOG"; then
  echo "Missing failed status field with original exit code" >&2
  exit 1
fi
