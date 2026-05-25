# Physical Test Tap Mistarget

- Timestamp: 2026-05-24 23:43 IST
- Step: Human-in-the-loop Mini Boost physical verification.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.21`, versionCode `22`

## Exact Error

The UI was scrolled so the intended `Test 1` tap coordinates hit the device list instead. Log output showed the wrong action:

```text
I/DualBT:MainViewModel: Device selected: Rockerz 110
```

## Reproduction Steps

1. Leave the DualBT device list scrolled.
2. Tap fixed coordinates intended for the calibration row.
3. Observe that the tap selects a visible device row instead of pressing `Test 1`.

## First Hypothesis

The test procedure used fixed coordinates without first confirming the current UI bounds. Reset the activity, reselect only `Mini boost 1` and `Mini boost 2`, dump the UI hierarchy, then tap the actual current `Test 1`/`Test 2` bounds.
