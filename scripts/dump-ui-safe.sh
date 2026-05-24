#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 || $# -gt 3 ]]; then
  echo "usage: $0 <device-serial> <local-xml-path> [remote-xml-path]" >&2
  exit 2
fi

SERIAL="$1"
LOCAL_XML="$2"
REMOTE_XML="${3:-/sdcard/$(basename "$LOCAL_XML")}"
LOG_FILE="$(mktemp /tmp/dualbt-uiautomator-dump.XXXXXX.log)"
trap 'rm -f "$LOG_FILE"' EXIT

mkdir -p "$(dirname "$LOCAL_XML")"

DUMP_STATUS=0
adb -s "$SERIAL" shell uiautomator dump "$REMOTE_XML" > "$LOG_FILE" 2>&1 || DUMP_STATUS=$?

PULL_STATUS=0
adb -s "$SERIAL" pull "$REMOTE_XML" "$LOCAL_XML" >> "$LOG_FILE" 2>&1 || PULL_STATUS=$?

if [[ $DUMP_STATUS -ne 0 || $PULL_STATUS -ne 0 ]]; then
  cat "$LOG_FILE" >&2
  exit 1
fi

if [[ ! -s "$LOCAL_XML" ]] || ! grep -q "<hierarchy" "$LOCAL_XML"; then
  cat "$LOG_FILE" >&2
  echo "UI dump did not produce a valid hierarchy XML at $LOCAL_XML" >&2
  exit 1
fi

if grep -q "theme_compatibility.xml" "$LOG_FILE"; then
  echo "warning: device uiautomator emitted MIUI theme_compatibility.xml noise; validated XML at $LOCAL_XML" >&2
fi

printf '%s\n' "$LOCAL_XML"
