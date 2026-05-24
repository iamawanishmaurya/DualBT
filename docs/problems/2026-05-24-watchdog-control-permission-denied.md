# Watchdog Control Permission Denied

- Timestamp: 2026-05-24 18:35:45 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, device `d1bc5c4a` connected.

## Exact Error

```text
./scripts/test-codex-watchdog-control.sh: line 25: /home/astra/codex/DualBT/scripts/codex-watchdog-control.sh: Permission denied
```

## Reproduction Steps

1. Run `./scripts/test-codex-watchdog-control.sh`.
2. The test attempts to execute `scripts/codex-watchdog-control.sh`.
3. The shell exits with code 126 because the control script is not executable.

## First Hypothesis

`scripts/codex-watchdog-control.sh` was added during an interrupted turn and never had its executable bit set.
