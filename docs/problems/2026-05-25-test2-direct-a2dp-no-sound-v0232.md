# Test 2 Direct A2DP Route Produces No Sound On v0.2.32

## Exact error
Physical verification failed: after tapping `Test 2` on v0.2.32, the user reported `No sound`.

Relevant app log:

```text
INFO MainViewModel Calibration test requested for Mini boost 2 as speaker 2
INFO SpeakerTest A2DP route activation for Mini boost 2: A2DP active-device switch unavailable: InvocationTargetException caused by SecurityException: Need BLUETOOTH permission: Neither user 10531 nor current process has android.permission.BLUETOOTH_PRIVILEGED.
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:2E:9E:5E:AE#type=8
INFO SpeakerTest Calibration stream unmuted: stream=3
DEBUG SpeakerTest Calibration volume already audible: stream=3, current=2, target=2/15
INFO SpeakerTest Test 2 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:2E:9E:5E:AE#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8, bytes=614400, targetFrames=153600, drainTimeoutMs=4700
INFO SpeakerTest Calibration playback drained: playedFrames=153600/153600, waitedMs=152
INFO SpeakerTest Test 2 finished for Mini boost 2
```

Relevant Android audio evidence:

```text
05-25 09:26:35:413 handleBluetoothA2dpActiveDeviceChangeExt state=2 addr=41:42:2E:9E:5E:AE prof=2 supprNoisy=true vol=2
05-25 09:26:35:480 onBluetoothA2dpActiveDeviceChange addr=41:42:2E:9E:5E:AE event=ACTIVE_DEVICE_CHANGE
05-25 09:26:35:487 APM handleDeviceConfigChange success for A2DP device addr=41:42:2E:9E:5E:AE codec=AUDIO_FORMAT_SBC
05-25 09:26:35:521 new player piid:57455 uid/pid:10531/29369 type:android.media.AudioTrack attr:AudioAttributes: usage=USAGE_MEDIA content=CONTENT_TYPE_MUSIC
05-25 09:26:35:797 player piid:57455 state:device DeviceId:7449
05-25 09:26:39:575 player piid:57455 state:stopped DeviceId:0
```

## Reproduction steps
1. Install and launch DualBT v0.2.32 on Android device `d1bc5c4a`.
2. Confirm the app shows `Ready to stream`, `2/2`, and enabled Test 1/Test 2.
3. Set Android media volume to `2/15`; lower in-app gain to `10%`.
4. Tap `Test 2`.
5. Wait for the calibration route logs and ask the user which speaker produced the beep.
6. User reports `No sound`.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.32` / versionCode `33`
- Selected speaker 2: `Mini boost 2` / address `41:42:2E:9E:5E:AE`
- Android media volume: `2/15`
- In-app output gain: `10%`

## First hypothesis
Because Test 2 has a direct preferred A2DP output, Android reports the A2DP active-device change as successful, and the `AudioTrack` stays alive for the tone duration, the remaining likely failure is audibility rather than routing: absolute Bluetooth volume index `2/15` may be too low for the Mini Boost speakers, or the app needs to request audio focus before playing its calibration tone.

## Additional evidence
After the user played audio through the local YouTube app, they reported that music played in the Test 2 route. Live `dumpsys audio` showed:

```text
source: ... pack: com.google.android.youtube ... gain: GAIN ... attr: AudioAttributes: usage=USAGE_MEDIA content=CONTENT_TYPE_MUSIC
STREAM_MUSIC bt_a2dp volume: 9/15
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]
APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:2E:9E:5E:AE
Active communication device: AudioDeviceAttributes: role:output type:bt_a2dp addr:41:42:2E:9E:5E:AE
```

This supports the first hypothesis: the selected Test 2 A2DP endpoint can play media, but the app test tone did not have the same effective media-focus/volume conditions as YouTube.

The user confirmed the physical speaker playing YouTube audio on this route was `Mini boost 2`.
