# Gradle Wrapper Cache Read-Only

- Problem: [2026-05-24-gradle-wrapper-cache-read-only.md](../problems/2026-05-24-gradle-wrapper-cache-read-only.md)
- Timestamp: 2026-05-24 08:11:10 IST

## What Failed

Running `./gradlew --version` with the default environment failed because Gradle tried to create a lock file under `/home/astra/.gradle`, which is read-only in this workspace.

## What Worked

Copied the existing cached Gradle 8.5 distribution into `/tmp/dualbt-gradle-home` and ran Gradle with `GRADLE_USER_HOME=/tmp/dualbt-gradle-home`.

## Why It Worked

The Gradle wrapper uses `GRADLE_USER_HOME` for distribution caches and lock files. Moving that location to `/tmp` gives Gradle a writable cache while reusing the already-installed Gradle distribution, avoiding a network download.

## Commands Run

```bash
./gradlew --version
sed -n '1,120p' gradle/wrapper/gradle-wrapper.properties
find /home/astra/.gradle/wrapper/dists -maxdepth 4 -type f
mkdir -p /tmp/dualbt-gradle-home/wrapper/dists
cp -a /home/astra/.gradle/wrapper/dists/gradle-8.5-bin /tmp/dualbt-gradle-home/wrapper/dists/
env GRADLE_USER_HOME=/tmp/dualbt-gradle-home ./gradlew --version
```

