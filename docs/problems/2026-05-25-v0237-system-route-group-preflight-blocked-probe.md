# v0.2.37 System Route Group Probe Blocked Before Service Start

## Exact error

```text
05-25 10:42:57.586 W/DualBT:MainViewModel(27768): Start blocked: Android exposes one active A2DP route at a time; active-device handoff cannot keep two classic Bluetooth speakers playing simultaneously.
```

## Reproduction steps

1. Install and launch DualBT `0.2.37`.
2. Keep `Mini boost 1` and `Mini boost 2` selected.
3. Scroll to `Start Stream`.
4. Tap `Start Stream`.
5. Observe the app blocks before MediaProjection and before the service can create an active default media track.

## Environment

- Date: 2026-05-25
- Device: Android phone `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- Version: `0.2.37`/versionCode `38`
- Build: `app/build/outputs/apk/debug/app-debug.apk`

## First hypothesis

`MediaRouter2` route grouping may only expose selected/selectable routes after an app owns an active media routing session. The current Activity preflight only inspects routes before playback exists, so it can block the route-group path too early. Start Stream should allow a bounded system-route-group probe on Android 11+ when one active A2DP route is present, and the service/router should stop if the route group is still not active after the probe.
