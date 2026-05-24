# Opencode WAL Checkpoint Failed

- Timestamp: 2026-05-24 18:17:54 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
Error: Unexpected error, check log file at /home/astra/.local/share/opencode/log/2026-05-24T124817.log for more details

Failed to run the query 'PRAGMA wal_checkpoint(PASSIVE)'
```

```text
Error: attempt to write a readonly database
```

## Reproduction Steps

1. Run `opencode run "In /home/astra/codex/DualBT, do not edit files. Run ./scripts/push-github.sh to push the current DualBT branch and tags to GitHub. If the script fails, report the exact error. If it succeeds, verify the remote branch and tags. Keep all output in English."`.
2. Observe that opencode exits with code 1 before running the requested push.
3. Run `opencode db 'PRAGMA wal_checkpoint(PASSIVE)'`.
4. Observe `attempt to write a readonly database`.

## First Hypothesis

Opencode's local SQLite state or log/session database is outside this sandbox's writable roots. The file is readable, but WAL checkpointing needs write access, so opencode fails before it can execute the GitHub push instruction.
