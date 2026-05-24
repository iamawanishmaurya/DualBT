# Test Buttons Same Physical Speaker

- Problem: `docs/problems/2026-05-24-test-buttons-same-physical-speaker.md`
- Solved: 2026-05-24 23:20 IST

## What Failed

`Test 1` and `Test 2` could both make the same physical Mini Boost speaker beep. The app treated Android's generic Bluetooth SCO/communication output as a fallback second speaker, but physical testing and logs showed it was not an independent media route.

## Research Options

1. Keep using the generic SCO fallback. Rejected because it caused the repeated failure: the same physical speaker could receive both tests.
2. Require two direct media output routes from Android's app-visible `AudioDeviceInfo` list. Chosen because it is the only public-API path that can be verified from inside the app.
3. Use Android combined audio routing strategy APIs. Rejected for this build because Android documents those as system APIs backed by vendor/HAL support, not normal third-party app APIs.
4. Use Bluetooth LE Audio / audio sharing. Valid for compatible phones and LE Audio accessories, but the tested Mini Boost speakers expose classic Bluetooth A2DP, not LE Audio broadcast sharing.
5. Use the speakers' own TWS/pairing mode so Android sees one route that the speaker hardware duplicates. Valid user workaround, but it is outside app-side routing control.

## What Worked

DualBT now requires two distinct direct non-SCO media routes before streaming starts. Calibration also blocks a selected speaker when Android does not expose a direct route for that speaker. The generic SCO fallback is no longer used for dual speaker output.

## Why It Worked

Android's `AudioRouting.setPreferredDevice()` is only a preferred route; the app must verify what route Android actually exposes and uses. On the Redmi Note 9 Pro test device, Android exposed one direct Bluetooth media route plus a generic communication route, so failing closed prevents false dual-output behavior.

## Commands Run

```bash
adb -s d1bc5c4a shell cmd media_session volume --set 3
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a logcat -c
adb -s d1bc5c4a shell am force-stop com.xpwnit.dualbt
adb -s d1bc5c4a shell monkey -p com.xpwnit.dualbt 1
adb -s d1bc5c4a shell dumpsys audio
classes=/tmp/dualbt-plain-tests-v0221/classes
sources=/tmp/dualbt-plain-test-sources-v0221.txt
mkdir -p "$classes"
find "$classes" -mindepth 1 -delete
find app/src/test/java -name '*Test.java' | sort > "$sources"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" @"$sources"
while IFS= read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp "$classes" "$class"; done < "$sources"
env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore ANDROID_HOME=/home/astra/.android/sdk ANDROID_SDK_ROOT=/home/astra/.android/sdk GRADLE_USER_HOME=/tmp/dualbt-gradle-home GRADLE_OPTS='-Dorg.gradle.daemon=false' JAVA_OPTS='--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Duser.country= -Duser.language=en -Duser.variant= -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar' PATH=/home/astra/.android/sdk/platform-tools:/home/astra/.android/sdk/emulator:/home/astra/.android/sdk/cmdline-tools/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/bin ./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell dumpsys package com.xpwnit.dualbt | sed -n '/versionCode/p;/versionName/p'
```

## Verification Result

On v0.2.20 with volume held at `3/15`, `Test 1` completed on the one exposed media route. A rapid `Test 2` tap was ignored while `Test 1` was running, and a standalone `Test 2` was blocked with:

```text
Test 2 blocked for Mini boost 2: Android exposes no direct media route for this speaker. Generic SCO fallback is disabled.
```

Streaming is blocked in v0.2.21 before MediaProjection with `Only 1/2 speaker routes available`; `DualBTService` does not start.
