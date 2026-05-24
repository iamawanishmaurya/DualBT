# Android Exposes One A2DP Output Route For Two Connected Mini Boost Speakers

## Exact Error

Two different physical speakers are paired and connected, and DualBT creates two output `AudioTrack` players, but Android routes both app players to one audio device route.

Bluetooth devices connected:

```text
41:42:26:B3:62:1C Mini boost 4
41:42:2E:9E:5E:AE Mini boost 4
```

DualBT app log:

```text
WARN AudioOutputRouter Android exposed 1/2 matching Bluetooth output route(s); unmatched tracks will use default routing
INFO AudioOutputRouter Track 0 started for Mini boost 4, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
INFO AudioOutputRouter Track 1 started for Mini boost 4, preferred=default, preferredAccepted=false, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
WARN AudioOutputRouter Android routed 2 output track(s) to 1 distinct device route(s); dual-speaker playback may be limited by platform routing
DEBUG AudioOutputRouter PCM writes=1500, bytes=30720, routes=Mini boost 4, Mini boost 4
```

Android audio service:

```text
AudioPlaybackConfiguration piid:56359 deviceId:7083 type:android.media.AudioTrack u/pid:10531/20024 state:started
AudioPlaybackConfiguration piid:56367 deviceId:7083 type:android.media.AudioTrack u/pid:10531/20024 state:started

Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]

APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:2E:9E:5E:AE
```

## Reproduction Steps

1. Pair and connect both physical `Mini boost 4` speakers on the Redmi Note 9 Pro.
2. Install DualBT `0.2.11`.
3. Launch DualBT and select both `Mini boost 4` rows.
4. Tap `Start Stream` and approve Android MediaProjection capture.
5. Inspect `files/logs/dualbt.log` through `run-as`.
6. Inspect `adb shell dumpsys audio`.

## Environment

- Host path: `/home/astra/codex/DualBT`
- Device: Redmi Note 9 Pro (`d1bc5c4a`)
- Android: API 31 / MIUI
- Package: `com.xpwnit.dualbt`
- App version: `0.2.11` / versionCode `12`
- Speakers:
  - `41:42:26:B3:62:1C` (`Mini boost 4`)
  - `41:42:2E:9E:5E:AE` (`Mini boost 4`)

## First Hypothesis

The app-side routing code is now writing to two tracks, but this Redmi/MIUI build exposes only one classic Bluetooth A2DP output sink to third-party app routing at a time. The Android framework can report both Bluetooth devices at the profile level while the audio service still exposes a single active A2DP sink for media output.

## Candidate Fix Directions

1. Use Android combined audio device routing system APIs such as multiple preferred devices for an audio strategy. Trade-off: these are system/privileged APIs, not normal third-party app APIs.
2. Target devices and Android builds whose vendor audio HAL exposes multiple active media devices. Trade-off: depends on OEM support and cannot be forced by DualBT on this Redmi test device.
3. Add LE Audio / broadcast-audio support when hardware supports it. Trade-off: requires Android 13+ capable source hardware and compatible LE Audio speakers; the tested `Mini boost 4` speakers appear as classic A2DP.
4. Add a user-facing diagnostic state that reports "dual connection available, independent output route unavailable" when `dumpsys`/`AudioTrack.getRoutedDevice` evidence collapses to one route. Trade-off: honest UX, but not a full dual-speaker output fix.

## References

- Android `AudioRouting.setPreferredDevice` only specifies a preferred route; `getPreferredDevice` is not guaranteed to be the actual playback device, so DualBT must verify `getRoutedDevice`: https://developer.android.com/reference/android/media/AudioRouting
- Android `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` returns currently connected output sink devices visible to apps: https://developer.android.com/reference/android/media/AudioManager
- AOSP combined audio routing supports multiple devices through system APIs and vendor HAL support: https://source.android.com/docs/core/audio/combined-audio-routing
- Android LE Audio includes broadcast audio use cases for sharing audio to one or more sink devices: https://developer.android.com/develop/connectivity/bluetooth/ble-audio/overview

