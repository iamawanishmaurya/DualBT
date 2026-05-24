# DualBT — Phases to Completion

> **Current State**: All 23 Kotlin source files, 4 NDK C++ files, 3 resource XMLs, and full Gradle config exist.  
> **Goal**: Compilable, installable, and fully functional app running in Android emulator.

---

## 📊 Environment Snapshot

| Item | Status |
|------|--------|
| Java | ✅ OpenJDK 21 |
| ADB | ✅ `/usr/bin/adb` v35.0.2 |
| Android SDK | ✅ `/home/astra/.android/sdk` |
| NDK | ✅ Present in SDK |
| CMake | ✅ Present in SDK |
| Emulator binary | ⚠️ Not on PATH (exists in SDK) |
| Gradle Wrapper JAR | ✅ 43KB (valid) |
| `gradlew` | ✅ Executable (minimal but functional) |
| Source files | ✅ 23 Kotlin + 4 C++ + 3 XML |

---

## Phase A — Build System Fixes
> Fix all config issues so `./gradlew assembleDebug` succeeds.

### A1. Fix `app/build.gradle.kts` — Add `prefab = true`
- [ ] **File**: [app/build.gradle.kts](file:///home/astra/codex/DualBT/app/build.gradle.kts)
- [ ] Change `buildFeatures { compose = true }` → `buildFeatures { compose = true; prefab = true }`
- **Why**: NDK's `CMakeLists.txt` uses `find_package(oboe REQUIRED CONFIG)` which requires prefab

### A2. Fix `local.properties` — Set `ANDROID_HOME` env
- [ ] **File**: [local.properties](file:///home/astra/codex/DualBT/local.properties)
- [ ] Verify `sdk.dir=/home/astra/.android/sdk` is correct (already is ✅)
- [ ] Export `ANDROID_HOME=/home/astra/.android/sdk` in shell before building

### A3. Verify SDK components installed
- [ ] Check that required SDK components exist:
  ```
  ls /home/astra/.android/sdk/platforms/android-34/
  ls /home/astra/.android/sdk/build-tools/
  ls /home/astra/.android/sdk/ndk/
  ls /home/astra/.android/sdk/cmake/
  ```
- [ ] If `platforms/android-34` missing → install via `sdkmanager "platforms;android-34"`
- [ ] If `build-tools` missing → install matching version
- [ ] If NDK missing → install via `sdkmanager "ndk;26.1.10909125"` (or whatever's needed)

### A4. Set `ANDROID_HOME` and add tools to PATH
- [ ] Run:
  ```bash
  export ANDROID_HOME=/home/astra/.android/sdk
  export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin
  ```

### A5. First build attempt
- [ ] Run `./gradlew assembleDebug` from project root
- [ ] Capture and analyze any errors
- [ ] Go to Phase B if compilation errors occur

---

## Phase B — Code Fixes & Compilation
> Fix any Kotlin/C++ compilation errors until clean build.

### B1. Fix Kotlin compilation errors (if any)
Common expected issues and fixes:

- [ ] **Import resolution**: Verify all imports in every `.kt` file resolve correctly
- [ ] **Compose compiler version**: Ensure `kotlinCompilerExtensionVersion = "1.5.8"` matches Kotlin `1.9.22`
  - Kotlin 1.9.22 → Compose Compiler 1.5.8 ✅ (correct pairing)
- [ ] **Hilt + kapt**: Verify `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject` annotations compile
- [ ] **Material3 API**: Verify all Material3 composable APIs used actually exist in BOM `2024.02.00`

### B2. Fix NDK/C++ compilation errors (if any)
- [ ] **Oboe via prefab**: If `find_package(oboe)` fails, fallback plan:
  - Option 1: Remove `externalNativeBuild` block from `app/build.gradle.kts` entirely (skip NDK)
  - Option 2: Download Oboe headers manually and adjust `CMakeLists.txt`
  - Option 3: Use `FetchContent` in CMake to pull Oboe
- [ ] **JNI function names**: Verify C++ JNI function names match exactly:
  ```
  Java_com_xpwnit_dualbt_audio_AudioSplitterBridge_nativeXxx
  ```
- [ ] The app already has a Kotlin-only fallback (`AudioSplitterKotlin.kt`), so NDK is optional

### B3. Fix `StatusBar` deprecation warning
- [ ] [StatusBar.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/StatusBar.kt) — `LogLevel.values()` is deprecated in newer Kotlin
  - Replace with `LogLevel.entries` if Kotlin 1.9+

### B4. Fix potential `LogScreen` issues
- [ ] [LogScreen.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/LogScreen.kt) — Same `LogLevel.values()` deprecation
- [ ] Verify `collectAsStateWithLifecycle()` import works with the lifecycle BOM

### B5. Iterative fix cycle
- [ ] Run `./gradlew assembleDebug` after each fix
- [ ] If same error appears twice → **web search for 3-5 solutions**, pick best
- [ ] Repeat until `BUILD SUCCESSFUL`

### B6. Verify APK generated
- [ ] Check `app/build/outputs/apk/debug/app-debug.apk` exists
- [ ] Check APK size is reasonable (should be ~5-15MB)

---

## Phase C — Emulator Setup & Install
> Get the APK running in an Android emulator.

### C1. Find or create emulator AVD
- [ ] List existing AVDs:
  ```bash
  $ANDROID_HOME/emulator/emulator -list-avds
  ```
- [ ] If none exist, create one:
  ```bash
  # Install system image
  $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-34;google_apis;x86_64"
  # Create AVD
  $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n DualBT_Test -k "system-images;android-34;google_apis;x86_64" -d pixel_6
  ```

### C2. Launch emulator
- [ ] Start emulator (headless if no display):
  ```bash
  $ANDROID_HOME/emulator/emulator -avd DualBT_Test -no-window -no-audio &
  ```
- [ ] Wait for boot: `adb wait-for-device && adb shell getprop sys.boot_completed`
- [ ] Verify: `adb devices` shows device online

### C3. Install APK
- [ ] Install: `adb install app/build/outputs/apk/debug/app-debug.apk`
- [ ] If install fails with signature error → `adb install -r -t app-debug.apk`
- [ ] Verify installed: `adb shell pm list packages | grep dualbt`

### C4. Launch app
- [ ] Launch:
  ```bash
  adb shell am start -n com.xpwnit.dualbt/.MainActivity
  ```
- [ ] Check logcat for crashes:
  ```bash
  adb logcat -s DualBT:* AndroidRuntime:E | head -100
  ```

---

## Phase D — Runtime Testing & Validation
> Verify every feature works correctly in the emulator.

### D1. UI Rendering
- [ ] App launches without crash
- [ ] Glassmorphism dark theme renders correctly
- [ ] Animated background orbs are visible and moving
- [ ] Header shows "DualBT" with gradient text
- [ ] "⚡ Emulator Mode" badge is visible (since no real BT)

### D2. Mock Bluetooth Devices
- [ ] Two mock devices appear: "Mock Speaker 1" and "Mock Speaker 2"
- [ ] Tapping a device card selects it (green checkmark, glow animation)
- [ ] Tapping again deselects it
- [ ] Status bar updates: "Select 2 speakers" → "Select 1 more" → "Ready to stream"
- [ ] Speaker count badge shows "1/2" → "2/2"

### D3. Streaming Controls
- [ ] "Start Mock Stream" button appears when 2 devices selected
- [ ] Button has gradient background when enabled
- [ ] Button is disabled/dim when no devices selected
- [ ] Tapping "Start Mock Stream":
  - Button changes to "Stop Streaming" (red)
  - Pulse animations appear on selected device cards
  - Status shows "Streaming to 2 speaker(s)"
  - GraphicEq icon appears in status bar
  - Status dot pulses green
- [ ] Tapping "Stop Streaming" reverses all the above

### D4. Animations
- [ ] Background orbs animate continuously (3 colored circles moving in elliptical paths)
- [ ] Device cards have press scale animation (shrink on press)
- [ ] Selection checkmark has scale+fade entrance animation
- [ ] Status text transitions with fade animation
- [ ] Stream button icon transitions with fade animation
- [ ] Pulse animation rings appear during streaming

### D5. Log Screen
- [ ] Tapping bug icon (top-right) opens log screen with slide-up animation
- [ ] Logs show real events (ViewModel init, device scan, etc.)
- [ ] Filter chips work (ALL / DEBUG / INFO / WARN / ERROR)
- [ ] Each log entry shows: level badge, tag, message, timestamp
- [ ] "Clear Logs" button clears all entries
- [ ] Close button dismisses with slide-down animation

### D6. Logging System Verification
- [ ] Check logcat for DualBT tags:
  ```bash
  adb logcat -s "DualBT:*" | head -50
  ```
- [ ] Verify log entries include:
  - `DualBT:MainActivity — Activity created`
  - `DualBT:MainViewModel — ViewModel initialized`
  - `DualBT:MainViewModel — Devices updated: 2, emulator=true`
  - `DualBT:BTScanner` entries
- [ ] Verify in-app log screen shows the same entries

### D7. Theme Switching (if applicable)
- [ ] If device is set to light mode → light glassmorphism theme renders
- [ ] If dark mode → dark glassmorphism theme renders
- [ ] Colors, glass effects, and borders adapt appropriately

### D8. Responsiveness
- [ ] UI works on different screen sizes (rotate emulator if possible)
- [ ] LazyColumn scrolls smoothly with many items
- [ ] No janky animations or frame drops visible in logcat

---

## Phase E — Final Polish & Completion
> Last fixes, documentation, and confirmation.

### E1. Fix any runtime crashes
- [ ] Review full logcat output for any exceptions
- [ ] Fix any `NullPointerException`, `ClassCastException`, etc.
- [ ] Verify Hilt injection works (no `UninitializedPropertyAccessException`)

### E2. Permission handling
- [ ] App should handle Bluetooth permissions gracefully on Android 12+
- [ ] `BLUETOOTH_CONNECT` and `BLUETOOTH_SCAN` require runtime permission on API 31+
- [ ] If not already handled → add runtime permission request in `MainActivity`

### E3. Edge cases
- [ ] App doesn't crash if "Start Streaming" pressed with 0 devices
- [ ] App doesn't crash if "Refresh" pressed multiple times rapidly
- [ ] Navigating away and back doesn't lose state
- [ ] App survives configuration change (rotation)

### E4. Build cleanup
- [ ] Remove any unused imports
- [ ] Ensure `proguard-rules.pro` has Hilt keep rules:
  ```
  -keep class dagger.hilt.** { *; }
  -keep class javax.inject.** { *; }
  -keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
  ```

### E5. Final verification
- [ ] Clean build: `./gradlew clean assembleDebug`
- [ ] Install and run one final time
- [ ] All D1-D8 checks pass
- [ ] Screenshot or screen recording captured as proof
- [ ] Update [task.md](file:///home/astra/.gemini/antigravity/brain/1596a3de-48c5-4d3a-8765-af3bfe6b62ed/task.md) — mark everything complete

---

## 📋 Summary of Files

### Source Files (23 Kotlin)
| File | Status | Layer |
|------|--------|-------|
| [DualBTApp.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/DualBTApp.kt) | ✅ | App entry |
| [MainActivity.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/MainActivity.kt) | ✅ | Activity |
| [BTDevice.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/bt/BTDevice.kt) | ✅ | Bluetooth |
| [BTScanner.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/bt/BTScanner.kt) | ✅ | Bluetooth |
| [BTRepository.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/bt/BTRepository.kt) | ✅ | Bluetooth |
| [CaptureEngine.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/CaptureEngine.kt) | ✅ | Audio |
| [AudioSplitterKotlin.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/AudioSplitterKotlin.kt) | ✅ | Audio |
| [AudioRouter.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/AudioRouter.kt) | ✅ | Audio |
| [AudioSplitterBridge.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/AudioSplitterBridge.kt) | ✅ | NDK bridge |
| [DualBTService.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/service/DualBTService.kt) | ✅ | Service |
| [MainViewModel.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/vm/MainViewModel.kt) | ✅ | ViewModel |
| [AppModule.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/di/AppModule.kt) | ✅ | DI |
| [AppLogger.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/logging/AppLogger.kt) | ✅ | Logging |
| [Color.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/theme/Color.kt) | ✅ | Theme |
| [Type.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/theme/Type.kt) | ✅ | Theme |
| [Theme.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/theme/Theme.kt) | ✅ | Theme |
| [GlassCard.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/components/GlassCard.kt) | ✅ | UI component |
| [AnimatedBackground.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/components/AnimatedBackground.kt) | ✅ | UI component |
| [PulseAnimation.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/components/PulseAnimation.kt) | ✅ | UI component |
| [MainScreen.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/MainScreen.kt) | ✅ | UI screen |
| [DeviceCard.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/DeviceCard.kt) | ✅ | UI component |
| [StatusBar.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/StatusBar.kt) | ✅ | UI component |
| [LogScreen.kt](file:///home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/ui/LogScreen.kt) | ✅ | UI screen |

### NDK Files (4 C++)
| File | Status |
|------|--------|
| [CMakeLists.txt](file:///home/astra/codex/DualBT/app/src/main/cpp/CMakeLists.txt) | ✅ |
| [AudioSplitter.h](file:///home/astra/codex/DualBT/app/src/main/cpp/AudioSplitter.h) | ✅ |
| [AudioSplitter.cpp](file:///home/astra/codex/DualBT/app/src/main/cpp/AudioSplitter.cpp) | ✅ |
| [jni_bridge.cpp](file:///home/astra/codex/DualBT/app/src/main/cpp/jni_bridge.cpp) | ✅ |

### Build Config
| File | Status | Issue? |
|------|--------|--------|
| [build.gradle.kts (project)](file:///home/astra/codex/DualBT/build.gradle.kts) | ✅ | — |
| [build.gradle.kts (app)](file:///home/astra/codex/DualBT/app/build.gradle.kts) | ⚠️ | Missing `prefab = true` |
| [settings.gradle.kts](file:///home/astra/codex/DualBT/settings.gradle.kts) | ✅ | — |
| [gradle.properties](file:///home/astra/codex/DualBT/gradle.properties) | ✅ | — |
| [gradle-wrapper.properties](file:///home/astra/codex/DualBT/gradle/wrapper/gradle-wrapper.properties) | ✅ | Gradle 8.5 |
| [gradlew](file:///home/astra/codex/DualBT/gradlew) | ✅ | Minimal but works |
| [local.properties](file:///home/astra/codex/DualBT/local.properties) | ✅ | — |

---

## ⏱️ Estimated Timeline

| Phase | Est. Time | Blocking? |
|-------|-----------|-----------|
| **A — Build Fixes** | 5-10 min | Yes (must pass first) |
| **B — Code Fixes** | 10-30 min | Yes (iterative) |
| **C — Emulator Setup** | 5-15 min | Yes |
| **D — Testing** | 10-20 min | No |
| **E — Polish** | 5-10 min | No |
| **Total** | ~35-85 min | — |
