# Laptop DualBT Speaker Reconnect Watcher

Problem: [docs/problems/2026-05-30-laptop-dualbt-speaker-disconnected.md](../problems/2026-05-30-laptop-dualbt-speaker-disconnected.md)

## What failed

The manual laptop dual-output route did not automatically recover after one Mini Boost speaker discharged and disconnected. PipeWire kept the old combined sink, but BlueZ removed the disconnected speaker card/sink.

## What worked

Create a watcher script that:

1. Trusts and reconnects both target Bluetooth device addresses.
2. Waits for both BlueZ audio cards/sinks to appear.
3. Forces both devices to the `a2dp-sink-sbc` profile for stable dual output.
4. Rebuilds `dualbt_bluetooth_pair` with low-latency combine settings.
5. Moves Zen audio streams back to the combined sink.
6. Optionally loops forever and repairs the route when a speaker returns.

## Why it worked

BlueZ/PipeWire sink IDs change after Bluetooth reconnects. Rebuilding the combined sink from the current sink names avoids stale child outputs and restores both speakers to the same software output path.

## Commands run

```bash
bluetoothctl devices
bluetoothctl info 41:42:26:B3:62:1C
bluetoothctl info 41:42:2E:9E:5E:AE
pactl list short cards
pactl list short sinks
pactl list short sink-inputs
bash -n scripts/dualbt-laptop-audio-watch.sh
chmod +x scripts/dualbt-laptop-audio-watch.sh
scripts/dualbt-laptop-audio-watch.sh --once
playerctl -p firefox.instance_1_881 play
pactl list short sinks
pactl list short sink-inputs
```
