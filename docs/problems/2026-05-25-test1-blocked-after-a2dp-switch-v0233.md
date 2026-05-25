# Test 1 Blocked After A2DP Switch On v0.2.33

## Exact error
DualBT v0.2.33 did not play the Test 1 calibration beep. The app accepted the Test 1 request, Android switched the A2DP active device to the Mini boost 1 address, but the app blocked playback because Android did not expose a direct matching audio output route for that speaker.

Relevant app log:

```text
INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
INFO SpeakerTest A2DP route activation for Mini boost 1: A2DP active-device switch unavailable: InvocationTargetException caused by SecurityException: Need BLUETOOTH permission: Neither user 10531 nor current process has android.permission.BLUETOOTH_PRIVILEGED.
DEBUG SpeakerTest Bluetooth output: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:2E:9E:5E:AE#type=8
WARN SpeakerTest No matching A2DP route for Mini boost 1 after delayed route rescans
INFO SpeakerTest Headset/SCO route activation for Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
DEBUG SpeakerTest Communication device: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
WARN SpeakerTest No matching Bluetooth communication route is exposed for Mini boost 1; generic SCO output will not be reused
WARN SpeakerTest Test 1 blocked for Mini boost 1: Android exposes no direct or targeted communication route for this speaker.
INFO SpeakerTest Test 1 finished for Mini boost 1
```

Relevant Android audio evidence:

```text
handleBluetoothA2dpActiveDeviceChangeExt state=2 addr=41:42:26:B3:62:1C prof=2 supprNoisy=true vol=2
onBluetoothA2dpActiveDeviceChange addr=41:42:26:B3:62:1C event=ACTIVE_DEVICE_CHANGE
APM handleDeviceConfigChange success for A2DP device addr=41:42:26:B3:62:1C codec=AUDIO_FORMAT_SBC
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:26:B3:62:1C codec: 1f000000]
APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:26:B3:62:1C
adb -s d1bc5c4a shell cmd media_session volume --get
[V] volume is 0 in range [0..15]
```

## Reproduction steps
1. Install and launch DualBT v0.2.33.
2. Confirm DualBT is foreground and Android media volume is `9/15`.
3. Confirm UI shows `Ready to stream`, `2/2`, in-app gain `10%`, and enabled `Test 1`/`Test 2`.
4. Tap `Test 1`.
5. Capture app logs, Android audio state, and media volume.
6. App blocks Test 1 and media volume reads `0/15`.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.33` / versionCode `34`
- Selected speaker 1: `Mini boost 1` / address `41:42:26:B3:62:1C`
- Selected speaker 2: `Mini boost 2` / address `41:42:2E:9E:5E:AE`
- Android media volume before test: `9/15`
- Android media volume after test: `0/15`
- In-app output gain: `10%`

## First hypothesis
The strict targeted-route guard is too conservative for Android's public A2DP API. After Android successfully switches the active A2DP device to Mini boost 1, the app should allow default media playback on the now-active A2DP route instead of blocking just because `AudioDeviceInfo` does not expose the target address immediately.
