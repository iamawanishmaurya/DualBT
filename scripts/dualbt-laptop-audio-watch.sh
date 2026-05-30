#!/usr/bin/env bash
set -u
set -o pipefail

SPEAKER_1="${DUALBT_SPEAKER_1:-41:42:26:B3:62:1C}"
SPEAKER_2="${DUALBT_SPEAKER_2:-41:42:2E:9E:5E:AE}"
SINK_NAME="${DUALBT_SINK_NAME:-dualbt_bluetooth_pair}"
SINK_DESCRIPTION="${DUALBT_SINK_DESCRIPTION:-DualBT_Bluetooth_Pair_LowLatency_SBC}"
APP_NAME="${DUALBT_APP_NAME:-Zen}"
PID_FILE="${DUALBT_WATCH_PID_FILE:-/tmp/dualbt-laptop-audio-watch.pid}"
LOG_FILE="${DUALBT_WATCH_LOG_FILE:-/tmp/dualbt-laptop-audio-watch.log}"
BT_PROFILE="${DUALBT_BT_PROFILE:-a2dp-sink-sbc}"
FALLBACK_BT_PROFILE="${DUALBT_FALLBACK_BT_PROFILE:-a2dp-sink}"
BT_VOLUME="${DUALBT_BT_VOLUME:-95%}"
APP_VOLUME="${DUALBT_APP_VOLUME:-90%}"
LATENCY_MSEC="${DUALBT_LATENCY_MSEC:-40}"
ADJUST_TIME="${DUALBT_ADJUST_TIME:-1}"
WATCH_INTERVAL="${DUALBT_WATCH_INTERVAL:-10}"
CONNECT_SETTLE_SECONDS="${DUALBT_CONNECT_SETTLE_SECONDS:-4}"
MAX_SINK_WAIT_SECONDS="${DUALBT_MAX_SINK_WAIT_SECONDS:-20}"

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

usage() {
  cat <<EOF
Usage: $0 [--once|--watch|--status|--stop]

Options:
  --once    Reconnect both speakers once, rebuild the combined sink, and move ${APP_NAME}.
  --watch   Keep repairing the route every ${WATCH_INTERVAL}s when a speaker disconnects/reappears.
  --daemon  Start --watch in the background and verify the PID file.
  --status  Print Bluetooth and PipeWire state.
  --stop    Stop the watcher process by PID file and unload the DualBT combined sink.

Environment overrides:
  DUALBT_SPEAKER_1=${SPEAKER_1}
  DUALBT_SPEAKER_2=${SPEAKER_2}
  DUALBT_APP_NAME=${APP_NAME}
  DUALBT_BT_VOLUME=${BT_VOLUME}
  DUALBT_APP_VOLUME=${APP_VOLUME}
  DUALBT_LATENCY_MSEC=${LATENCY_MSEC}
  DUALBT_WATCH_INTERVAL=${WATCH_INTERVAL}
  DUALBT_WATCH_PID_FILE=${PID_FILE}
  DUALBT_WATCH_LOG_FILE=${LOG_FILE}
EOF
}

require_commands() {
  local missing=0
  for command in bluetoothctl pactl awk sed date; do
    if ! command -v "$command" >/dev/null 2>&1; then
      log "Missing required command: $command"
      missing=1
    fi
  done
  return "$missing"
}

addr_token() {
  printf '%s' "$1" | tr ':' '_'
}

card_name() {
  printf 'bluez_card.%s' "$(addr_token "$1")"
}

sink_name_for_addr() {
  printf 'bluez_output.%s.1' "$(addr_token "$1")"
}

is_connected() {
  bluetoothctl info "$1" 2>/dev/null | awk -F': ' '/Connected:/ {print $2}' | grep -qx 'yes'
}

trust_and_connect() {
  local address="$1"
  bluetoothctl trust "$address" >/dev/null 2>&1 || true
  if is_connected "$address"; then
    log "$address already connected"
    return 0
  fi
  log "Connecting $address"
  if bluetoothctl connect "$address"; then
    sleep "$CONNECT_SETTLE_SECONDS"
    is_connected "$address"
    return $?
  fi
  return 1
}

set_card_profile() {
  local address="$1"
  local card
  card="$(card_name "$address")"
  if ! pactl list short cards | awk '{print $2}' | grep -qx "$card"; then
    log "Card missing for $address: $card"
    return 1
  fi
  if pactl set-card-profile "$card" "$BT_PROFILE" >/dev/null 2>&1; then
    log "$card profile set to $BT_PROFILE"
    return 0
  fi
  if pactl set-card-profile "$card" "$FALLBACK_BT_PROFILE" >/dev/null 2>&1; then
    log "$card profile set to fallback $FALLBACK_BT_PROFILE"
    return 0
  fi
  log "Could not set A2DP profile for $card"
  return 1
}

sink_exists() {
  local sink="$1"
  pactl list short sinks | awk '{print $2}' | grep -qx "$sink"
}

wait_for_sinks() {
  local sink1="$1"
  local sink2="$2"
  local elapsed=0
  while [ "$elapsed" -lt "$MAX_SINK_WAIT_SECONDS" ]; do
    if sink_exists "$sink1" && sink_exists "$sink2"; then
      return 0
    fi
    sleep 1
    elapsed=$((elapsed + 1))
  done
  log "Timed out waiting for both sinks: $sink1, $sink2"
  return 1
}

unload_combine_sink() {
  pactl list short modules \
    | awk -v name="sink_name=${SINK_NAME}" '$2 == "module-combine-sink" && index($0, name) {print $1}' \
    | while read -r module_id; do
        if [ -n "$module_id" ]; then
          log "Unloading stale combine sink module $module_id"
          pactl unload-module "$module_id" >/dev/null 2>&1 || true
        fi
      done
}

load_combine_sink() {
  local sink1="$1"
  local sink2="$2"
  local module_id
  module_id="$(pactl load-module module-combine-sink \
    sink_name="$SINK_NAME" \
    slaves="$sink1,$sink2" \
    latency_msec="$LATENCY_MSEC" \
    adjust_time="$ADJUST_TIME" \
    sink_properties="device.description=${SINK_DESCRIPTION}")"
  log "Loaded ${SINK_NAME} module ${module_id} with slaves ${sink1}, ${sink2}"
}

move_app_streams() {
  local input_ids
  input_ids="$(pactl list sink-inputs | awk -v app="$APP_NAME" '
    /^Sink Input #/ { id=$3; sub("#", "", id) }
    $0 ~ "application.name = \"" app "\"" { print id }
  ')"
  if [ -z "$input_ids" ]; then
    log "No active ${APP_NAME} sink input to move"
    return 0
  fi
  for input_id in $input_ids; do
    pactl move-sink-input "$input_id" "$SINK_NAME" >/dev/null 2>&1 || true
    pactl set-sink-input-volume "$input_id" "$APP_VOLUME" >/dev/null 2>&1 || true
    log "Moved ${APP_NAME} sink input ${input_id} to ${SINK_NAME}"
  done
}

setup_once() {
  require_commands || return 1

  local sink1 sink2
  sink1="$(sink_name_for_addr "$SPEAKER_1")"
  sink2="$(sink_name_for_addr "$SPEAKER_2")"

  trust_and_connect "$SPEAKER_1" || log "Connect attempt failed for $SPEAKER_1"
  trust_and_connect "$SPEAKER_2" || log "Connect attempt failed for $SPEAKER_2"

  set_card_profile "$SPEAKER_1" || true
  set_card_profile "$SPEAKER_2" || true

  if ! wait_for_sinks "$sink1" "$sink2"; then
    status
    return 1
  fi

  pactl set-sink-mute "$sink1" 0 >/dev/null 2>&1 || true
  pactl set-sink-mute "$sink2" 0 >/dev/null 2>&1 || true
  pactl set-sink-volume "$sink1" "$BT_VOLUME" >/dev/null 2>&1 || true
  pactl set-sink-volume "$sink2" "$BT_VOLUME" >/dev/null 2>&1 || true

  unload_combine_sink
  load_combine_sink "$sink1" "$sink2"
  pactl set-default-sink "$SINK_NAME" >/dev/null 2>&1 || true
  move_app_streams
  status
}

status() {
  log "Bluetooth status"
  bluetoothctl info "$SPEAKER_1" 2>/dev/null | sed -n '/Device /p;/Name:/p;/Connected:/p;/Battery Percentage:/p'
  bluetoothctl info "$SPEAKER_2" 2>/dev/null | sed -n '/Device /p;/Name:/p;/Connected:/p;/Battery Percentage:/p'
  log "PipeWire/PulseAudio status"
  pactl list short cards
  pactl list short sinks
  pactl list short sink-inputs
  pactl get-default-sink 2>/dev/null || true
}

watch_loop() {
  require_commands || exit 1
  while true; do
    if ! is_connected "$SPEAKER_1" || ! is_connected "$SPEAKER_2" || ! sink_exists "$SINK_NAME"; then
      setup_once || true
    else
      move_app_streams
    fi
    sleep "$WATCH_INTERVAL"
  done
}

script_path() {
  local source_dir
  source_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  printf '%s/%s' "$source_dir" "$(basename "${BASH_SOURCE[0]}")"
}

pid_is_running() {
  [ -f "$PID_FILE" ] || return 1
  local pid
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  [ -n "$pid" ] || return 1
  ps -p "$pid" >/dev/null 2>&1
}

start_daemon() {
  require_commands || exit 1
  if pid_is_running; then
    log "Watcher already running with PID $(cat "$PID_FILE")"
    return 0
  fi
  rm -f "$PID_FILE"
  log "Starting watcher daemon; log=$LOG_FILE"
  nohup "$(script_path)" --watch >>"$LOG_FILE" 2>&1 &
  local pid="$!"
  printf '%s\n' "$pid" >"$PID_FILE"
  sleep 1
  if pid_is_running; then
    log "Watcher daemon started with PID $pid"
    return 0
  fi
  log "Watcher daemon failed to stay running"
  tail -n 40 "$LOG_FILE" 2>/dev/null || true
  return 1
}

stop_daemon() {
  if pid_is_running; then
    local pid
    pid="$(cat "$PID_FILE")"
    log "Stopping watcher daemon PID $pid"
    kill "$pid" >/dev/null 2>&1 || true
    sleep 1
  fi
  rm -f "$PID_FILE"
  unload_combine_sink
}

case "${1:---once}" in
  --once)
    setup_once
    ;;
  --watch)
    watch_loop
    ;;
  --daemon)
    start_daemon
    ;;
  --status)
    require_commands && status
    ;;
  --stop)
    stop_daemon
    ;;
  -h|--help)
    usage
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac
