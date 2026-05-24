# Embedded Scrcpy Repository Staged Solution

- Problem: [2026-05-24-embedded-scrcpy-repo-staged.md](../problems/2026-05-24-embedded-scrcpy-repo-staged.md)
- What failed: `git add -A` staged the unexpected nested Git repository `testing/scrcpy` as a gitlink. A plain `git rm --cached testing/scrcpy` then failed because Git required forced cached removal for that staged gitlink state.
- What worked: `git rm --cached -f testing/scrcpy` removed only the accidental gitlink from the index while leaving the local `testing/scrcpy` directory untouched.
- Why it worked: The unwanted state was in Git's index, not a tracked DualBT source file. Removing it from the index avoids committing an unintended embedded repository without deleting local files.
- Commands run:

```bash
GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git add -A
GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git rm --cached testing/scrcpy
GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git rm --cached -f testing/scrcpy
```
