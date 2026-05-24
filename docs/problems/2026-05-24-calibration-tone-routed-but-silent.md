# Calibration Tone Routed But Silent

## Exact Error

User feedback after tapping `Test 1` on v0.2.14:

```text
No sound
```

App and system evidence:

```text
INFO SpeakerTest Test 1 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8, bytes=614400
INFO SpeakerTest Test 1 finished for Mini boost 1

AudioPlaybackConfiguration piid:56543 uid/pid:10531/10679 type:android.media.AudioTrack attr: usage=USAGE_MEDIA content=CONTENT_TYPE_MUSIC
player piid:56543 state:started DeviceId:0
player piid:56543 state:device DeviceId:7083
player piid:56543 state:stopped DeviceId:0

STREAM_MUSIC Muted: false
setStreamVolume(stream:STREAM_MUSIC index:12 flags:0x0) from com.android.server.media
```

Related repeated symptom:

- `docs/problems/2026-05-24-playing-session-no-audible-song.md`

## Reproduction Steps

1. Install DualBT v0.2.14 on `d1bc5c4a`.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Set `STREAM_MUSIC` to 12 and `STREAM_VOICE_CALL` to 10 through `cmd media_session volume`.
4. Tap `Test 1`.
5. Ask the user for the physical speaker result.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.14, versionCode 15
- Route: `Mini boost 1`, address `41:42:26:B3:62:1C`
- Android output type: `TYPE_BLUETOOTH_A2DP` (`#type=8`)
- Test tone: stereo PCM16 sine, 48 kHz, 3.2 seconds

## First Hypothesis

Android is accepting the preferred device and reporting a routed `AudioTrack`, but physical output is still silent. Since this repeats the earlier no-audible-song symptom, stop retrying and research multiple Android audio-output fixes before choosing the next implementation.
