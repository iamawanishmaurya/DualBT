# Calibration Test Button Layout Mis-Tap

## Exact Error

Expected action:

```text
Tap Test 2 to play the second speaker calibration tone.
```

Actual app log:

```text
INFO MainViewModel Capture permission required before streaming to Mini boost 1, Mini boost 2
INFO MainActivity Requesting MediaProjection permission
INFO MainViewModel Capture permission granted; starting streaming to Mini boost 1, Mini boost 2
INFO AudioOutputRouter Track 0 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8
INFO AudioOutputRouter Track 1 started for Mini boost 2, mode=communication-sco, preferred=Redmi Note 9 Pro@00:00:00:00:00:00#type=7
WARN MainViewModel Selection locked while streaming
```

## Reproduction Steps

1. Select two Mini Boost speakers in v0.2.14.
2. Scroll so lower controls are visible.
3. Attempt to tap `Test 2` by coordinate automation after layout changes.
4. Observe that `Start Stream` is triggered instead.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.14
- Input method: `adb shell input tap`

## First Hypothesis

The per-card `Test` buttons move as cards expand and as the list scrolls, making coordinate automation fragile. Move calibration controls to a stable top-level panel near the status card so `Test 1` and `Test 2` have fixed, easy-to-target positions.
