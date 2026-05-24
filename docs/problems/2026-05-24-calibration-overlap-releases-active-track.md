# Calibration Overlap Releases Active Track

- Timestamp: 2026-05-24 23:08 IST
- Step: v0.2.19 physical calibration test after disabling generic SCO fallback.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.19`, versionCode `20`
  - Media volume: `3/15`

## Exact Error

```text
ERROR SpeakerTest Calibration failed for Mini boost 1 | Unable to retrieve AudioTrack pointer for write()
```

Adjacent logs:

```text
MainViewModel Calibration test requested for Mini boost 1 as speaker 1
SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
SpeakerTest Bluetooth output: Mini boost 4@41:42:26:B3:62:1C#type=8
MainViewModel Calibration test requested for Mini boost 2 as speaker 2
SpeakerTest Test 2 blocked for Mini boost 2: Android exposes no direct media route for this speaker. Generic SCO fallback is disabled.
ERROR SpeakerTest Calibration failed for Mini boost 1 | Unable to retrieve AudioTrack pointer for write()
```

## Reproduction Steps

1. Install DualBT v0.2.19.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Press `Test 1`.
4. Quickly press `Test 2` before `Test 1` finishes.

## First Hypothesis

`SpeakerCalibrationPlayer.play()` calls `stop()` before starting every new test. A second tap can release `currentTrack` while the first worker thread is still writing PCM, causing Android to throw from `AudioTrack.write()`. Calibration requests should be ignored while one test is already running.
