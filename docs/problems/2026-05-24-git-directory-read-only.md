# Git Directory Read-Only

- Timestamp: 2026-05-24 08:05:50 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
chmod: changing permissions of '.git': Read-only file system
```

## Reproduction Steps

1. Confirm `.git` exists and is empty with `ls -la .git`.
2. Run `chmod u+w .git` from `/home/astra/codex/DualBT`.
3. Observe the command exits with code 1 and the error above.

## First Hypothesis

The workspace contains a read-only placeholder `.git` directory. Since it cannot be modified or replaced, Git operations need an explicit writable `--git-dir` outside the checkout.
