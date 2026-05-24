# Watchdog Control Test Silent Failure

- Problem: [2026-05-24-watchdog-control-test-silent-failure.md](../problems/2026-05-24-watchdog-control-test-silent-failure.md)
- Timestamp: 2026-05-24 18:37:20 IST

## What Failed

`scripts/test-codex-watchdog-control.sh` exited with code 1 after the controller was stopped, but printed no failure message.

## What Worked

Tracing with `bash -x` showed the final `status` check was expected to fail, but because it was the last command in the script, the test returned that expected non-zero status. Rewriting the assertion as an explicit `if` block fixed the test harness.

## Why It Worked

The controller's stopped state is represented by a non-zero status command. The test must treat that non-zero exit as expected behavior, not as the test process exit code.

## Commands Run

```bash
bash -x scripts/test-codex-watchdog-control.sh
```
