# Git Metadata External Directory

- Problems:
  - [2026-05-24-git-repository-detection-failed.md](../problems/2026-05-24-git-repository-detection-failed.md)
  - [2026-05-24-git-directory-read-only.md](../problems/2026-05-24-git-directory-read-only.md)
- Timestamp: 2026-05-24 08:07:00 IST

## What Failed

Git did not recognize `/home/astra/codex/DualBT` as a repository because `.git` is an empty placeholder. Updating `.git` in place failed because the directory is mounted read-only.

## What Worked

Initialized Git metadata in `/tmp/DualBT.git`, used `/home/astra/codex/DualBT` as the work tree, created the implementation branch, and attached the provided GitHub URL as `origin`.

## Why It Worked

Git supports separating repository metadata from the working tree through `GIT_DIR` and `GIT_WORK_TREE`. This avoids writing into the read-only `.git` placeholder while preserving normal add, status, commit, tag, and push operations for the current session.

## Commands Run

```bash
git status --short --branch
git rev-parse --show-toplevel
ls -la .git
stat .git
chmod u+w .git
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git init
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git checkout -b implementation/dualbt-v0.1.0
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git remote add origin https://github.com/iamawanishmaurya/DualBT.git
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
```

