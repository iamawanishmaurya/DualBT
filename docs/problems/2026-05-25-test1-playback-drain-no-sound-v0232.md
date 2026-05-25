# Test 1 Still Produces No Sound After Playback Drain Fix On v0.2.32

## Exact error
Physical verification failed: after tapping `Test 1` on v0.2.32, the user reported `No sound`.

Relevant app log:

```text
INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
INFO SpeakerTest A2DP route activation for Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
WARN SpeakerTest No matching A2DP route for Mini boost 1 after delayed route rescans
DEBUG SpeakerTest Calibration volume already audible: stream=3, current=2, target=2/15
INFO SpeakerTest Test 1 started for Mini boost 1, mode=media-default-after-a2dp-activation, preferred=default, preferredAccepted=false, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8, bytes=614400, targetFrames=153600, drainTimeoutMs=4700
INFO SpeakerTest Calibration playback drained: playedFrames=153600/153600, waitedMs=151
INFO SpeakerTest Test 1 finished for Mini boost 1
```

Relevant Android audio evidence:

```text
05-25 09:24:38:179 new player piid:57447 uid/pid:10531/29369 type:android.media.AudioTrack attr:AudioAttributes: usage=USAGE_MEDIA content=CONTENT_TYPE_MUSIC
05-25 09:24:38:184 player piid:57447 state:started DeviceId:0
05-25 09:24:38:267 player piid:57447 state:device DeviceId:7449
05-25 09:24:41:593 player piid:57447 state:stopped DeviceId:0
```

## Reproduction steps
1. Install and launch DualBT v0.2.32 on Android device `d1bc5c4a`.
2. Confirm the app shows `Ready to stream`, `2/2`, and enabled Test 1/Test 2.
3. Set Android media volume to `2/15`; lower in-app gain to `10%`.
4. Tap `Test 1`.
5. Wait for the calibration route logs and ask the user which speaker produced the beep.
6. User reports `No sound`.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.32` / versionCode `33`
- Selected speaker 1: `Mini boost 1` / address `41:42:26:B3:62:1C`
- Visible stale A2DP output during Test 1: `Mini boost 4` / address `41:42:2E:9E:5E:AE`
- Android media volume: `2/15`
- In-app output gain: `10%`

## First hypothesis
The playback-drain fix proved the `AudioTrack` stayed alive and was routed by Android, so the remaining failure is likely route identity, not early release: Android is still routing the default media track to the visible/stale A2DP device rather than the requested `Mini boost 1`, or the selected speaker's A2DP endpoint is connected but not actually accepting media from this app route.
