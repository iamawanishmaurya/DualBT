# Playing Session With No Audible Song

## Exact Error

User-reported physical result during v0.2.12 test:

```text
no song is being played also first play from speaker one then from speaker 2 so as user i get idea which speaker is 1 and 2 as now the spreaker look identical
```

Device-side evidence at the same test point:

```text
MusicAVMediaSession state=PlaybackState {state=3, position=400, buffered position=0, speed=1.0, ...}
AudioPlaybackConfiguration piid:56495 deviceId:7083 type:android.media.AudioTrack u/pid:10531/965 state:started attr: usage=USAGE_MEDIA
AudioPlaybackConfiguration piid:56503 deviceId:7073 type:android.media.AudioTrack u/pid:10531/965 state:started attr: usage=USAGE_VOICE_COMMUNICATION
AudioPlaybackConfiguration piid:56511 deviceId:7083 type:android.media.AudioTrack u/pid:10077/3451 state:started attr: usage=USAGE_MEDIA
STREAM_MUSIC Muted: false streamVolume:7 Devices: bt_a2dp
```

## Reproduction Steps

1. Install DualBT v0.2.12 on physical device `d1bc5c4a`.
2. Select the two Mini Boost speakers and start streaming.
3. Start `/sdcard/Download/dualbt-test-music.mp3` through MIUI Music.
4. Confirm `dumpsys media_session` reports the MIUI player as playing.
5. Ask the user for physical speaker output.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.12, versionCode 13
- Speakers:
  - `41:42:26:B3:62:1C`
  - `41:42:2E:9E:5E:AE`
- Player: `com.miui.player/.ui.MusicActivity`
- Test media: `/sdcard/Download/dualbt-test-music.mp3`

## First Hypothesis

The OS reports active playback, but the user cannot map or hear the speakers because DualBT lacks a per-speaker calibration sound. Add a test-tone path that plays Mini boost 1 first and Mini boost 2 second using the same routing code, then re-run the physical test with user feedback.
