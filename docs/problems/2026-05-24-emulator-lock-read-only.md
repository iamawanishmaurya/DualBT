# Emulator Lock Read-Only

- Timestamp: 2026-05-24 09:05:05 IST
- Environment: `/home/astra/codex/DualBT`, AVD path `/home/astra/.android/avd/Medium_Phone.avd`.

## Exact Error

```text
rm: cannot remove '/home/astra/.android/avd/Medium_Phone.avd/multiinstance.lock': Read-only file system
```

## Reproduction Steps

1. Run `find /home/astra/.android/avd/Medium_Phone.avd -name '*.lock' -o -name '*lock*'`.
2. Confirm `/home/astra/.android/avd/Medium_Phone.avd/multiinstance.lock` exists.
3. Run `rm /home/astra/.android/avd/Medium_Phone.avd/multiinstance.lock`.
4. Observe removal fails because the AVD path is read-only.

## First Hypothesis

The existing AVD state cannot be repaired from this sandbox. Use another AVD or create a writable AVD under `/tmp`.

