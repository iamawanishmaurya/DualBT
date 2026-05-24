# Tool Policy Rejected Temp Cleanup

- Timestamp: 2026-05-24 09:46:09 IST
- Exact error:

```text
exec_command failed for `/usr/bin/zsh -lc 'rm -rf /tmp/dualbt-session-test/classes
mkdir -p /tmp/dualbt-session-test/classes
javac -cp app/src/main/java -d /tmp/dualbt-session-test/classes app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/main/java/com/xpwnit/dualbt/state/StreamDevice.java app/src/main/java/com/xpwnit/dualbt/state/StreamSessionController.java'`: CreateProcess { message: "Rejected(\"`/usr/bin/zsh -lc 'rm -rf /tmp/dualbt-session-test/classes\\nmkdir -p /tmp/dualbt-session-test/classes\\njavac -cp app/src/main/java -d /tmp/dualbt-session-test/classes app/src/test/java/com/xpwnit/dualbt/state/StreamSessionControllerTest.java app/src/main/java/com/xpwnit/dualbt/state/StreamDevice.java app/src/main/java/com/xpwnit/dualbt/state/StreamSessionController.java'` rejected: blocked by policy\")" }
```

- Reproduction steps:
  1. Run a shell command that includes `rm -rf /tmp/dualbt-session-test/classes`.
  2. Attempt to create the classes directory and compile the plain Java stream-session test in the same command.
- Environment:
  - Workspace: `/home/astra/codex/DualBT`
  - Shell: zsh
  - Sandbox: workspace-write, approval policy `never`
  - Writable temp root: `/tmp`
- First hypothesis: The command was rejected by policy because it contained a recursive force deletion pattern, even though the target was under `/tmp`.
