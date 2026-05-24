# Classic A2DP One Speaker Playback

- Problem: `docs/problems/2026-05-24-classic-a2dp-one-speaker-playback.md`
- Related problem: `docs/problems/2026-05-24-android-one-a2dp-output-route.md`
- Solved: 2026-05-24 23:20 IST

## What Failed

DualBT could connect and display two Mini Boost speaker entries, but Android routed actual media playback to one classic Bluetooth A2DP sink. The app then tried to compensate with a generic SCO/communication route, which did not produce independent second-speaker playback.

## What Worked

The app now treats this as unsupported on the current device route set:

- Streaming starts only if Android exposes two direct non-SCO media output routes for the selected speakers.
- Calibration test buttons only beep a selected speaker when a direct media route exists for that speaker.
- The output gain stays synced to the Android media volume, with the test device held at `3/15` for about 20% volume.

## Why It Worked

This fixes the app behavior, not the Android platform limitation. The public route list on this Redmi Note 9 Pro exposes one usable Bluetooth media sink for the Mini Boost pair. Blocking fake fallback routes prevents DualBT from claiming dual output when the OS is actually collapsing playback to one route.

For true simultaneous playback on this hardware class, the viable paths are outside this third-party app's current public API control:

- Pair the speakers to each other with their own TWS/party mode, then connect Android to that single combined speaker route.
- Use a phone and speakers that support Android Bluetooth LE Audio / audio sharing.
- Use a vendor/system build that exposes combined audio routing to apps or system components.

## Commands Run

```bash
adb -s d1bc5c4a shell dumpsys bluetooth_manager
adb -s d1bc5c4a shell dumpsys audio
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt cat files/logs/dualbt.log
adb -s d1bc5c4a shell cmd media_session volume --set 3
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt/.service.DualBTService
```

## Verification Result

With v0.2.21 installed, stream startup is blocked in the activity before MediaProjection:

```text
WARN MainViewModel Start blocked: Android exposes 1/2 direct media route(s). Generic SCO fallback is disabled.
```

The UI shows `Only 1/2 speaker routes available`, and `dumpsys activity services com.xpwnit.dualbt/.service.DualBTService` returned no running service.
