# UI Stays Streaming After Route Block

- Timestamp: 2026-05-24 23:32 IST
- Step: Installed v0.2.20 on-device stream route verification.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.20`, versionCode `21`
  - Selected speakers: `Mini boost 1`, `Mini boost 2`
  - Media volume: `3/15`

## Exact Error

The foreground service correctly blocked output and stopped:

```text
W/DualBT:AudioOutputRouter: Dual Bluetooth output blocked: Android exposes 1/2 direct media route(s). Generic SCO fallback is disabled because it can route both tracks to the same speaker.
W/DualBT:CaptureEngine: Output router could not start
W/DualBT:DualBTService: Foreground service stopping because audio capture could not start
I/DualBT:DualBTService: Service destroyed
```

But the activity UI still displayed:

```text
Streaming to 2 speaker(s)
2/2
```

`dumpsys activity services com.xpwnit.dualbt/.service.DualBTService` showed:

```text
(nothing)
```

## Reproduction Steps

1. Install DualBT v0.2.20.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Tap `Start Stream`.
4. Accept MediaProjection permission.
5. Observe service logs showing route rejection and service shutdown.
6. Dump the UI hierarchy and observe that the activity still reports streaming.

## First Hypothesis

`MainActivity` marks `StreamSessionController` as streaming immediately after MediaProjection consent, before `DualBTService` can reject the route. The activity should perform the same direct-route availability check before requesting/confirming capture permission so unsupported route sets never enter the streaming UI state.
