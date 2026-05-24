# Offline Gradle Resolution

- Problems:
  - [2026-05-24-gradle-plugin-resolution-offline.md](../problems/2026-05-24-gradle-plugin-resolution-offline.md)
  - [2026-05-24-agp-classpath-offline-artifacts-missing.md](../problems/2026-05-24-agp-classpath-offline-artifacts-missing.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

The writable Gradle home initially had no plugin or classpath artifacts, and remote repositories were unavailable.

## What Worked

Copied the global Gradle module cache into `/tmp/dualbt-gradle-home` and added an optional `DUALBT_OFFLINE_MAVEN` repository for missing classpath artifacts.

## Why It Worked

Gradle can resolve dependencies from a filesystem Maven repository and copied module cache while running in offline mode.

## Commands Run

```bash
mkdir -p /tmp/dualbt-gradle-home/caches
cp -a /home/astra/.gradle/caches/modules-2 /tmp/dualbt-gradle-home/caches/
mkdir -p /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2 /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0
cp /home/astra/.android/sdk/cmdline-tools/latest/lib/external/org/ow2/asm/asm/9.2/asm-9.2.jar /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2/asm-9.2.jar
cp /home/astra/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.2/b24408a1c2214bc57380faf45b37bd67be7732b5/asm-9.2.pom /tmp/dualbt-offline-maven/org/ow2/asm/asm/9.2/asm-9.2.pom
cp /home/astra/.gradle/caches/modules-2/files-2.1/com.squareup/javapoet/1.10.0/712c178d35185d8261295913c9f2a7d6867a6007/javapoet-1.10.0.jar /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0/javapoet-1.10.0.jar
cp /home/astra/.gradle/caches/modules-2/files-2.1/com.squareup/javapoet/1.10.0/111612d623e9d2047798f13324fcb29966f080e3/javapoet-1.10.0.pom /tmp/dualbt-offline-maven/com/squareup/javapoet/1.10.0/javapoet-1.10.0.pom
```

