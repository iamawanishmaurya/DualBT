# Volume Controls Lost After Activity Recreate

- Problem: [docs/problems/2026-05-24-volume-controls-lost-after-activity-recreate.md](../problems/2026-05-24-volume-controls-lost-after-activity-recreate.md)
- Solved: 2026-05-24 22:48 IST

## What Failed

After switching away from DualBT and returning later, `MainActivity` could be recreated with a fresh local `StreamSessionController`. The foreground service kept streaming, but activity-local state no longer knew streaming was active, so earlier UI volume updates could be skipped.

## What Worked

`MainActivity` now always forwards output-volume changes to `DualBTService`. The service safely handles volume-only intents: it applies the gain if capture is active, and stops itself if a volume-only update arrives without an active stream.

The later v0.2.18 fix also made the hardware media-volume path the preferred YouTube workflow, so volume control no longer depends on reopening DualBT while YouTube is playing.

## Why It Worked

The running foreground service is the owner of the active capture/output pipeline. Sending volume updates directly to the service removes the dependency on recreated activity state, while the service-side guard prevents accidental long-lived service starts.

## Commands Run

```bash
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell input tap 210 675
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 80 files/logs/dualbt.log
find app/src/test/java -name '*Test.java' | sort > /tmp/dualbt-plain-test-sources-v0218.txt && mkdir -p /tmp/dualbt-plain-tests-v0218/classes && javac -sourcepath app/src/main/java:app/src/test/java -d /tmp/dualbt-plain-tests-v0218/classes @/tmp/dualbt-plain-test-sources-v0218.txt && while IFS= read -r source; do class=${source#app/src/test/java/}; class=${class%.java}; class=${class//\//.}; java -cp /tmp/dualbt-plain-tests-v0218/classes "$class"; done < /tmp/dualbt-plain-test-sources-v0218.txt
```
