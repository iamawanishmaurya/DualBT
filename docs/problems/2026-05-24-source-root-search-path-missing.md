# Source Root Search Path Missing

- Timestamp: 2026-05-24 08:04:46 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
rg: src: No such file or directory (os error 2)
```

## Reproduction Steps

1. Run `rg --files app src .` from `/home/astra/codex/DualBT`.
2. Observe `rg` reports that the top-level `src` path is missing.

## First Hypothesis

This Android project keeps sources under `app/src`, not a repository-level `src` directory. Future file searches should target `app/src` or the repository root only.
