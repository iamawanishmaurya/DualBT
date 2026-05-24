# Watchdog Control Permission Denied

- Problem: [2026-05-24-watchdog-control-permission-denied.md](../problems/2026-05-24-watchdog-control-permission-denied.md)
- Timestamp: 2026-05-24 18:37:20 IST

## What Failed

`scripts/test-codex-watchdog-control.sh` could not execute `scripts/codex-watchdog-control.sh` because the control script did not have its executable bit set.

## What Worked

Running `chmod +x scripts/codex-watchdog-control.sh` made the script executable.

## Why It Worked

The control script has a valid shell shebang, but Unix requires execute permission before it can be invoked directly.

## Commands Run

```bash
chmod +x scripts/codex-watchdog-control.sh
```
