# App-Level Dual Audio System Route Group

Problem: [docs/problems/2026-05-25-app-level-dual-audio-system-route-group-needed.md](../problems/2026-05-25-app-level-dual-audio-system-route-group-needed.md)

## What failed

The previous app-level approaches could start two `AudioTrack` objects, but Android routed both to the same active classic A2DP output on the tested phone. Active A2DP handoff only switched the current output; it did not keep two Bluetooth speakers active together.

## What worked

Add a system route-group path using `MediaRouter2.RoutingController`:

1. Inspect selected and selectable system media routes.
2. Match selected DualBT speakers against those route names.
3. If the second speaker is selectable, call `selectRoute` so the system routing session can add it to the selected route group.
4. When the route group is active, play one default media output track and let Android duplicate that stream to the selected route group.
5. If no route group is exposed, keep blocking the stream instead of pretending dual playback is active.

## Why it worked

`MediaRouter2` is the public app-level seam that can add selectable routes to an existing media routing session. Android documents that selected routes in a routing session are expected to play the same media together. This does not bypass vendor or Bluetooth stack limits; it enables true app-level use of OS-provided dual-route support when present.

## Commands run

```bash
date '+%Y-%m-%d %H:%M:%S %Z'
adb -s d1bc5c4a shell input tap 540 2190
adb -s d1bc5c4a shell input tap 780 2218
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 160 files/logs/dualbt.log
```
