# Codex Opencode GitHub Environment Mismatch

- Timestamp: 2026-05-24 18:02:11 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
fatal: unable to access 'https://github.com/iamawanishmaurya/testing01.git/': Could not resolve host: github.com
```

```text
error connecting to api.github.com
check your internet connection or https://githubstatus.com
```

```text
curl: (6) Could not resolve host: github.com
```

## Reproduction Steps

1. Confirm that opencode created `/home/astra/test` with remote `https://github.com/iamawanishmaurya/testing01.git`.
2. Run `git -C /home/astra/test status --short --branch`.
3. Observe the local repo status works.
4. Run `git -C /home/astra/test ls-remote origin HEAD`.
5. Observe DNS failure for `github.com`.
6. Run `gh repo view iamawanishmaurya/testing01 --json name,url,visibility,defaultBranchRef`.
7. Observe GitHub API connection failure.
8. Run `curl -I https://github.com/`.
9. Observe curl DNS failure.

## First Hypothesis

Opencode is executing outside the restricted Codex shell or with different network/keyring integration. The same local Git repository and remote URL are visible from this shell, but outbound GitHub DNS/network and GitHub CLI auth are not available here.
