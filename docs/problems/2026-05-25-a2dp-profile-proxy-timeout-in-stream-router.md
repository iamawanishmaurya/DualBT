# A2DP Profile Proxy Timeout In Stream Router

- Timestamp: 2026-05-25 00:09 IST
- Step: v0.2.23 physical streaming test on Redmi Note 9 Pro.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.23`, versionCode `24`
  - Selected speakers: `Mini boost 1` and `Mini boost 2`

## Exact Error

After accepting MediaProjection, the experimental streaming output router hit the same A2DP proxy timeout twice and stopped the foreground service:

```text
AudioOutputRouter Active A2DP handoff for track 0 Mini boost 1: A2DP profile proxy timed out
AudioOutputRouter Active A2DP handoff for track 1 Mini boost 2: A2DP profile proxy timed out
AudioOutputRouter Active A2DP handoff could not prepare output for Mini boost 2
CaptureEngine Output router could not start
DualBTService Foreground service stopping because audio capture could not start
```

## Reproduction Steps

1. Install and launch DualBT v0.2.23.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Press `Start Stream`.
4. Accept the Android MediaProjection prompt.
5. Inspect `files/logs/dualbt.log`.

## First Hypothesis

The streaming start path calls the synchronous A2DP activator from `DualBTService.onStartCommand()`, which runs on the app main thread. Android's `BluetoothAdapter.getProfileProxy()` documentation says the service listener is invoked on the application's main looper. Blocking the main thread while waiting for that callback can deadlock the callback delivery and produce repeat proxy timeouts.
