# Test 2 Still Silent At 2/15 After Audio Focus On v0.2.33

## Exact error
Physical verification failed: after tapping `Test 2` on v0.2.33 with Android media volume at `2/15`, the user reported that no beep was produced.

Relevant app log:

```text
INFO SpeakerTest Calibration audio focus granted
DEBUG SpeakerTest Calibration volume already audible: stream=3, current=2, target=2/15
INFO SpeakerTest Test 2 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:2E:9E:5E:AE#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8, focusGranted=true, bytes=614400, targetFrames=153600, drainTimeoutMs=4700
INFO SpeakerTest Calibration playback drained: playedFrames=153600/153600, waitedMs=153
INFO SpeakerTest Calibration audio focus abandoned
INFO SpeakerTest Test 2 finished for Mini boost 2
```

## Reproduction steps
1. Install and launch DualBT v0.2.33.
2. Pause/stop YouTube to isolate the app.
3. Set Android media volume to `2/15`.
4. Set in-app gain to `10%`.
5. Tap `Test 2`.
6. Ask the user which physical speaker produced the beep.
7. User reports no beep, then reports that a song plays when they start music again.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.33` / versionCode `34`
- Selected speaker 2: `Mini boost 2` / address `41:42:2E:9E:5E:AE`
- Android media volume: `2/15`
- In-app output gain: `10%`

## First hypothesis
The route and focus path are working, but `2/15` is below the practical audibility threshold for the Mini Boost speaker in this room/device state. The next diagnostic is to follow the user's request and test at Android media volume `9/15`, where YouTube music was audible.
