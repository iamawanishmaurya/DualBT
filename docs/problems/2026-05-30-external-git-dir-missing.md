# External Git Metadata Directory Missing

## Exact error

```text
fatal: not a git repository: '/tmp/DualBT.git'
```

## Reproduction steps

1. Run `GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch`.
2. Observe Git exits with status 128 because `/tmp/DualBT.git` is missing.

## Environment

- Date: 2026-05-30
- Worktree: `/home/astra/codex/DualBT`
- Expected external Git metadata: `/tmp/DualBT.git`

## First hypothesis

The external Git metadata directory lived in `/tmp` and was removed between sessions. The worktree still exists, so the fix is to locate or reconstruct usable Git metadata before committing the laptop audio recovery script.
