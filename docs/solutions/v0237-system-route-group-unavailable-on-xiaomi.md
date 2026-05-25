# v0.2.37 Xiaomi System Route Group Limitation

Problem: [docs/problems/2026-05-25-v0237-system-route-group-unavailable-on-xiaomi.md](../problems/2026-05-25-v0237-system-route-group-unavailable-on-xiaomi.md)

## What failed

The bounded system route-group probe reached the service and activated a default media `AudioTrack`, but the Xiaomi system controller reported one selected route and no selectable routes:

```text
Selected=[Mini boost 4], selectable=[]
```

That means this phone does not expose a public app-level route group for the second classic Bluetooth speaker.

## What worked

The v0.2.37 implementation now performs the strongest safe app-level attempt:

1. Allows Start Stream to reach the service for an Android 11+ route-group probe.
2. Starts a default media probe track.
3. Asks `MediaRouter2` to add matching selectable routes.
4. Continues only if both target speakers become active selected routes.
5. Stops the service if the OS exposes no route group.

## Why it worked

The app now supports true simultaneous output on Android devices that expose route groups through `MediaRouter2`, including Samsung-style or LE Audio sharing paths. On this Xiaomi/classic-A2DP setup, the OS does not expose the needed route group, so stopping is the correct verified behavior.

## Commands run

```bash
adb -s d1bc5c4a shell input tap 780 2218
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt
adb -s d1bc5c4a logcat -d -v time | rg 'DualBT|SystemRouteGroup|AudioOutputRouter|CaptureEngine|DualBTService' | tail -n 180
adb -s d1bc5c4a shell dumpsys audio | rg -n "MediaRouter|Routes|A2DP|a2dp|Mini|Bluetooth|Device" | tail -n 120
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 160 files/logs/dualbt.log
```
