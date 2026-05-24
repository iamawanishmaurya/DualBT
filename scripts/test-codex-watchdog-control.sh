#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CONTROL="$ROOT_DIR/scripts/codex-watchdog-control.sh"
TMP_DIR="$(mktemp -d)"
LOG_FILE="$TMP_DIR/watchdog.log"
PID_FILE="$TMP_DIR/watchdog.pid"

cleanup() {
  if [[ -f "$PID_FILE" ]]; then
    "$CONTROL" stop --pid "$PID_FILE" --log "$LOG_FILE" >/dev/null 2>&1 || true
  fi
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

bash -n "$CONTROL"

if "$CONTROL" status --pid "$PID_FILE" --log "$LOG_FILE" >/dev/null 2>&1; then
  echo "Expected missing watchdog to report not running" >&2
  exit 1
fi

"$CONTROL" start --interval 1 --pid "$PID_FILE" --log "$LOG_FILE" >/dev/null
sleep 2

"$CONTROL" status --pid "$PID_FILE" --log "$LOG_FILE" | rg -q 'status=running'

if [[ ! -s "$LOG_FILE" ]]; then
  echo "Expected watchdog log to contain heartbeats" >&2
  exit 1
fi

rg -q 'codex_watchdog heartbeat' "$LOG_FILE"

"$CONTROL" stop --pid "$PID_FILE" --log "$LOG_FILE" | rg -q 'status=stopped'

if "$CONTROL" status --pid "$PID_FILE" --log "$LOG_FILE" >/dev/null 2>&1; then
  echo "Expected stopped watchdog to report not running" >&2
  exit 1
fi
