#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INTERVAL_SECONDS=60
COUNT=0
LOG_FILE="$ROOT_DIR/docs/codex-watchdog.log"
STATUS_COMMAND="env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=$ROOT_DIR git status --short --branch >/dev/null"

usage() {
  cat <<'EOF'
Usage: scripts/codex-watchdog.sh [options]

Polls once per minute by default and appends heartbeat lines to a log file.

Options:
  --interval SECONDS       Poll interval. Defaults to 60.
  --count N                Number of polls before exit. Defaults to infinite.
  --log PATH               Heartbeat log path. Defaults to docs/codex-watchdog.log.
  --status-command CMD     Shell command used as the health check.
  --once                   Record one heartbeat and exit.
  -h, --help               Show this help.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --interval)
      INTERVAL_SECONDS="$2"
      shift 2
      ;;
    --count)
      COUNT="$2"
      shift 2
      ;;
    --log)
      LOG_FILE="$2"
      shift 2
      ;;
    --status-command)
      STATUS_COMMAND="$2"
      shift 2
      ;;
    --once)
      COUNT=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if ! [[ "$INTERVAL_SECONDS" =~ ^[1-9][0-9]*$ ]]; then
  echo "--interval must be a positive integer" >&2
  exit 2
fi

if ! [[ "$COUNT" =~ ^[0-9]+$ ]]; then
  echo "--count must be a non-negative integer" >&2
  exit 2
fi

mkdir -p "$(dirname "$LOG_FILE")"

poll_number=0
while true; do
  poll_number=$((poll_number + 1))
  timestamp="$(date -Iseconds)"
  status="ok"
  exit_code=0

  set +e
  output="$(bash -c "$STATUS_COMMAND" 2>&1)"
  exit_code=$?
  set -e

  if [[ "$exit_code" -ne 0 ]]; then
    status="fail"
  fi

  output="${output//$'\n'/ }"
  line="$timestamp codex_watchdog heartbeat poll=$poll_number pid=$$ cwd=$ROOT_DIR status=$status exit=$exit_code command=\"$STATUS_COMMAND\" output=\"$output\""
  printf '%s\n' "$line" | tee -a "$LOG_FILE"

  if [[ "$COUNT" != "0" && "$poll_number" -ge "$COUNT" ]]; then
    exit "$exit_code"
  fi

  sleep "$INTERVAL_SECONDS"
done
