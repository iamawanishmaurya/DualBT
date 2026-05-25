# AudioDeviceInfo Stale After Accepted A2DP Switch

- Timestamp: 2026-05-25 09:05:36 IST
- Environment: Android device `d1bc5c4a`, DualBT `0.2.30` (`versionCode=31`), package `com.xpwnit.dualbt`, selected speakers `Mini boost 1` and `Mini boost 2`, in-app gain `10%`, Android media volume `2/15`.
- Exact error: Retried `Test 1` after Android reported the A2DP sink as `Mini boost 1`, but the app still blocked because `AudioManager.getDevices(GET_DEVICES_OUTPUTS)` repeatedly exposed only the other Mini Boost address (`41:42:2E:9E:5E:AE`) as the visible A2DP output.
- Reproduction steps:
  1. Install and launch DualBT `0.2.30`.
  2. Keep `Mini boost 1` and `Mini boost 2` selected with in-app gain `10%`.
  3. Set Android media volume to `2/15`.
  4. Confirm with `dumpsys audio` that the connected A2DP sink includes `41:42:26:B3:62:1C`.
  5. Tap `Test 1`.
  6. Capture `run-as com.xpwnit.dualbt tail -n 80 files/logs/dualbt.log`.
- Evidence:
  ```text
  INFO SpeakerTest A2DP route activation for Mini boost 1: setActiveDevice(Mini boost 4@41:42:26:B3:62:1C) returned true
  DEBUG SpeakerTest Bluetooth output: Mini boost 4@41:42:2E:9E:5E:AE#type=8
  WARN SpeakerTest No matching A2DP route for Mini boost 1 after delayed route rescans
  WARN SpeakerTest No matching Bluetooth communication route is exposed for Mini boost 1; generic SCO output will not be reused
  WARN SpeakerTest Test 1 blocked for Mini boost 1: Android exposes no direct or targeted communication route for this speaker.
  ```
- First hypothesis: On this MIUI/Android build, the hidden A2DP active-device switch can be accepted before the public `AudioDeviceInfo` route list reflects the target address. When that happens, forcing a preferred device is unsafe, but a default media `AudioTrack` after an accepted A2DP switch may route to the newly active A2DP device.

