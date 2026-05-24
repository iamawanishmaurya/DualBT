# Solution: Dual Bluetooth Output Routing Missing In Verified Java Path

## Problem

Linked problem: [docs/problems/2026-05-24-dual-output-routing-missing.md](../problems/2026-05-24-dual-output-routing-missing.md)

## What Failed

The verified Java APK path could select two Bluetooth routes and start `AudioPlaybackCapture`, but it only split captured PCM into two byte buffers. It did not create playback outputs or write either split buffer to `AudioTrack`, so the physical speaker test was not meaningful for actual playback.

## What Worked

Added a Java output path in the verified build:

- `AudioOutputRouteMatcher` matches same-name speakers by Bluetooth address first, then by unused name fallback.
- `AndroidAudioOutputRouter` creates two streaming `AudioTrack` outputs, prefers matched Bluetooth output devices with `setPreferredDevice`, and writes both split PCM buffers.
- `AndroidPlaybackCaptureEngine` now owns the output router, excludes DualBT's own UID from capture, and forwards split PCM to the router.
- The UI button now says `Start Stream` instead of `Start Mock Stream`.

## Why It Worked

`AudioTrack` is the public Android playback API available in the verified Java fallback build. `AudioRouting.setPreferredDevice` is the public route preference hook for an `AudioTrack`, and the app can verify actual routing with `getRoutedDevice` while the track is playing. DualBT now logs both preferred and routed devices so the physical test can distinguish app output writes from platform routing limits.

## Commands Run

```bash
classes=/tmp/dualbt-all-java-tests-v0211b-$$
mkdir -p "$classes"
find app/src/test/java -name '*Test.java' | sort > /tmp/dualbt-test-sources-v0211b.txt
javac -d "$classes" -sourcepath app/src/main/java:app/src/test/java @/tmp/dualbt-test-sources-v0211b.txt
while read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp "$classes" "$class"; done < /tmp/dualbt-test-sources-v0211b.txt

env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven \
DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore \
ANDROID_HOME=/home/astra/.android/sdk \
ANDROID_SDK_ROOT=/home/astra/.android/sdk \
GRADLE_USER_HOME=/tmp/dualbt-gradle-home \
GRADLE_OPTS='-Dorg.gradle.daemon=false' \
JAVA_OPTS='--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Duser.country= -Duser.language=en -Duser.variant= -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar' \
PATH=/home/astra/.android/sdk/platform-tools:/home/astra/.android/sdk/emulator:/home/astra/.android/sdk/cmdline-tools/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/bin \
./gradlew --no-daemon --offline clean assembleDebug

adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt cat files/logs/dualbt.log
adb -s d1bc5c4a shell dumpsys audio
```

## Remaining Caveat

This fixes the missing app-side writer. It does not prove the Redmi/MIUI platform can route two classic A2DP speakers independently. That remaining physical-device limitation is tracked in [docs/problems/2026-05-24-android-one-a2dp-output-route.md](../problems/2026-05-24-android-one-a2dp-output-route.md).

