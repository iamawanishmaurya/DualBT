# v0.2.35 Dual Tracks Route To One A2DP Device

## Exact Error

The user reported that after playing music from YouTube, audio was playing on the speaker mapped to Test 2 instead of both speakers:

```text
i played using my youtube and now it is playing musci in test 2
```

After accepting the Android MediaProjection prompt and starting DualBT v0.2.35, the app started two media output tracks, but Android routed them to one distinct output device:

```text
1779683996235 WARN AudioOutputRouter Using experimental active A2DP handoff because Android exposes only 1/2 direct media route(s)
1779683996236 WARN AudioOutputRouter Android exposed 1/2 matching Bluetooth output route(s); unmatched tracks will use default routing
1779683996537 INFO AudioOutputRouter Track 0 started for Mini boost 1, mode=media, preferred=default, preferredAccepted=false, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8
1779683997452 INFO AudioOutputRouter Track 1 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:2E:9E:5E:AE#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8
1779683997453 WARN AudioOutputRouter Android routed 2 output track(s) to 1 distinct device route(s); dual-speaker playback may be limited by platform routing
1779683997601 INFO CaptureEngine Audio playback capture started: buffer=30720, routes=Mini boost 1, Mini boost 2
```

Android audio state also showed only one connected public A2DP sink:

```text
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]
APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:2E:9E:5E:AE
```

## Reproduction Steps

1. Install DualBT v0.2.35.
2. Select Mini boost 1 and Mini boost 2.
3. Set Android media volume to `9/15`.
4. Tap `Start Stream`.
5. Accept the Android MediaProjection `Start now` prompt.
6. Play music in YouTube.
7. Observe that DualBT starts two tracks, but Android reports one distinct A2DP route and the user hears audio on the Test 2 speaker path.

## Environment

- Device: `d1bc5c4a`
- App: `com.xpwnit.dualbt`
- Version: `0.2.35` / versionCode `36`
- Android media volume: `9/15`
- Selected speaker addresses:
  - Mini boost 1: `41:42:26:B3:62:1C`
  - Mini boost 2: `41:42:2E:9E:5E:AE`
- Evidence files:
  - `/tmp/dualbt-ui-v0235-stream-started-1010.xml`

## First Hypothesis

The app can capture and split PCM, but Android's public audio routing on this MIUI device exposes only one active A2DP sink at a time. Creating two `AudioTrack`s and setting preferred devices cannot force simultaneous output when the platform audio policy only publishes one A2DP output route. Since this is the repeated one-speaker failure, further fixes require researching Android-supported multi-device Bluetooth output approaches before another implementation attempt.
