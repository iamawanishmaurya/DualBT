# Embedded Scrcpy Repository Staged

- Timestamp: 2026-05-24 16:36:39 IST
- Exact warning:

```text
warning: adding embedded git repository: testing/scrcpy
hint: You've added another git repository inside your current repository.
hint: Clones of the outer repository will not contain the contents of
hint: the embedded repository and will not know how to obtain it.
hint: If you meant to add a submodule, use:
hint:
hint: 	git submodule add <url> testing/scrcpy
hint:
hint: If you added this path by mistake, you can remove it from the
hint: index with:
hint:
hint: 	git rm --cached testing/scrcpy
hint:
hint: See "git help submodule" for more information.
hint: Disable this message with "git config set advice.addEmbeddedRepo false"
```

- Reproduction steps:
  1. An unexpected untracked directory exists at `testing/scrcpy`.
  2. Run `git add -A` with the external Git metadata for the workspace.
  3. Git stages `testing/scrcpy` as an embedded repository gitlink and prints the warning above.
- Environment:
  - Workspace: `/home/astra/codex/DualBT`
  - Git metadata: `/tmp/DualBT.git`
  - Branch: `implementation/dualbt-v0.1.0`
- First hypothesis: `testing/scrcpy` is unrelated untracked local material and should not be committed as a submodule or gitlink for the DualBT app.

## Cleanup Attempt Error

```text
error: the following file has staged content different from both the
file and the HEAD:
    testing/scrcpy
(use -f to force removal)
```
