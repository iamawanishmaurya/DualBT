# External Git Metadata Lost

- Timestamp: 2026-05-24 17:45:10 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
fatal: not a git repository: '/tmp/DualBT.git'
```

## Reproduction Steps

1. Run `env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch`.
2. Observe that Git exits with code 128 and reports `/tmp/DualBT.git` is not a repository.
3. Run `find /tmp -maxdepth 2 -type d -name 'DualBT.git'`.
4. Observe no `/tmp/DualBT.git` directory is present.

## First Hypothesis

The external Git metadata stored under `/tmp` was removed when the host environment changed or `/tmp` was refreshed. The workspace files remain present, but local commit history and tags stored only under `/tmp/DualBT.git` are no longer available.
