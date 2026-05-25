# Solution: v0.2.36 Route Availability Result Constructor Mismatch

## Problem

Linked problem: `docs/problems/2026-05-25-v0236-route-availability-result-constructor-mismatch.md`

## What Failed

`AndroidAudioRouteAvailability.Result` gained a fourth `statusMessage` constructor argument, but the invalid-context early returns still called the old three-argument constructor.

## What Worked

Updated each early return to provide a short UI-safe status message:

```text
Audio context unavailable
Select 2 speakers
Audio routing unavailable
```

## Why It Worked

All `Result` construction paths now match the new constructor signature and provide both a detailed log message and a compact status string for the UI.

## Commands Run

```bash
./gradlew --no-daemon --offline clean assembleDebug
```

## Result

The Android debug build completed successfully in 38s and produced `app/build/outputs/apk/debug/app-debug.apk`.
