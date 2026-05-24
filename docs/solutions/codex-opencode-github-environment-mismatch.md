# Codex Opencode GitHub Environment Mismatch

- Problem: [2026-05-24-codex-opencode-github-environment-mismatch.md](../problems/2026-05-24-codex-opencode-github-environment-mismatch.md)
- Timestamp: 2026-05-24 18:02:11 IST
- Status: Environment-blocked, not fully solved inside this sandbox.

## What Failed

The GitHub repository `iamawanishmaurya/testing01` that opencode created successfully is visible locally under `/home/astra/test`, but this Codex shell cannot query it through Git or `gh`.

## What Worked

Local inspection of `/home/astra/test` worked. The repo has branch `master...origin/master`, remote `https://github.com/iamawanishmaurya/testing01.git`, and `test.txt` exists.

## Why It Worked

Local filesystem access does not require DNS or outbound HTTPS. Git and GitHub API operations do, and those still fail from this shell.

## Researched Options

1. Run the push from opencode or the same host shell that successfully created `testing01`.
   - Trade-off: Most efficient because that environment already has GitHub DNS, HTTPS, and keyring auth working.
2. Give this Codex shell the same unrestricted network namespace and keyring/session environment as opencode.
   - Trade-off: Correct if Codex must push directly, but it requires changing the runtime outside the repo.
3. Configure a working DNS resolver or proxy for this shell.
   - Trade-off: Can fix DNS/proxy-only failures, but previous direct-IP HTTPS tests also failed here.
4. Use plain Git with stored credentials once this shell has network access.
   - Trade-off: Keeps the repo flow simple and avoids `gh`, but still depends on outbound GitHub access.
5. Use the GitHub CLI only from the environment where `gh auth status` reports keyring authentication.
   - Trade-off: Works in opencode, but this shell reports an invalid `gh` token and cannot reach `api.github.com`.

## Decision

Do not keep retrying GitHub pushes from this Codex shell. The fastest reliable path is to push DualBT from the same environment that pushed `testing01`, or change this Codex shell runtime so `getent hosts github.com`, `curl -I https://github.com/`, and `gh repo view iamawanishmaurya/testing01` all work here.

## Commands Run

```bash
gh auth status
gh repo view iamawanishmaurya/testing01 --json name,url,visibility,defaultBranchRef
git -C /home/astra/test status --short --branch
git -C /home/astra/test remote -v
git -C /home/astra/test ls-remote origin HEAD
getent hosts github.com
curl -I https://github.com/
curl --resolve github.com:443:140.82.112.4 -I https://github.com/
```
