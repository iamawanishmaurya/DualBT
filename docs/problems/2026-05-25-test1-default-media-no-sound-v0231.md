# Test 1 Default Media Route Produces No Sound On v0.2.31

## Exact error
Physical verification failed: after tapping `Test 1`, the user reported `No sound`.

Relevant app log:

```text
INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
INFO SpeakerTest A2DP route activation for Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:2E:9E:5E:AE#type=8
WARN SpeakerTest No matching A2DP route for Mini boost 1 after delayed route rescans
DEBUG SpeakerTest Calibration volume already audible: stream=3, current=2, target=2/15
INFO SpeakerTest Test 1 started for Mini boost 1, mode=media-default-after-a2dp-activation, preferred=default, preferredAccepted=false, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8, bytes=614400
INFO SpeakerTest Test 1 finished for Mini boost 1
```

## Reproduction steps
1. Install and launch DualBT v0.2.31 on Android device `d1bc5c4a`.
2. Select `Mini boost 1` and `Mini boost 2`; app shows `Ready to stream` and `2/2`.
3. Set Android media volume to `2/15`; lower in-app gain to `10%`.
4. Tap `Test 1`.
5. Ask the user which physical speaker produced the beep.
6. User reports `No sound`.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.31` / versionCode `32`
- Selected speaker 1: `Mini boost 1` / address `41:42:26:B3:62:1C`
- Visible stale A2DP output: `Mini boost 4` / address `41:42:2E:9E:5E:AE`
- Android media volume: `2/15`
- In-app output gain: `10%`

## First hypothesis
The hidden A2DP active-device call can return `true` before Android has actually routed app media to that target, or it can accept the request while the media route remains stale/unusable for a normal app-created `AudioTrack`. The default-media fallback is therefore not enough; the fix needs a stronger route-confirmation strategy before playback or a different user-assisted handoff path.
