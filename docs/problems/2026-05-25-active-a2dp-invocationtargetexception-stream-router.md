# Active A2DP InvocationTargetException In Stream Router

- Date: 2026-05-25 00:19:18 IST
- Environment: Physical Android device `d1bc5c4a`, DualBT v0.2.24, selected speakers `Mini boost 1` (`41:42:26:B3:62:1C`) and `Mini boost 2` (`41:42:2E:9E:5E:AE`), Android media volume `2/15`

## Exact Error

```text
1779648551866	WARN	AudioOutputRouter	Using experimental active A2DP handoff because Android exposes only 1/2 direct media route(s)
1779648552817	INFO	AudioOutputRouter	Active A2DP handoff for track 0 Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
1779648552937	INFO	AudioOutputRouter	Track 0 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
1779648553645	INFO	AudioOutputRouter	Active A2DP handoff for track 1 Mini boost 2: A2DP active-device switch unavailable: InvocationTargetException
1779648553650	WARN	AudioOutputRouter	Active A2DP handoff could not prepare output for Mini boost 2
1779648553666	WARN	CaptureEngine	Output router could not start
1779648553667	WARN	DualBTService	Foreground service stopping because audio capture could not start
```

## Reproduction Steps

1. Install and launch DualBT v0.2.24 on physical device `d1bc5c4a`.
2. Set Android media volume to `2/15`.
3. Select `Mini boost 1` and `Mini boost 2`.
4. Tap `Start Stream`.
5. Accept the MediaProjection prompt.
6. Inspect `files/logs/dualbt.log`.

## First Hypothesis

The reflective `BluetoothA2dp.setActiveDevice()` call is reaching the platform method, but the platform method throws internally when DualBT tries to switch the active A2DP device while the first `AudioTrack` is already started. The logged message currently hides the underlying cause inside `InvocationTargetException`, so the first fix should expose the wrapped exception and avoid starting output until the route sequence is fully prepared.
