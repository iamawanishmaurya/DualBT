# Device Launch Crash

- Timestamp: 2026-05-24 20:28:10 IST
- Environment: `/home/astra/codex/DualBT`, device `d1bc5c4a` (`Redmi Note 9 Pro`, API 31), APK `app/build/outputs/apk/debug/app-debug.apk`.

## Exact Error

UIAutomator dump shows the system crash dialog:

```text
DualBT keeps stopping
```

Window focus showed:

```text
mCurrentFocus=Window{... Application Error: com.xpwnit.dualbt}
```

## Reproduction Steps

1. Install `app/build/outputs/apk/debug/app-debug.apk` with `adb -s d1bc5c4a install -r`.
2. Grant `RECORD_AUDIO`, `BLUETOOTH_SCAN`, and `BLUETOOTH_CONNECT`.
3. Launch with `adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity`.
4. Observe MIUI crash dialog `DualBT keeps stopping`.
5. Dump UI with `adb -s d1bc5c4a shell uiautomator dump`.

## First Hypothesis

The default verified Java UI path has an Android runtime compatibility crash on a physical API 31 MIUI device. The next step is to retrieve the crash stack from `logcat`/Dropbox and fix the source-level root cause.

## Crash Stack Evidence

Captured at 2026-05-24 20:26:51 IST with:

```bash
adb -s d1bc5c4a logcat -b crash -d | tail -n 260
adb -s d1bc5c4a logcat -d -v time | rg -i 'FATAL EXCEPTION|AndroidRuntime|com\.xpwnit\.dualbt|xpwnit|dualbt|RuntimeException|Exception|Error' -C 8
```

Relevant stack:

```text
05-24 20:24:11.565 29231 29231 E AndroidRuntime: FATAL EXCEPTION: main
05-24 20:24:11.565 29231 29231 E AndroidRuntime: Process: com.xpwnit.dualbt, PID: 29231
05-24 20:24:11.565 29231 29231 E AndroidRuntime: java.lang.RuntimeException: Unable to start activity ComponentInfo{com.xpwnit.dualbt/com.xpwnit.dualbt.MainActivity}: java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.WindowInsetsController com.android.internal.policy.DecorView.getWindowInsetsController()' on a null object reference
05-24 20:24:11.565 29231 29231 E AndroidRuntime: Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.WindowInsetsController com.android.internal.policy.DecorView.getWindowInsetsController()' on a null object reference
05-24 20:24:11.565 29231 29231 E AndroidRuntime: 	at com.android.internal.policy.PhoneWindow.getInsetsController(PhoneWindow.java:3959)
05-24 20:24:11.565 29231 29231 E AndroidRuntime: 	at com.xpwnit.dualbt.MainActivity.configureWindow(MainActivity.java:128)
05-24 20:24:11.565 29231 29231 E AndroidRuntime: 	at com.xpwnit.dualbt.MainActivity.onCreate(MainActivity.java:72)
```

## Updated Hypothesis

`MainActivity.configureWindow()` calls `getWindow().getInsetsController()` before the decor view is fully attached on this MIUI/API 31 device. The fix should move the system bar appearance update until after `setContentView()`/decor attachment, or guard the `WindowInsetsController` lookup so launch cannot crash.

## Solution

Resolved by `docs/solutions/device-launch-crash.md`.
