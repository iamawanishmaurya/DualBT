# Test 2 Reused Mini Boost 1 A2DP Output

- Timestamp: 2026-05-25 00:46 IST
- Step: v0.2.26 physical calibration test for `Mini boost 2`.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.26`, versionCode `27`
  - Android media volume: `2/15`

## Exact Error

After selecting `Mini boost 1` and `Mini boost 2`, tapping `Test 2` produced this log evidence:

```text
INFO MainViewModel Calibration test requested for Mini boost 2 as speaker 2
INFO SpeakerTest A2DP route activation for Mini boost 2: setActiveDevice(Mini boost 4@41:42:2E:9E:5E:AE) returned true
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:26:B3:62:1C#type=8
WARN SpeakerTest Test 2 using active A2DP route after handoff for Mini boost 2, reported=Mini boost 4@41:42:26:B3:62:1C#type=8
INFO SpeakerTest Test 2 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
```

## Reproduction Steps

1. Install v0.2.26 on physical device `d1bc5c4a`.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Keep Android media volume at `2/15`.
4. Tap `Test 2`.
5. Inspect app file logs.

## First Hypothesis

The calibration path treats a successful reflective A2DP `setActiveDevice` result as enough to reuse the first visible A2DP output, even when `AudioManager` still reports that output with the `Mini boost 1` address. This recreates the same-speaker test failure and prevents the new targeted Headset/SCO path from running.
