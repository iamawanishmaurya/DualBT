# UIAutomator Dump Resets Test State

- Timestamp: 2026-05-25 08:46:59 IST
- Environment: Android device `d1bc5c4a`, DualBT `0.2.28` (`versionCode=29`), package `com.xpwnit.dualbt`, Android media volume `2/15`.
- Exact error: After selecting `Mini boost 1` and `Mini boost 2` and reducing in-app gain to `10%`, running `scripts/dump-ui-safe.sh d1bc5c4a ...` caused the Activity/ViewModel to recreate. The next live UI dump showed `Select 2 speakers`, `0/2`, `100%`, and disabled `Test 1`/`Test 2`; the attempted `Test 1` tap produced no calibration-start log.
- Reproduction steps:
  1. Launch DualBT `0.2.28` on `d1bc5c4a`.
  2. Select `Mini boost 1` and `Mini boost 2`.
  3. Reduce in-app gain from `100%` to `10%`.
  4. Run `scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-v0228-lowgain-ready.xml`.
  5. Attempt `adb -s d1bc5c4a shell input tap 310 906` for `Test 1`.
  6. Check `run-as com.xpwnit.dualbt tail -n 100 files/logs/dualbt.log` and dump the UI again.
- Evidence:
  - Log after UI dump included `MainActivity Activity paused`, `MainActivity Activity created`, `MainViewModel ViewModel initialized`, and a fresh bonded scan.
  - UI after the attempted test showed `0/2`, `100%`, and disabled `Test 1`/`Test 2`.
  - App log did not include a `Test 1 started` or route activation entry for the tap.
- First hypothesis: selected speakers and output gain are stored only in the current ViewModel instance. When UIAutomator/MIUI recreates the Activity, the app loses the selected-device IDs and gain, so physical testing state disappears before the test button is pressed.

