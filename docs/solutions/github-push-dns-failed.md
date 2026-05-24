# GitHub Push DNS Failed

- Problem: [2026-05-24-github-push-dns-failed.md](../problems/2026-05-24-github-push-dns-failed.md)
- Timestamp: 2026-05-24 09:30:16 IST
- Status: Environment-blocked, not fully solved inside this sandbox.

## What Failed

Pushing local commits to `https://github.com/iamawanishmaurya/DualBT.git` failed because the sandbox could not resolve `github.com`.

## Researched Options

1. Verify general DNS/network and retry after connectivity is restored.
   - Trade-off: Safest and preserves repository state, but requires external network availability.
2. Configure Git/OS proxy settings if the environment requires a proxy.
   - Trade-off: Effective in corporate networks, but this sandbox does not expose a usable proxy configuration to mutate.
3. Change DNS resolver settings such as `/etc/resolv.conf`.
   - Trade-off: Common Linux fix, but system resolver files are outside the writable workspace and not appropriate to modify here.
4. Switch the remote to SSH.
   - Trade-off: Can bypass HTTPS proxy issues, but still requires DNS/network plus SSH credentials.
5. Push from a host environment outside the sandbox using the local branch and tags.
   - Trade-off: Most reliable for this environment because local Git state is intact and the blocker is external networking.

References:

- GitHub Docs: troubleshooting connectivity to GitHub over HTTPS and SSH, https://docs.github.com/en/get-started/using-github/troubleshooting-connectivity-problems
- GitHub Docs: managing remote repositories, https://docs.github.com/en/get-started/git-basics/managing-remote-repositories
- Git documentation: `git remote`, https://git-scm.com/docs/git-remote

## What Worked

Local commit and tag creation worked with the external Git metadata directory at `/tmp/DualBT.git`. Remote push did not work in this sandbox.

## Why It Worked

The local Git repository does not require external DNS or network access. Remote push does, and the sandbox's network namespace cannot resolve GitHub.

## Decision

Do not retry the same push command in this environment. Keep local commits and tags intact, and push from an environment where `github.com` resolves, or after the sandbox network policy changes.

## 2026-05-24 Follow-up

The DNS blocker was checked again with `getent hosts github.com` at 2026-05-24 16:44:26 IST. It still failed with exit code 2 and no host records. A separate authentication blocker was also found and logged in [2026-05-24-github-cli-auth-invalid.md](../problems/2026-05-24-github-cli-auth-invalid.md).

At 2026-05-24 17:10:22 IST, DNS bypass was tested with curl `--resolve` and Git `http.curloptResolve`. Both failed to connect to GitHub on port 443, so the remaining blocker is broader outbound HTTPS access, not just hostname resolution. See [2026-05-24-github-direct-connect-blocked.md](../problems/2026-05-24-github-direct-connect-blocked.md).

At 2026-05-24 17:51:02 IST, a plain Git push was retried after the user removed Tailscale on the host and after `/tmp/DualBT.git` was recreated. The push still failed with `Could not resolve host: github.com`, so the host resolver still does not provide GitHub DNS to this shell.

## Commands Run

```bash
env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git push -u origin implementation/dualbt-v0.1.0
getent hosts github.com
timeout 20 curl --connect-timeout 10 --max-time 20 -I --resolve github.com:443:140.82.112.4 https://github.com/
timeout 20 env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git -c http.curloptResolve=github.com:443:140.82.112.4 ls-remote origin HEAD
timeout 30 env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git push -u origin implementation/dualbt-v0.1.0
```
