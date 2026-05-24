# Calibration Overlap Releases Active Track

- Problem: `docs/problems/2026-05-24-calibration-overlap-releases-active-track.md`
- Solved: 2026-05-24 23:20 IST

## What Failed

Rapidly pressing `Test 1` and `Test 2` on v0.2.19 could release the active calibration `AudioTrack` while the first test thread was still writing PCM.

Exact failure:

```text
ERROR SpeakerTest Calibration failed for Mini boost 1 | Unable to retrieve AudioTrack pointer for write()
```

## What Worked

Added `SpeakerTestRunGate` and wired `SpeakerCalibrationPlayer` to ignore a new calibration request while one test is already running.

## Why It Worked

The crash was a race between the first test's writer thread and the second test's startup path. Blocking overlapping starts keeps ownership of the active `AudioTrack` with the running calibration test until it reaches its normal `finally` release path.

## Commands Run

```bash
javac -d /tmp/dualbt-test-classes app/src/main/java/com/xpwnit/dualbt/audio/SpeakerTestRunGate.java app/src/test/java/com/xpwnit/dualbt/audio/SpeakerTestRunGateTest.java
java -cp /tmp/dualbt-test-classes com.xpwnit.dualbt.audio.SpeakerTestRunGateTest
classes=/tmp/dualbt-plain-tests-v0221/classes
sources=/tmp/dualbt-plain-test-sources-v0221.txt
mkdir -p "$classes"
find "$classes" -mindepth 1 -delete
find app/src/test/java -name '*Test.java' | sort > "$sources"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" @"$sources"
while IFS= read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp "$classes" "$class"; done < "$sources"
env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore ANDROID_HOME=/home/astra/.android/sdk ANDROID_SDK_ROOT=/home/astra/.android/sdk GRADLE_USER_HOME=/tmp/dualbt-gradle-home GRADLE_OPTS='-Dorg.gradle.daemon=false' JAVA_OPTS='--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Duser.country= -Duser.language=en -Duser.variant= -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar' PATH=/home/astra/.android/sdk/platform-tools:/home/astra/.android/sdk/emulator:/home/astra/.android/sdk/cmdline-tools/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/bin ./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell cmd media_session volume --set 3
```

## Verification Result

On v0.2.20, pressing `Test 1` and quickly pressing `Test 2` produced:

```text
WARN SpeakerTest Test 2 ignored because another calibration test is still running
INFO SpeakerTest Test 1 finished for Mini boost 1
```

The `Unable to retrieve AudioTrack pointer for write()` error did not recur.
