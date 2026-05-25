# YouTube Audio Plays On Test 2 Before Stream Consent

## Exact Error

The user reported: `i played using my youtube and now it is playing musci in test 2`.

Live evidence at `2026-05-25T10:08:57+05:30` showed that DualBT had not yet started the v0.2.35 playback-capture stream. The foreground Android UI was still the MediaProjection consent prompt:

```text
Start recording or casting with DualBT?
Cancel
Start now
```

The latest DualBT file log stopped at:

```text
1779683840290 INFO MainViewModel Capture permission required before streaming to Mini boost 1, Mini boost 2
1779683840303 INFO MainActivity Requesting MediaProjection permission
1779683840351 INFO MainActivity Activity paused
```

Android audio state showed the active A2DP device as the Test 2 speaker:

```text
mBluetoothName=Mini boost 4
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]
Active communication device: AudioDeviceAttributes: role:output type:bt_a2dp addr:41:42:2E:9E:5E:AE
```

## Reproduction Steps

1. Install and launch DualBT v0.2.35.
2. Keep Mini boost 1 and Mini boost 2 selected.
3. Tap `Start Stream`.
4. Do not accept the Android MediaProjection `Start now` prompt yet.
5. Play music from YouTube.
6. User hears YouTube audio on the speaker mapped to Test 2.

## Environment

- Device: `d1bc5c4a`
- App: `com.xpwnit.dualbt`
- Version: `0.2.35` / versionCode `36`
- Android media volume: `9/15`
- Active A2DP address during report: `41:42:2E:9E:5E:AE`
- UI evidence: `/tmp/dualbt-ui-current-1009.xml`

## First Hypothesis

This is not yet evidence that the v0.2.35 splitter is routing only one stream. The stream consent dialog was still pending, so YouTube audio was using Android's normal active A2DP route, currently Mini boost 2. The next test must accept MediaProjection consent, confirm DualBT logs `Audio playback capture started`, and then ask the user which physical speaker(s) play YouTube audio.
