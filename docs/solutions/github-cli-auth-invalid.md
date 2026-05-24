# GitHub CLI Auth Invalid

- Problem: [2026-05-24-github-cli-auth-invalid.md](../problems/2026-05-24-github-cli-auth-invalid.md)
- Timestamp: 2026-05-24 16:44:26 IST
- Status: Environment-blocked, not fully solved inside this sandbox.

## What Failed

`gh auth status` reports that the active `iamawanishmaurya` token is invalid. No `GH_TOKEN`, `GITHUB_TOKEN`, `GH_ENTERPRISE_TOKEN`, or `GITHUB_ENTERPRISE_TOKEN` environment variable is set in this shell.

## What Worked

Local Git state verification worked with the external Git metadata directory at `/tmp/DualBT.git`. The branch, commits, tags, and remote URL are present locally.

## Why It Worked

Local verification does not require GitHub authentication. Remote push requires both network access to GitHub and a valid credential.

## Decision

Do not run interactive `gh auth login` from this sandbox. Re-authenticate from an environment with network access and an interactive terminal, then push the local branch and tags with the explicit external Git metadata variables.

## Commands Run

```bash
gh --version
gh auth status
for name in GH_TOKEN GITHUB_TOKEN GH_ENTERPRISE_TOKEN GITHUB_ENTERPRISE_TOKEN; do if [ -n "$(eval printf %s \"\$$name\")" ]; then echo "$name=SET"; else echo "$name=UNSET"; fi; done
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git remote -v
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git status --short --branch
for name in HTTP_PROXY HTTPS_PROXY ALL_PROXY NO_PROXY http_proxy https_proxy all_proxy no_proxy; do value=$(eval printf %s \"\$$name\"); if [ -n "$value" ]; then echo "$name=SET"; else echo "$name=UNSET"; fi; done
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config --list --show-origin
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config branch.implementation/dualbt-v0.1.0.remote origin
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config branch.implementation/dualbt-v0.1.0.merge refs/heads/implementation/dualbt-v0.1.0
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git config push.default current
```
