# Classic A2DP Playback Reaches One Mini Boost Speaker Only

## Exact Error

The user reports the test song plays from only one physical speaker. Runtime diagnostics show both `Mini boost 4` devices are connected at the Bluetooth profile level, but Android's audio service exposes one active A2DP media sink and routes both DualBT `AudioTrack` players to the same device id.

Current evidence:

```text
BluetoothManager:
41:42:26:B3:62:1C [BR/EDR] Mini boost 4
41:42:2E:9E:5E:AE [BR/EDR] Mini boost 4
mActiveDevice: 41:42:2E:9E:5E:AE

AudioService:
AudioPlaybackConfiguration ... deviceId:7083 type:android.media.AudioTrack u/pid:10531/25241 state:started
AudioPlaybackConfiguration ... deviceId:7083 type:android.media.AudioTrack u/pid:10531/25241 state:started
Connected devices:
  [DeviceInfo: type:0x80 (bt_a2dp) name:Mini boost 4 addr:41:42:2E:9E:5E:AE codec: 1f000000]
APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:2E:9E:5E:AE
```

## Reproduction Steps

1. Connect both physical `Mini boost 4` speakers.
2. Start DualBT streaming to both selected speakers.
3. Play a test song.
4. Listen physically: audio is heard from one speaker only.
5. Inspect `dumpsys audio` and observe both app `AudioTrack` players routed to the same device id.

## Environment

- Host path: `/home/astra/codex/DualBT`
- Device: Redmi Note 9 Pro (`d1bc5c4a`)
- Android: API 31 / MIUI
- App package: `com.xpwnit.dualbt`
- Current app release: `0.2.11`
- Test media: `/home/astra/codex/DualBT/music.webm`
- Speakers:
  - `41:42:26:B3:62:1C` (`Mini boost 4`)
  - `41:42:2E:9E:5E:AE` (`Mini boost 4`)

## First Hypothesis

DualBT's app-side two-track output path is active, but classic Bluetooth A2DP media routing on this Redmi/MIUI build exposes a single media output sink to third-party apps. A normal app can label and select both speakers, but cannot force simultaneous classic A2DP playback to two independent sinks unless the platform exposes multiple active media routes, the vendor supports a dual-audio feature, the speakers form a hardware/TWS pair, or the app uses a non-Bluetooth-classic relay path.

