# Codex Watchdog

`scripts/codex-watchdog.sh` records a heartbeat every minute so long-running Codex work has an external progress trail.

Run it manually when you want polling:

```bash
./scripts/codex-watchdog.sh
```

Useful bounded run for checks:

```bash
./scripts/codex-watchdog.sh --interval 60 --count 5
```

The default log file is `docs/codex-watchdog.log`. The default health check is a lightweight Git status using the external Git metadata directory at `/tmp/DualBT.git`.
