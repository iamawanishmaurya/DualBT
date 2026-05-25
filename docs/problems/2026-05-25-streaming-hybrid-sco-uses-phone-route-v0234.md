# Streaming Hybrid SCO Uses Phone Route On v0.2.34

## Exact error
DualBT v0.2.34 started the foreground streaming service, but the output router assigned Mini boost 1 to a `communication-sco` track whose preferred and routed device was the phone, not the physical Mini boost 1 speaker.

Relevant app log:

```text
INFO MainViewModel Capture permission granted; starting streaming to Mini boost 1, Mini boost 2
WARN AudioOutputRouter Using experimental hybrid Bluetooth split: one A2DP media route plus targeted Headset/SCO for Mini boost 1
INFO AudioOutputRouter Headset/SCO route activation for Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
DEBUG AudioOutputRouter Communication device: Redmi Note 9 Pro@00:00:00:00:00:00#type=7
INFO AudioOutputRouter Communication route requested for Mini boost 1, requested=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, accepted=true
WARN AudioOutputRouter Android exposed 1/2 matching Bluetooth output route(s); unmatched tracks will use default routing
INFO AudioOutputRouter Track 0 started for Mini boost 1, mode=communication-sco, preferred=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, preferredAccepted=true, routed=Redmi Note 9 Pro@00:00:00:00:00:00#type=7
INFO AudioOutputRouter Track 1 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:2E:9E:5E:AE#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:2E:9E:5E:AE#type=8
INFO CaptureEngine Audio playback capture started: buffer=30720, routes=Mini boost 1, Mini boost 2
```

Relevant Android audio evidence:

```text
AudioPlaybackConfiguration ... type:android.media.AudioTrack ... usage=USAGE_VOICE_COMMUNICATION ... uid/pid:10531/... state:started
AudioPlaybackConfiguration ... type:android.media.AudioTrack ... usage=USAGE_MEDIA ... uid/pid:10531/... state:started
rec update ... src:REMOTE_SUBMIX not silenced pack:com.xpwnit.dualbt
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]
```

## Reproduction steps
1. Install and launch DualBT v0.2.34.
2. Select Mini boost 1 and Mini boost 2.
3. Tap `Start Stream`.
4. Accept Android's `Start recording or casting with DualBT?` MediaProjection dialog.
5. Inspect DualBT logs.
6. Streaming starts, but the Mini boost 1 track is routed to `Redmi Note 9 Pro` instead of Mini boost 1.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.34` / versionCode `35`
- Selected speaker 1: `Mini boost 1` / address `41:42:26:B3:62:1C`
- Selected speaker 2: `Mini boost 2` / address `41:42:2E:9E:5E:AE`
- Android media volume: `9/15`
- In-app output gain: `100%` before starting; service synced output gain to `60%`

## First hypothesis
The streaming hybrid planner treats any visible Bluetooth SCO output as suitable for the unmatched selected speaker. On this phone, that SCO output is the local phone route (`Redmi Note 9 Pro`), not Mini boost 1. The router should reject generic/unmatched SCO and use the active A2DP handoff experiment instead of sending a selected speaker leg to the phone route.
