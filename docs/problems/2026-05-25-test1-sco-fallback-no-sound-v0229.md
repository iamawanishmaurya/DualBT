# Test 1 SCO Fallback No Sound On v0.2.29

- Timestamp: 2026-05-25 08:55:35 IST
- Environment: Android device `d1bc5c4a`, DualBT `0.2.29` (`versionCode=30`), package `com.xpwnit.dualbt`, selected speakers `Mini boost 1` and `Mini boost 2`, in-app gain `10%`, Android media volume set to `2/15` before test.
- Exact error: User confirmed `No sound` after tapping `Test 1`, even though app logs showed the calibration path started.
- Reproduction steps:
  1. Install and launch DualBT `0.2.29`.
  2. Select `Mini boost 1` (`41:42:26:B3:62:1C`) and `Mini boost 2` (`41:42:2E:9E:5E:AE`).
  3. Set in-app gain to `10%` and Android media volume to `2/15`.
  4. Tap `Test 1`.
  5. Ask the user which physical speaker played.
- Evidence:
  ```text
  INFO MainViewModel Calibration test requested for Mini boost 1 as speaker 1
  INFO SpeakerTest A2DP route activation for Mini boost 1: A2DP active-device switch unavailable: InvocationTargetException caused by SecurityException: Need BLUETOOTH permission ... android.permission.BLUETOOTH_PRIVILEGED.
  WARN SpeakerTest Test 1 using targeted Headset/SCO route for Mini boost 1
  INFO SpeakerTest Test 1 started for Mini boost 1, mode=communication-sco, preferred=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, preferredAccepted=true, routed=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, bytes=102400
  [V] volume is 0 in range [0..15]
  ```
- User result: `No sound`.
- First hypothesis: The fallback route is not actually targeting the Mini Boost speaker. The code is accepting the phone earpiece/speaker communication device (`type=7`) after Headset/SCO activation, so calibration audio starts but is routed to a non-audible or wrong output path.

