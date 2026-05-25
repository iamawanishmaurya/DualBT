# Test 1 A2DP Handoff Block Resolution

Problem: `docs/problems/2026-05-25-test1-blocked-after-a2dp-switch-v0233.md`

## What failed
DualBT v0.2.33 switched the real Android media route toward Mini boost 1, but blocked the Test 1 calibration beep because public `AudioDeviceInfo` metadata still showed the other Mini Boost route. The old path then tried SCO fallback, which did not expose a targeted communication route and left the test blocked.

## What worked
DualBT v0.2.34 treats an attempted A2DP handoff as sufficient to use default media playback when the public route list is stale. At Android media volume `9/15` and in-app gain `100%`, Test 1 produced an audible beep on physical `Mini boost 1`.

## Why it worked
The user's YouTube check proved that the real media route switched to Mini boost 1 even when the app could not immediately match the public output route. Default media playback after the handoff follows the system's active A2DP endpoint, while avoiding the SCO fallback that blocked playback and affected media volume.

## Commands run
```bash
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-route-planner-red/classes app/src/test/java/com/xpwnit/dualbt/audio/SpeakerCalibrationRoutePlannerTest.java
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-route-planner-green/classes app/src/test/java/com/xpwnit/dualbt/audio/SpeakerCalibrationRoutePlannerTest.java
java -cp /tmp/dualbt-route-planner-green/classes com.xpwnit.dualbt.audio.SpeakerCalibrationRoutePlannerTest
find app/src/test/java -name '*Test.java' | sort > /tmp/dualbt-plain-test-sources-v0234.txt
javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-plain-tests-v0234/classes @/tmp/dualbt-plain-test-sources-v0234.txt
./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell cmd media_session volume --set 9
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-ui-v0234-test1-9.xml
adb -s d1bc5c4a logcat -c
adb -s d1bc5c4a shell input tap 310 906
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 160 files/logs/dualbt.log
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell dumpsys audio
```
