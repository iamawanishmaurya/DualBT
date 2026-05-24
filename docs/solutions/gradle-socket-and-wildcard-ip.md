# Gradle Socket And Wildcard IP Failures

- Problems:
  - [2026-05-24-gradle-wildcard-ip-failed.md](../problems/2026-05-24-gradle-wildcard-ip-failed.md)
  - [2026-05-24-gradle-daemon-socket-blocked.md](../problems/2026-05-24-gradle-daemon-socket-blocked.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

Gradle could not start normally in the sandbox because daemon TCP sockets and cache lock listener networking are blocked.

## What Worked

Used a writable Gradle home, launched Gradle with matching client JVM args plus the Gradle instrumentation agent, and patched only the temporary Gradle 8.5 cache in `/tmp/dualbt-gradle-home` with a no-op lock contention handler.

During physical device validation at 2026-05-24 20:20 IST, `/tmp/dualbt-gradle-home` had been recreated from the original durable cache, so the temporary Gradle patch had to be applied again.

## Why It Worked

Matching JVM args prevented Gradle from forking a socket-backed daemon. The temporary no-op lock contention handler avoided UDP socket creation for Gradle cache lock notifications. This is scoped to `/tmp` and does not modify project source or the system Gradle cache.

## Commands Run

```bash
mkdir -p /tmp/dualbt-gradle-home/wrapper/dists
cp -a /home/astra/.gradle/wrapper/dists/gradle-8.5-bin /tmp/dualbt-gradle-home/wrapper/dists/
javac -classpath '/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/*' -d /tmp/dualbt-gradle-patch/classes /tmp/dualbt-gradle-patch/src/org/gradle/cache/internal/locklistener/DefaultFileLockContentionHandler.java
jar uf /tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/gradle-persistent-cache-8.5.jar -C /tmp/dualbt-gradle-patch/classes org/gradle/cache/internal/locklistener/DefaultFileLockContentionHandler.class
```

Reapply command used during physical device validation:

```bash
javac -classpath '/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/*' -d /tmp/dualbt-gradle-patch/classes /tmp/dualbt-gradle-patch/src/org/gradle/cache/internal/locklistener/DefaultFileLockContentionHandler.java
jar uf /tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/gradle-persistent-cache-8.5.jar -C /tmp/dualbt-gradle-patch/classes org/gradle/cache/internal/locklistener/DefaultFileLockContentionHandler.class
```
