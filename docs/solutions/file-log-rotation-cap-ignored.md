# File Log Rotation Cap Ignored Solution

- Problem: [2026-05-24-file-log-rotation-cap-ignored.md](../problems/2026-05-24-file-log-rotation-cap-ignored.md)
- What failed: `FileLogSink` forced `maxBytes` to at least 1024 bytes, so a test sink configured with 140 bytes never rotated.
- What worked: Preserve the caller's requested cap while only guarding against zero or negative values with `Math.max(1, maxBytes)`.
- Why it worked: The app still passes a production-sized cap, while tests and future callers can use a small cap to validate rotation deterministically.
- Commands run:

```bash
mkdir -p /tmp/dualbt-file-log-test/classes-green-0959
javac -cp app/src/main/java -d /tmp/dualbt-file-log-test/classes-green-0959 app/src/test/java/com/xpwnit/dualbt/logging/FileLogSinkTest.java app/src/main/java/com/xpwnit/dualbt/logging/LogStore.java app/src/main/java/com/xpwnit/dualbt/logging/FileLogSink.java
java -cp /tmp/dualbt-file-log-test/classes-green-0959 com.xpwnit.dualbt.logging.FileLogSinkTest
```
