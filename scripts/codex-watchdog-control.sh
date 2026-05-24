#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WATCHDOG="$ROOT_DIR/scripts/codex-watchdog.sh"
DEFAULT_LOG="$ROOT_DIR/docs/codex-watchdog.log"
DEFAULT_PID="$ROOT_DIR/docs/codex-watchdog.pid"

usage() {
  cat <<'EOF'
Usage: scripts/codex-watchdog-control.sh <command> [options]

Commands:
  start       Start the detached one-minute watchdog.
  status      Print whether the watchdog is running and show the last heartbeat.
  stop        Stop the detached watchdog.
  tail        Follow the heartbeat log.

Options:
  --interval SECONDS       Poll interval for start. Defaults to 60.
  --log PATH               Heartbeat log path. Defaults to docs/codex-watchdog.log.
  --pid PATH               PID file path. Defaults to docs/codex-watchdog.pid.
  -h, --help               Show this help.
EOF
}

if [[ $# -lt 1 ]]; then
  usage >&2
  exit 2
fi

COMMAND="$1"
shift
INTERVAL_SECONDS=60
LOG_FILE="$DEFAULT_LOG"
PID_FILE="$DEFAULT_PID"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --interval)
      INTERVAL_SECONDS="$2"
      shift 2
      ;;
    --log)
      LOG_FILE="$2"
      shift 2
      ;;
    --pid)
      PID_FILE="$2"
      shift 2
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

is_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" >/dev/null 2>&1
}

read_pid() {
  if [[ -f "$PID_FILE" ]]; then
    tr -d '[:space:]' < "$PID_FILE"
  fi
}

last_heartbeat() {
  if [[ -s "$LOG_FILE" ]]; then
    tail -n 1 "$LOG_FILE"
  else
    printf 'none'
  fi
}

case "$COMMAND" in
  start)
    mkdir -p "$(dirname "$LOG_FILE")" "$(dirname "$PID_FILE")"
    pid="$(read_pid)"
    if is_running "$pid"; then
      echo "status=running pid=$pid log=$LOG_FILE"
      exit 0
    fi
    rm -f "$PID_FILE"
    nohup "$WATCHDOG" --interval "$INTERVAL_SECONDS" --log "$LOG_FILE" >/dev/null 2>&1 &
    pid="$!"
    printf '%s\n' "$pid" > "$PID_FILE"
    echo "status=started pid=$pid interval=$INTERVAL_SECONDS log=$LOG_FILE"
    ;;
  status)
    pid="$(read_pid)"
    if is_running "$pid"; then
      echo "status=running pid=$pid log=$LOG_FILE last=\"$(last_heartbeat)\""
      exit 0
    fi
    rm -f "$PID_FILE"
    echo "status=stopped log=$LOG_FILE last=\"$(last_heartbeat)\""
    exit 1
    ;;
  stop)
    pid="$(read_pid)"
    if is_running "$pid"; then
      kill "$pid"
      for _ in 1 2 3 4 5; do
        if ! is_running "$pid"; then
          break
        fi
        sleep 1
      done
      if is_running "$pid"; then
        kill -9 "$pid" >/dev/null 2>&1 || true
      fi
    fi
    rm -f "$PID_FILE"
    echo "status=stopped log=$LOG_FILE last=\"$(last_heartbeat)\""
    ;;
  tail)
    mkdir -p "$(dirname "$LOG_FILE")"
    touch "$LOG_FILE"
    tail -f "$LOG_FILE"
    ;;
  -h|--help)
    usage
    ;;
  *)
    echo "Unknown command: $COMMAND" >&2
    usage >&2
    exit 2
    ;;
esac
