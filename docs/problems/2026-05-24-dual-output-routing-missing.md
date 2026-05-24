# Dual Bluetooth Output Routing Missing In Verified Java Path

## Exact Error
Physical testing with two connected `Mini boost 4` speakers proved DualBT can select two routes and start playback capture, but the verified Java runtime does not write captured PCM to Bluetooth output devices.

Evidence from code search:

```text
app/src/main/java/com/xpwnit/dualbt/audio/AndroidPlaybackCaptureEngine.java:
  splitter.copyToOutputs(sharedBuffer, bytesRead, firstOutput, secondOutput);
  AppLogger.d("CaptureEngine", "Captured PCM chunks=...");

No active Java default-build code references:
  android.media.AudioTrack
  AudioTrack.write(...)
  AudioTrack.setPreferredDevice(...)
```

The only `AudioTrack` router is in the planned Kotlin stack:

```text
app/src/main/java/com/xpwnit/dualbt/audio/AudioRouter.kt
```

That stack is not the verified default APK path in this offline build.

## Reproduction Steps
1. Pair and connect two `Mini boost 4` speakers.
2. Launch DualBT `0.2.10`.
3. Select both `Mini boost 4` rows.
4. Start MediaProjection capture.
5. Observe logcat showing capture chunks for both route names.
6. Inspect the verified Java service/capture code and confirm no output `AudioTrack` writer is active.

## Environment
- Device: Redmi Note 9 Pro (`d1bc5c4a`)
- Android API: 31 / MIUI
- Speakers:
  - `41:42:26:B3:62:1C` (`Mini boost 4`)
  - `41:42:2E:9E:5E:AE` (`Mini boost 4`)
- App: DualBT `0.2.10` debug APK
- Verified build path: Java framework fallback

## First Hypothesis
The default APK implements capture and PCM splitting, but not Bluetooth playback routing. To make the physical speaker test meaningful, the verified Java path needs an `AudioTrack`-based output router that creates one output track per selected route, prefers matching Bluetooth A2DP devices where possible, writes each split PCM buffer, and excludes DualBT's own UID from playback capture to avoid feedback.
