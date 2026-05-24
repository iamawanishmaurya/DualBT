# Source Root Search Path Missing

- Problem: [2026-05-24-source-root-search-path-missing.md](../problems/2026-05-24-source-root-search-path-missing.md)
- Timestamp: 2026-05-24 08:07:00 IST

## What Failed

The command `rg --files app src .` included a repository-level `src` path that does not exist in this Android project.

## What Worked

Use `app/src` or repository-root searches instead of `src`.

## Why It Worked

Android application sources are stored under `app/src/main/...`, and the project has no top-level source directory.

## Commands Run

```bash
rg --files app src .
find app -maxdepth 4 -type f
```

