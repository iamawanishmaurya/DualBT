# Streaming Still Blocked After A2DP Handoff

- Timestamp: 2026-05-25 00:01 IST
- Step: Physical Mini Boost verification after v0.2.22 calibration handoff.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.22`, versionCode `23`
  - Selected speakers: `Mini boost 1` and `Mini boost 2`

## Exact Error

Physical calibration improved, but streaming is still not able to prove simultaneous output:

```text
Test 2 user feedback: Mini boost 2
Repeated Test 1 user feedback: Mini boost 1
```

The app-side streaming path still blocks when Android exposes only one direct public Bluetooth media route:

```text
Dual Bluetooth output blocked: Android exposes 1/2 direct media route(s). Generic SCO fallback is disabled because it can route both tracks to the same speaker.
```

## Reproduction Steps

1. Install and open DualBT v0.2.22.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Use `Test 2`; the active A2DP handoff can switch output to `Mini boost 2`.
4. Use `Test 1`; the active A2DP handoff can switch output back to `Mini boost 1`.
5. Try the streaming path with the same selected speakers.

## First Hypothesis

`BluetoothA2dp.setActiveDevice()` can switch the one active classic A2DP media endpoint, but DualBT's stream router still requires two simultaneous public `AudioDeviceInfo.TYPE_BLUETOOTH_A2DP` routes. The next experiment should create each streaming `AudioTrack` after activating its target speaker and then ask the user whether both physical speakers play at the same time. If Android reroutes both tracks to the latest active A2DP endpoint, the app must surface the device limitation instead of claiming success.
