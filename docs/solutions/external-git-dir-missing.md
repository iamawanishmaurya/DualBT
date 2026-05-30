# External Git Metadata Recovery

Problem: [docs/problems/2026-05-30-external-git-dir-missing.md](../problems/2026-05-30-external-git-dir-missing.md)

## What failed

The previous workflow relied on `/tmp/DualBT.git`, but that external Git metadata directory was no longer present.

## What worked

Reinitialized local Git metadata in `/home/astra/codex/DualBT/.git`, restored the `origin` remote, fetched `implementation/dualbt-v0.1.0` and tags from GitHub, pointed the local branch at `origin/implementation/dualbt-v0.1.0`, and used a mixed reset to rebuild the index without changing working tree files.

## Why it worked

The missing metadata was only a local `/tmp` state problem. Fetching from GitHub restored commit/tag history, while `git reset --mixed` updated the index to the remote branch without overwriting the current script and documentation edits.

## Commands run

```bash
GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
git init
git remote add origin https://github.com/iamawanishmaurya/DualBT.git
git fetch origin implementation/dualbt-v0.1.0 --tags
git symbolic-ref HEAD refs/heads/implementation/dualbt-v0.1.0
git update-ref refs/heads/implementation/dualbt-v0.1.0 refs/remotes/origin/implementation/dualbt-v0.1.0
git reset --mixed refs/remotes/origin/implementation/dualbt-v0.1.0
git status --short --branch
```
