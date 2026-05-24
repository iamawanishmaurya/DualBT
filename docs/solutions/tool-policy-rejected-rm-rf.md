# Tool Policy Rejected Temp Cleanup Solution

- Problem: [2026-05-24-tool-policy-rejected-rm-rf.md](../problems/2026-05-24-tool-policy-rejected-rm-rf.md)
- What failed: A test command included `rm -rf /tmp/dualbt-session-test/classes`, and the sandbox rejected the entire command before execution.
- What worked: Compiling into a fresh directory, `/tmp/dualbt-session-test/classes-red-0946`, avoided recursive deletion.
- Why it worked: The sandbox policy blocked the destructive cleanup pattern, not Java compilation or writing under `/tmp`.
- Commands run:

```bash
mkdir -p /tmp/dualbt-session-test/classes-red-0946
javac -cp app/src/main/java -d /tmp/dualbt-session-test/classes-red-0946 app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/main/java/com/xpwnit/dualbt/state/StreamDevice.java app/src/main/java/com/xpwnit/dualbt/state/StreamSessionController.java
```
