# Test 2 9/15 Rerun Tap Missed Because YouTube Was Foreground

## Exact error
The requested Test 2 rerun at Android media volume `9/15` did not trigger a new DualBT calibration test. App logs had no new `Calibration test requested` entry after the tap.

Relevant evidence:

```text
adb -s d1bc5c4a shell cmd media_session volume --get
[V] volume is 9 in range [0..15]
```

`dumpsys audio` showed YouTube had become active again:

```text
requestAudioFocus() ... callingPack=com.google.android.youtube
new player ... uid/pid:10199/... type:android.media.AudioTrack ... usage=USAGE_MEDIA
```

The DualBT app log after the tap ended at Activity pause/resume events and did not include a new Test 2 calibration request.

## Reproduction steps
1. User starts YouTube music after the 2/15 app beep is inaudible.
2. Set Android media volume to `9/15`.
3. Send a coordinate tap intended for DualBT's Test 2 button.
4. Inspect DualBT logs.
5. No new Test 2 calibration request appears.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.33` / versionCode `34`
- External app active: YouTube
- Android media volume: `9/15`

## First hypothesis
The phone was no longer showing DualBT when the coordinate tap was sent, so the tap went to the wrong foreground surface. Bring DualBT to the foreground, keep volume at `9/15`, verify the UI, and retry Test 2.
