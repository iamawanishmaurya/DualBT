# Android Toybox Find Does Not Support GNU Printf

## Exact Error

```text
find: bad arg '%sn'
```

## Reproduction Steps

1. Run a device-side file listing command through `adb shell run-as com.xpwnit.dualbt`.
2. Use GNU-style `find -printf '%p %s\n'` against the app files directory.
3. Observe Android toybox `find` reject the `-printf` format argument.

Command:

```bash
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt find files -maxdepth 3 -type f -printf '%p %s\n'
```

## Environment

- Host: `/home/astra/codex/DualBT`
- Device: Redmi Note 9 Pro (`d1bc5c4a`)
- Android: API 31 / MIUI
- Package: `com.xpwnit.dualbt`

## First Hypothesis

Android's bundled toybox `find` does not implement GNU `find -printf`. Use simpler portable device commands such as `ls`, `cat`, or `find ... -exec ls -l {}` where supported.

