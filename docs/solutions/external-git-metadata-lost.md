# External Git Metadata Lost

- Problem: [2026-05-24-external-git-metadata-lost.md](../problems/2026-05-24-external-git-metadata-lost.md)
- Timestamp: 2026-05-24 17:45:10 IST
- Status: Pending reinitialization.

## What Failed

The external Git metadata directory `/tmp/DualBT.git` disappeared, so Git commands using `GIT_DIR=/tmp/DualBT.git` fail before they can inspect, commit, or push.

## What Worked

The working tree at `/home/astra/codex/DualBT` still contains the project files, documentation, helper script, source files, and ignored build artifacts.

## Why It Worked

Project files live in the workspace writable root, while the Git object database was stored in `/tmp`. The workspace survived, but `/tmp` state did not.

## What Worked

Reinitializing `/tmp/DualBT.git`, switching to `implementation/dualbt-v0.1.0`, reattaching `origin`, and restoring branch tracking made Git commands usable again.

Committing the restored workspace snapshot created root commit `3183d11`, and local tag `v0.2.8` was recreated on that snapshot.

## Why It Worked

Git supports using an external object database and index through `GIT_DIR` and `GIT_WORK_TREE`, avoiding the read-only `.git` placeholder inside the workspace.

## Decision

Recreate external Git metadata at `/tmp/DualBT.git`, reattach `origin`, recommit the current workspace snapshot, and recreate the latest release tag before attempting plain Git push again.

## Commands Run

```bash
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
find /tmp -maxdepth 2 -type d -name 'DualBT.git' -o -name '.git'
ls -la .git
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git init
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git checkout -B implementation/dualbt-v0.1.0
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git remote add origin https://github.com/iamawanishmaurya/DualBT.git
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config branch.implementation/dualbt-v0.1.0.remote origin
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config branch.implementation/dualbt-v0.1.0.merge refs/heads/implementation/dualbt-v0.1.0
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config push.default current
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git add -A
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git commit -m "chore: restore workspace snapshot for github publish"
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git tag v0.2.8
```
