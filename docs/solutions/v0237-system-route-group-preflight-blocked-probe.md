# v0.2.37 System Route Group Preflight Probe Fix

Problem: [docs/problems/2026-05-25-v0237-system-route-group-preflight-blocked-probe.md](../problems/2026-05-25-v0237-system-route-group-preflight-blocked-probe.md)

## What failed

The first v0.2.37 route-group implementation inspected `MediaRouter2` before DualBT had an active media output track. On the tested phone, that kept the Activity in the old one-A2DP-route block path and prevented the service from probing whether the OS could add a selectable route after playback became active.

## What worked

Allow a bounded system route-group probe when Android 11+ exposes one active classic A2DP route and active handoff would otherwise be the only path. During router startup, create one default media `AudioTrack`, write a short silent buffer to activate the media routing session, call `MediaRouter2.RoutingController#selectRoute` for matching selectable routes, then keep the output only if both selected speakers become active in the system route group.

## Why it worked

This separates "allowed to probe" from "dual route proven active." The UI can request capture to test the route group, but the service still stops instead of streaming if the OS does not expose true grouped routes.

## Commands run

```bash
adb -s d1bc5c4a shell input tap 540 2190
adb -s d1bc5c4a logcat -d -v time | rg 'DualBT|SystemRouteGroup|AudioOutputRouter|CaptureEngine|MainActivity' | tail -n 80
adb -s d1bc5c4a shell input tap 780 2218
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 160 files/logs/dualbt.log
```
