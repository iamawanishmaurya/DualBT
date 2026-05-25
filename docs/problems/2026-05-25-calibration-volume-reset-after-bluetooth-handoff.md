# Calibration Volume Reset After Bluetooth Handoff

- Timestamp: 2026-05-25 00:54 IST
- Step: v0.2.27 A/B physical calibration after `Test 1` produced no sound.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.27`, versionCode `28`

## Exact Error

User feedback after `Test 1`:

```text
No sound
```

Route and volume evidence immediately after the failure:

```text
adb -s d1bc5c4a shell cmd media_session volume --get
[V] volume is 0 in range [0..15]
```

`dumpsys audio` also reported:

```text
STREAM_MUSIC:
   Muted: true
   streamVolume:0
   Current: ... 80 (bt_a2dp): 2 ...
```

## Reproduction Steps

1. Install v0.2.27 on physical device `d1bc5c4a`.
2. Select both Mini Boost speakers.
3. Set media volume externally to `2/15`.
4. Run targeted `Test 2`.
5. Run A2DP `Test 1`.
6. Check `cmd media_session volume --get`.

## First Hypothesis

The Bluetooth A2DP active-device handoff or SCO cleanup can mute/reset the active media stream after the external ADB volume command. Calibration playback should enforce the low test volume in-app immediately before playing each tone.
