# Device Launch Crash Solution

- Timestamp: 2026-05-24 20:40:20 IST
- Problem: `docs/problems/2026-05-24-device-launch-crash.md`

## What Failed

The physical Redmi Note 9 Pro launch crashed before UI rendering. Logcat showed `MainActivity.configureWindow()` calling `PhoneWindow.getInsetsController()` before the decor view was available:

```text
java.lang.NullPointerException: Attempt to invoke virtual method 'android.view.WindowInsetsController com.android.internal.policy.DecorView.getWindowInsetsController()' on a null object reference
	at com.xpwnit.dualbt.MainActivity.configureWindow(MainActivity.java:128)
	at com.xpwnit.dualbt.MainActivity.onCreate(MainActivity.java:72)
```

## What Worked

Split early window color setup from system-bar appearance setup:

- `configureWindow()` now only sets status/navigation bar colors early.
- `applySystemBarAppearance()` runs after `setContentView(root)`.
- `SystemBarAppearancePolicy` prevents light system-bar application until Android R+, light theme, and content-root availability are all true.
- The controller is now obtained from the root view with a null guard instead of from `PhoneWindow` before decor creation.

## Why It Worked

On this MIUI/API 31 device, `PhoneWindow.getInsetsController()` dereferenced a null decor view during early activity creation. Deferring the appearance call until after content view creation avoids the null decor state while preserving the light-theme status bar behavior.

## Commands Run

```bash
adb -s d1bc5c4a logcat -b crash -d | tail -n 260
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-ui-policy-test app/src/test/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicyTest.java
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-plain-tests app/src/test/java/com/xpwnit/dualbt/audio/AudioCaptureSpecTest.java app/src/test/java/com/xpwnit/dualbt/audio/PcmSplitterTest.java app/src/test/java/com/xpwnit/dualbt/bt/BluetoothSpeakerCatalogTest.java app/src/test/java/com/xpwnit/dualbt/logging/FileLogSinkTest.java app/src/test/java/com/xpwnit/dualbt/state/StreamRoutePlanTest.java app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/test/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicyTest.java
./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a logcat -b crash -d
```

## Verification Evidence

- Rebuilt APK installed successfully.
- `pidof com.xpwnit.dualbt` returned PID `31641`.
- Window focus stayed on `com.xpwnit.dualbt/.MainActivity`, not `Application Error`.
- UI dump showed `DualBT`, `Bluetooth Ready`, bonded Bluetooth devices, and stream controls.
- `logcat -b crash` had no new DualBT `AndroidRuntime` crash after the fix.
