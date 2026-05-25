# Mini Boost 2 SCO Test Silent

- Timestamp: 2026-05-25 00:36 IST
- Step: Physical speaker verification after v0.2.25 hybrid Bluetooth split.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.25`, versionCode `26`
  - Android media volume: `2/15`

## Exact Error

User feedback:

```text
only seaker mini boost 1 when seleted in test and click on test producce sound and the mini boost 2 does not produce any sound
```

Runtime evidence before fixing:

```text
INFO AudioOutputRouter Track 0 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
INFO AudioOutputRouter Track 1 started for Mini boost 2, mode=communication-sco, preferred=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, preferredAccepted=true, routed=Redmi Note 9 Pro@00:00:00:00:00:00#type=7
INFO CaptureEngine Audio playback capture started: buffer=30720, routes=Mini boost 1, Mini boost 2
```

## Reproduction Steps

1. Install v0.2.25 on physical device `d1bc5c4a`.
2. Set Android media volume to `2/15`.
3. Select `Mini boost 1` and `Mini boost 2`.
4. Press speaker test for `Mini boost 1`; user hears sound.
5. Press speaker test for `Mini boost 2`; user reports no sound.
6. Start dual stream; logs show two app tracks, but physical output still requires user confirmation.

## First Hypothesis

The app is trying to use a Bluetooth SCO/Headset route for `Mini boost 2`, but both the calibration path and stream path still produce stereo 48 kHz media-style PCM. Bluetooth SCO/HFP playback is voice-call oriented and commonly expects mono 8 kHz or 16 kHz communication audio. The test path also has a dormant communication fallback that is not used for the unmatched second speaker.
