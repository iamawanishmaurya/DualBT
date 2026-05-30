# Laptop DualBT Speaker Disconnected After Battery Drain

## Exact error

The first Mini Boost speaker is paired but disconnected, so the laptop dual-output sink only has one Bluetooth child output available.

```text
Device 41:42:26:B3:62:1C
Connected: no

Device 41:42:2E:9E:5E:AE
Connected: yes

pactl list short sinks:
679 bluez_output.41_42_2E_9E_5E_AE.1 PipeWire s16le 2ch 48000Hz SUSPENDED
703 dualbt_bluetooth_pair PipeWire float32le 2ch 48000Hz SUSPENDED
```

## Reproduction steps

1. Connect both Mini Boost speakers to the laptop.
2. Let one speaker discharge or power off.
3. Power it back on while the laptop dual-output route still exists.
4. Observe one Bluetooth card/sink is missing and the combined sink only routes to the remaining speaker.

## Environment

- Date: 2026-05-30
- Host: Astra laptop
- Audio stack: PipeWire with PulseAudio compatibility
- Bluetooth devices:
  - `41:42:26:B3:62:1C` (`Mini boost 4`) disconnected
  - `41:42:2E:9E:5E:AE` (`Mini boost 4`) connected

## First hypothesis

When one speaker powers off, BlueZ removes its card/sink from PipeWire. The manual combined sink then becomes stale. The laptop setup needs a recovery script that reconnects missing paired devices, waits for both BlueZ sinks, rebuilds the combined sink, and moves Zen audio back to the combined output.
