# Gradle Javaagent Cache Missing

- Problem: [2026-05-24-gradle-javaagent-cache-missing.md](../problems/2026-05-24-gradle-javaagent-cache-missing.md)
- Timestamp: 2026-05-24 18:52:30 IST

## What Failed

The debug APK build failed before Gradle startup because `/tmp/dualbt-gradle-home` had been removed and the hard-coded Java agent path no longer existed.

## What Worked

Recreated `/tmp/dualbt-gradle-home`, copied the cached Gradle 8.5 distribution and module cache from `/home/astra/.gradle`, recreated `/tmp/dualbt-offline-maven`, and regenerated `/tmp/dualbt-debug.keystore`.

## Why It Worked

The durable Gradle and Android SDK caches still exist under `/home/astra`, while the writable build-time cache lives under `/tmp`. Rehydrating the `/tmp` paths restores the exact offline build inputs used by the project.

## Commands Run

```bash
mkdir -p /tmp/dualbt-gradle-home/wrapper/dists /tmp/dualbt-gradle-home/caches /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2 /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0
cp -a /home/astra/.gradle/wrapper/dists/gradle-8.5-bin /tmp/dualbt-gradle-home/wrapper/dists/
cp -a /home/astra/.gradle/caches/modules-2 /tmp/dualbt-gradle-home/caches/
cp /home/astra/.android/sdk/cmdline-tools/latest/lib/external/org/ow2/asm/asm/9.2/asm-9.2.jar /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2/asm-9.2.jar
cp /home/astra/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.2/b24408a1c2214bc57380faf45b37bd67be7732b5/asm-9.2.pom /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2/asm-9.2.pom
cp /home/astra/.gradle/caches/modules-2/files-2.1/com.squareup/javapoet/1.10.0/712c178d35185d8261295913c9f2a7d6867a6007/javapoet-1.10.0.jar /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0/javapoet-1.10.0.jar
cp /home/astra/.gradle/caches/modules-2/files-2.1/com.squareup/javapoet/1.10.0/111612d623e9d2047798f13324fcb29966f080e3/javapoet-1.10.0.pom /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0/javapoet-1.10.0.pom
keytool -genkeypair -v -keystore /tmp/dualbt-debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
```
