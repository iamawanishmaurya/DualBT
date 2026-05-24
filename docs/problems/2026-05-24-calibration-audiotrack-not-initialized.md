# Calibration AudioTrack Not Initialized

## Exact Error

DualBT v0.2.13 app log after tapping `Test 1`:

```text
INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:26:B3:62:1C#type=8
WARN SpeakerTest Calibration AudioTrack was not initialized for Mini boost 1
INFO SpeakerTest Test 1 finished for Mini boost 1
```

## Reproduction Steps

1. Install DualBT v0.2.13.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Tap `Test 1`.
4. Pull `files/logs/dualbt.log` with `run-as`.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.13, versionCode 14
- Route: `Mini boost 1`, address `41:42:26:B3:62:1C`
- Audio path: `SpeakerCalibrationPlayer` using `AudioTrack.MODE_STATIC`

## First Hypothesis

The static `AudioTrack` allocation is failing for the generated calibration buffer on this device/audio route. Switch calibration playback to `MODE_STREAM` with a min-buffer-based track and chunked writes.
