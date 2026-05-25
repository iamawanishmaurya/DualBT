# v0.2.37 System Route Group Unavailable On Xiaomi Test Device

## Exact error

```text
1779686323071	INFO	AudioOutputRouter	System route group probe track started, silentProbeBytes=4096
1779686323074	WARN	AudioOutputRouter	System route group probe failed: System route group unavailable. Selected=[Mini boost 4], selectable=[]
1779686323442	WARN	CaptureEngine	Output router could not start
1779686323443	WARN	DualBTService	Foreground service stopping because audio capture could not start
```

## Reproduction steps

1. Install DualBT `0.2.37`.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Tap `Start Stream`.
4. Accept the MediaProjection prompt.
5. Observe the service starts, creates a default media probe track, then stops because Android exposes no selectable system media route for the second Mini Boost speaker.

## Environment

- Date: 2026-05-25
- Device: Android phone `d1bc5c4a`
- OS family: MIUI/Xiaomi Android build
- App package: `com.xpwnit.dualbt`
- Version: `0.2.37`/versionCode `38`
- Android media volume: `2/15`

## First hypothesis

The tested Xiaomi build can connect both classic Bluetooth speakers but its system media routing provider exposes only the current active A2DP route to apps. Because `MediaRouter2` reports `selectable=[]`, DualBT cannot create Samsung-style simultaneous output on this phone with normal app permissions.
