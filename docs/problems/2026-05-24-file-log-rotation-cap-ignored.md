# File Log Rotation Cap Ignored

- Timestamp: 2026-05-24 09:59:27 IST
- Exact error:

```text
Exception in thread "main" java.lang.AssertionError: rotation keeps latest entry only: expected 1 but got 3
	at com.xpwnit.dualbt.logging.FileLogSinkTest.assertEquals(FileLogSinkTest.java:42)
	at com.xpwnit.dualbt.logging.FileLogSinkTest.main(FileLogSinkTest.java:27)
```

- Reproduction steps:
  1. Compile `FileLogSinkTest`, `LogStore`, and `FileLogSink`.
  2. Run `java -cp /tmp/dualbt-file-log-test/classes-green-0959 com.xpwnit.dualbt.logging.FileLogSinkTest`.
  3. Observe that the file retains three lines after appending an entry that should rotate the tiny test log.
- Environment:
  - Workspace: `/home/astra/codex/DualBT`
  - JDK: OpenJDK 21
  - Test output directory: `/tmp/dualbt-file-log-test/classes-green-0959`
- First hypothesis: `FileLogSink` clamps `maxBytes` to at least 1024 bytes, so the test's 140-byte rotation threshold is ignored.
