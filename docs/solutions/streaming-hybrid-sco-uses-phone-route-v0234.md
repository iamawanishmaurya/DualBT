# Solution: Streaming Hybrid SCO Used Phone Route In v0.2.34

## Problem

Linked problem: `docs/problems/2026-05-25-streaming-hybrid-sco-uses-phone-route-v0234.md`

## What Failed

The v0.2.34 streaming router treated the phone's generic SCO output as a valid second Bluetooth route. The service started two output tracks, but Mini boost 1 was assigned to `communication-sco` routed to `Redmi Note 9 Pro@00:00:00:00:00:00#type=7`.

## What Worked

v0.2.35 rejects generic phone SCO routes and only allows the hybrid strategy when Android exposes a communication route that matches one of the selected speaker addresses. After accepting MediaProjection consent, the v0.2.35 stream started without `communication-sco` and without the `Redmi Note 9 Pro` route:

```text
Using experimental active A2DP handoff because Android exposes only 1/2 direct media route(s)
Track 0 started for Mini boost 1, mode=media, preferred=default, preferredAccepted=false
Track 1 started for Mini boost 2, mode=media, preferred=Mini boost 4@41:42:2E:9E:5E:AE#type=8
Audio playback capture started: buffer=30720, routes=Mini boost 1, Mini boost 2
```

## Why It Worked

The router now distinguishes a real selected-speaker SCO route from Android's generic phone communication route. When no matching SCO route exists, it falls back to active A2DP media handoff instead of creating a phone-routed voice-communication track.

## Commands Run

```bash
adb -s d1bc5c4a shell input tap 780 2218
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-ui-v0235-stream-started-1010.xml
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 220 files/logs/dualbt.log
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt
adb -s d1bc5c4a shell dumpsys audio
adb -s d1bc5c4a shell dumpsys media_session
```

## Follow-Up

The phone/SCO route bug is fixed, but v0.2.35 still logs that both media tracks route to one distinct device route. That remaining platform-routing failure is tracked separately.
