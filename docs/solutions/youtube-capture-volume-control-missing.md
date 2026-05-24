# YouTube Capture Volume Control Missing

- Problem: [docs/problems/2026-05-24-youtube-capture-volume-control-missing.md](../problems/2026-05-24-youtube-capture-volume-control-missing.md)
- Solved: 2026-05-24 22:48 IST

## What Failed

DualBT captures YouTube audio with playback capture and re-emits the PCM through its own `AudioTrack` outputs. The earlier app-screen volume controls changed DualBT gain, but they required bringing DualBT to the foreground. On this physical device, moving away from YouTube can stop or disrupt the YouTube playback session, so those controls were not enough for the real YouTube workflow.

## What Worked

DualBT v0.2.18 now observes Android system media-volume changes while the foreground service is running. Hardware volume keys update `STREAM_MUSIC`; the service maps that stream volume to DualBT output gain and applies it before writing captured PCM to the two output tracks.

Verified physical behavior:

- YouTube stayed foreground with `PlaybackState` state `3`.
- Volume down changed Android media volume to `2/15`, DualBT gain to `13%`, and the user confirmed the speakers got quieter.
- Volume up changed Android media volume to `7/15`, DualBT gain to `47%`, and the user confirmed the speakers got louder.

## Why It Worked

The source app remains in the foreground while Android processes hardware volume keys. DualBT's foreground service can observe the resulting media-volume setting and apply the same change to the captured PCM path it controls. That avoids relying on YouTube background playback, exported service intents, or manual in-app buttons during the listening session.

## Alternatives Considered

- In-app volume buttons: simple and already useful, but not enough when YouTube must stay foreground.
- Foreground notification actions: useful fallback, but less ergonomic and depends on notification UI access.
- Exporting a service intent for shell/external volume updates: rejected because it weakens the service boundary.
- System media-volume observer: chosen because it works with normal phone volume keys while YouTube remains active.
- Platform-level dual A2DP/LE Audio routing: the long-term best path for true multi-speaker routing, but not a quick app-side fix for volume control.

## Commands Run

```bash
mkdir -p /tmp/dualbt-volume-map-red/classes && javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-volume-map-red/classes app/src/test/java/com/xpwnit/dualbt/audio/OutputVolumeMapperTest.java
mkdir -p /tmp/dualbt-volume-map-green/classes && javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-volume-map-green/classes app/src/test/java/com/xpwnit/dualbt/audio/OutputVolumeMapperTest.java && java -cp /tmp/dualbt-volume-map-green/classes com.xpwnit.dualbt.audio.OutputVolumeMapperTest
find app/src/test/java -name '*Test.java' | sort > /tmp/dualbt-plain-test-sources-v0218.txt && mkdir -p /tmp/dualbt-plain-tests-v0218/classes && javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-plain-tests-v0218/classes @/tmp/dualbt-plain-test-sources-v0218.txt && while IFS= read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp /tmp/dualbt-plain-tests-v0218/classes "$class"; done < /tmp/dualbt-plain-test-sources-v0218.txt
env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore ANDROID_HOME=/home/astra/.android/sdk ANDROID_SDK_ROOT=/home/astra/.android/sdk GRADLE_USER_HOME=/tmp/dualbt-gradle-home GRADLE_OPTS='-Dorg.gradle.daemon=false' JAVA_OPTS='--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -XX:MaxMetaspaceSize=384m -XX:+HeapDumpOnOutOfMemoryError -Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Duser.country= -Duser.language=en -Duser.variant= -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar' PATH=/home/astra/.android/sdk/platform-tools:/home/astra/.android/sdk/emulator:/home/astra/.android/sdk/cmdline-tools/latest/bin:/usr/local/sbin:/usr/local/bin:/usr/bin ./gradlew --no-daemon --offline clean assembleDebug
/home/astra/.android/sdk/build-tools/34.0.0/aapt dump badging app/build/outputs/apk/debug/app-debug.apk | rg "package:|sdkVersion|targetSdkVersion"
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell am start -a android.intent.action.VIEW -d 'https://www.youtube.com/watch?v=343IJsEHdPU' com.google.android.youtube
adb -s d1bc5c4a shell input keyevent KEYCODE_VOLUME_DOWN
adb -s d1bc5c4a shell input keyevent KEYCODE_VOLUME_UP
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 60 files/logs/dualbt.log
adb -s d1bc5c4a shell dumpsys media_session | rg -n "YouTube playerlib|state=PlaybackState|metadata" -C 1
adb -s d1bc5c4a shell cmd media_session volume --get
```
