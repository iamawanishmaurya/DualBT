# Test 1 No Sound After SCO Test

- Timestamp: 2026-05-25 00:53 IST
- Step: v0.2.27 A/B physical calibration after targeted `Test 2`.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.27`, versionCode `28`
  - Android media volume: `2/15`

## Exact Error

After `Test 2` used targeted Headset/SCO and produced sound from one unidentified speaker, `Test 1` was tapped for an A/B comparison. User feedback:

```text
No sound
```

Runtime evidence before fixing:

```text
INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
INFO SpeakerTest A2DP route activation for Mini boost 1: A2DP active-device switch unavailable: InvocationTargetException caused by SecurityException: Need BLUETOOTH permission: Neither user 10531 nor current process has android.permission.BLUETOOTH_PRIVILEGED.
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:26:B3:62:1C#type=8
INFO SpeakerTest Test 1 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
```

## Reproduction Steps

1. Install v0.2.27 on physical device `d1bc5c4a`.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Tap `Test 2`; it uses targeted Headset/SCO and produces one audible beep.
4. Tap `Test 1`.
5. Ask the user which speaker played.

## First Hypothesis

The targeted Headset/SCO test leaves Android's Bluetooth audio state favoring the communication route or inactive A2DP state. The following A2DP handoff for `Mini boost 1` fails with a hidden API permission error, so the app writes to an `AudioTrack` whose preferred device is reported but not physically audible.
