# Watchdog Control Test Silent Failure

- Timestamp: 2026-05-24 18:36:30 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, device `d1bc5c4a` connected.

## Exact Error

```text
./scripts/test-codex-watchdog-control.sh
# exited with code 1 and no stdout/stderr
```

## Reproduction Steps

1. Run `chmod +x scripts/codex-watchdog-control.sh`.
2. Run `./scripts/test-codex-watchdog-control.sh`.
3. Observe the command exits with code 1 and no visible output.

## First Hypothesis

One of the test assertions is failing silently through `rg -q` or status-command exit handling. The next step is to trace the test with `bash -x` to find the failing assertion.
