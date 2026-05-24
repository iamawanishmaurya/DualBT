# YouTube Capture Volume Control Missing

## Exact Error

User feedback:

```text
so i was able to play music using my local youtube appp but was not able to control audio volume fix it and i think issue is with music use youtube and play any music for testing
```

Current app behavior:

- DualBT captures source audio with `AudioRecord` playback capture.
- DualBT writes captured PCM to its own `AudioTrack` outputs.
- One fallback route can use `USAGE_VOICE_COMMUNICATION`, while the media route uses `USAGE_MEDIA`.
- No app-level gain exists between capture and output, so volume control depends on Android stream routing and can be inconsistent across media and communication fallback routes.

## Reproduction Steps

1. Connect the two Mini Boost speakers to physical device `d1bc5c4a`.
2. Start DualBT capture/streaming.
3. Play music from the local YouTube app.
4. Attempt to control output volume.
5. Observe that the user cannot reliably control the audible DualBT output volume.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App under test: `com.xpwnit.dualbt` v0.2.14
- Source app: local YouTube app (`com.google.android.youtube` available)
- Output modes: A2DP media plus experimental communication-SCO fallback

## First Hypothesis

Because DualBT re-emits captured PCM through its own `AudioTrack` instances, app-level output gain is needed. Add tested PCM gain scaling plus visible DualBT volume controls that update the running foreground service.

## Additional Evidence - 2026-05-24 22:37 IST

- Tapping DualBT `Volume -` lowered the service gain from 100% to 40%, proving the in-service PCM gain path works.
- After bringing DualBT to the foreground, YouTube reported `PlaybackState` state `1`, so app-screen buttons are insufficient for a YouTube-foreground workflow.
- The next fix should let DualBT follow Android media-volume changes while the source app remains in front.
