# UI Stays Streaming After Route Block

- Problem: `docs/problems/2026-05-24-ui-stays-streaming-after-route-block.md`
- Solved: 2026-05-24 23:41 IST

## What Failed

The service correctly rejected unsupported dual Bluetooth output and stopped, but `MainActivity` had already marked the controller as streaming after MediaProjection consent. The UI could therefore show `Streaming to 2 speaker(s)` even though `DualBTService` was no longer running.

## What Worked

Added `AndroidAudioRouteAvailability` and checked the selected route set before requesting or confirming MediaProjection capture. When Android exposes fewer than two direct media routes, the activity now blocks the start and shows:

```text
Only 1/2 speaker routes available
```

## Why It Worked

The activity and service now agree on the same route precondition. Unsupported route sets never enter `StreamSessionController`'s streaming state, so the UI cannot drift away from the service result.

## Commands Run

```bash
classes=/tmp/dualbt-plain-tests-v0221/classes
sources=/tmp/dualbt-plain-test-sources-v0221.txt
mkdir -p "$classes"
find "$classes" -mindepth 1 -delete
find app/src/test/java -name '*Test.java' | sort > "$sources"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" @"$sources"
while IFS= read -r source; do
  class=${source#app/src/test/java/}
  class=${class%.java}
  class=${class//\//.}
  java -cp "$classes" "$class"
done < "$sources"
env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore ANDROID_HOME=/home/astra/.android/sdk ANDROID_SDK_ROOT=/home/astra/.android/sdk GRADLE_USER_HOME=/tmp/dualbt-gradle-home GRADLE_OPTS='-Dorg.gradle.daemon=false' JAVA_OPTS='--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Duser.country= -Duser.language=en -Duser.variant= -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar' PATH=/home/astra/.android/sdk/platform-tools:/home/astra/.android/sdk/emulator:/home/astra/.android/sdk/cmdline-tools/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/bin ./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell am force-stop com.xpwnit.dualbt
adb -s d1bc5c4a shell cmd media_session volume --set 3
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell input tap 540 2210
adb -s d1bc5c4a shell input swipe 540 2090 540 850 500
adb -s d1bc5c4a shell input tap 540 1530
adb -s d1bc5c4a shell input tap 540 2215
./scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-ui-v0221-route-block.xml
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt/.service.DualBTService
```

## Verification Result

v0.2.21 blocked the start in `MainActivity` with:

```text
W/DualBT:MainViewModel: Start blocked: Android exposes 1/2 direct media route(s). Generic SCO fallback is disabled.
```

The UI showed `Only 1/2 speaker routes available`, no MediaProjection dialog appeared, `DualBTService` was not running, and media volume remained `3/15`.
