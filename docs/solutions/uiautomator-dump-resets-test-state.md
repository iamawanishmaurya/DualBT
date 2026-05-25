# UIAutomator Dump Resets Test State

- Problem: [docs/problems/2026-05-25-uiautomator-dump-resets-test-state.md](../problems/2026-05-25-uiautomator-dump-resets-test-state.md)
- Solved: 2026-05-25 08:53:43 IST

## What Failed

DualBT kept selected speakers and output gain only in `MainActivity`/`StreamSessionController` memory. On the Redmi Note 9 Pro, `uiautomator dump` could pause and recreate the activity while gathering verification XML. That recreated a fresh controller, resetting the physical test setup to `0/2` selected speakers and `100%` gain before the `Test 1` tap.

## What Worked

Persisted the selected speaker addresses and output gain in `SharedPreferences`, restored selected devices after each bonded-device scan, and added pure Java coverage for ordered address encoding plus session restore behavior.

## Why It Worked

The selected device identities are stable Bluetooth MAC addresses, not transient UI objects. Saving those addresses lets the recreated activity rebuild the selected-device order from the current scan result before rendering the test controls. Persisting output gain prevents the same recreation from returning physical tests to full gain.

## Commands Run

```bash
classes=/tmp/dualbt-state-persistence-red/classes
mkdir -p "$classes"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/test/java/com/xpwnit/dualbt/state/StreamSessionStateCodecTest.java

classes=/tmp/dualbt-state-persistence-green/classes
mkdir -p "$classes"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/test/java/com/xpwnit/dualbt/state/StreamSessionStateCodecTest.java
java -cp "$classes" com.xpwnit.dualbt.state.StreamSessionControllerTest
java -cp "$classes" com.xpwnit.dualbt.state.StreamSessionStateCodecTest

find app/src/test/java -name '*Test.java' | sort > /tmp/dualbt-plain-test-sources-v0229.txt
mkdir -p /tmp/dualbt-plain-tests-v0229/classes
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-plain-tests-v0229/classes @/tmp/dualbt-plain-test-sources-v0229.txt
while IFS= read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp /tmp/dualbt-plain-tests-v0229/classes "$class"; done < /tmp/dualbt-plain-test-sources-v0229.txt

env DUALBT_OFFLINE_MAVEN=/tmp/dualbt-offline-maven DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore ANDROID_HOME=/home/astra/.android/sdk ANDROID_SDK_ROOT=/home/astra/.android/sdk GRADLE_USER_HOME=/tmp/dualbt-gradle-home GRADLE_OPTS='-Dorg.gradle.daemon=false' ./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell cmd media_session volume --set 2
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-v0229-persist-after-dump.xml
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 60 files/logs/dualbt.log
adb -s d1bc5c4a shell cmd media_session volume --get
```

