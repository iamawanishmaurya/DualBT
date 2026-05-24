# Changelog

All notable changes to DualBT will be documented in this file.

## [0.2.21] - 2026-05-24

### Fixed
- Blocked unsupported dual-speaker route sets in the activity before MediaProjection so the UI no longer stays in a false streaming state after the service rejects one exposed Bluetooth media route.
- Added a user-visible route availability status when Android exposes fewer than two direct speaker routes.

## [0.2.20] - 2026-05-24

### Fixed
- Added a calibration run gate so rapid `Test 1`/`Test 2` taps cannot release an active calibration `AudioTrack` while it is still writing PCM.

## [0.2.19] - 2026-05-24

### Fixed
- Disabled the generic Bluetooth SCO fallback after physical testing showed it could make `Test 1` and `Test 2` beep the same Mini Boost speaker.
- Added direct-route validation so streaming starts only when Android exposes two distinct non-SCO media output routes for the selected speakers.

### Added
- Added plain Java route-support tests for rejecting one A2DP route plus a generic SCO fallback.

## [0.2.18] - 2026-05-24

### Added
- Added system media-volume syncing in the foreground service so hardware volume keys can control DualBT output gain while YouTube remains the foreground source app.
- Added a plain Java `OutputVolumeMapperTest` for Android media-volume to DualBT gain mapping.

## [0.2.17] - 2026-05-24

### Fixed
- Fixed volume controls after activity recreation by always forwarding foreground UI volume changes to the running service and making volume-only service starts stop cleanly when no stream is active.

## [0.2.16] - 2026-05-24

### Added
- Added foreground-service notification `Vol -` and `Vol +` actions so DualBT output gain can be adjusted while YouTube remains the foreground source app.

## [0.2.15] - 2026-05-24

### Added
- Added visible DualBT output volume controls that scale captured PCM before writing to the selected speaker routes.
- Added a tested PCM gain scaler with 0-200% gain and signed 16-bit clipping.
- Moved `Test 1` and `Test 2` controls into a stable top-level calibration panel for safer physical-device testing.

## [0.2.14] - 2026-05-24

### Fixed
- Switched speaker calibration playback from static-buffer `AudioTrack` output to streamed min-buffer playback after physical testing showed the static calibration track failed to initialize on the Redmi Note 9 Pro.

## [0.2.13] - 2026-05-24

### Added
- Added per-speaker `Test 1` and `Test 2` calibration controls for selected devices so identical Mini Boost speakers can be identified before dual playback testing.
- Added a routed Android calibration tone player that uses the same A2DP and communication-SCO fallback strategy as the streaming output path.
- Added a plain Java calibration tone generator test.

## [0.2.12] - 2026-05-24

### Added
- Added duplicate Bluetooth speaker aliases so the two physical `Mini boost 4` speakers display as `Mini boost 1` and `Mini boost 2` while preserving their original addresses.
- Added an experimental A2DP plus communication-SCO fallback for devices that expose one Bluetooth media sink but a separate Bluetooth communication route.
- Added tests for duplicate Mini Boost aliases and output fallback planning.

### Changed
- Ignored the local `music.webm` test asset so user-provided media is not staged by `git add -A`.

## [0.2.11] - 2026-05-24

### Added
- Added a verified Java `AudioTrack` output router for the default APK path that creates two output tracks, prefers matching Bluetooth output devices, writes split PCM to both routes, and logs output routing/write evidence.
- Added route-matching tests for two same-name `Mini boost 4` speakers with different Bluetooth addresses.

### Changed
- Playback capture now excludes DualBT's own UID to avoid recapturing the app's output track audio.
- Renamed the stream button from `Start Mock Stream` to `Start Stream` in the verified Java UI.

## [0.2.10] - 2026-05-24

### Added
- Added a safe UI dump wrapper for physical-device validation that treats known MIUI `uiautomator` theme-config noise as non-blocking only after validating XML hierarchy output.
- Added physical-device validation documentation for app install, scrcpy mirroring/control, logging overlay, dark/light theme checks, MediaProjection consent, and playback-capture start/stop.

### Fixed
- Fixed a physical-device launch crash by deferring light system-bar appearance updates until after the activity content root exists.

## [0.2.9] - 2026-05-24

### Added
- Added a tested Codex watchdog polling utility that records one-minute heartbeat lines for long-running work.
- Added a watchdog controller for starting, checking, tailing, and stopping the detached heartbeat process.
- Added watchdog documentation and ignored the generated heartbeat log file.

## [0.2.8] - 2026-05-24

### Added
- Added a tested Java audio capture specification for the default service path.
- Added a Java AudioPlaybackCapture engine that starts `AudioRecord` from MediaProjection consent, reads PCM, and fans out captured bytes to two output buffers.

### Changed
- `DualBTService` now starts real playback capture after validating MediaProjection consent and exactly two selected route targets, and stops capture during service teardown.

## [0.2.7] - 2026-05-24

### Added
- Added a tested stream route plan serializer for selected speaker targets.

### Changed
- `MainActivity` now passes the selected two-speaker route plan into `DualBTService` after capture consent.
- `DualBTService` now rejects capture starts that do not include exactly two route targets.

## [0.2.6] - 2026-05-24

### Added
- Added bonded Bluetooth audio-device discovery for the default Java build path.
- Added a tested Bluetooth speaker catalog with audio filtering, de-duplication, and mock fallback.

### Changed
- Device refresh now scans real bonded Bluetooth audio devices when permissions and adapter state allow it, then falls back to emulator mock speakers.

## [0.2.5] - 2026-05-24

### Added
- Added a tested Java PCM splitter for the default offline build path.
- Added a repository-local plain Java PCM splitter test.

## [0.2.4] - 2026-05-24

### Added
- Added persistent internal file logging with append, newline escaping, rotation, and clear support.
- Added a repository-local plain Java file-log sink test.

### Changed
- `AppLogger` now initializes a file sink from `DualBTApp` and persists each logcat/in-app log entry to app internal storage.

## [0.2.3] - 2026-05-24

### Added
- Added a repository-local plain Java stream-session controller test.
- Added MediaProjection permission gating before mock streaming starts.

### Changed
- Streaming now enters a pending permission state, starts the foreground service only after capture consent, and stops the service when streaming stops.
- `DualBTService` now requires MediaProjection consent extras before entering foreground mode.

## [0.2.2] - 2026-05-24

### Added
- Added a tested stream session controller for the default Java build path.

### Changed
- Mock streaming now requires exactly two selected speakers, rejects a third selection, locks selection while streaming, and logs blocked start/selection attempts.

## [0.2.1] - 2026-05-24

### Added
- Added a conditional `DUALBT_PLANNED_STACK=true` Gradle path for the original Kotlin, Compose, Hilt, and NDK architecture.

### Changed
- Kept the verified Java framework APK path as the default offline build while excluding fallback Java classes when the planned stack is enabled.

## [0.2.0] - 2026-05-24

### Added
- Added a platform-only Android implementation that can build from the available offline cache.
- Added macOS-inspired dark and light glassmorphism UI, animated background orbs, mock speaker cards, streaming state animations, and responsive scroll layout.
- Added a full in-app and logcat logging system with filtering and clearing.

### Changed
- Replaced the dependency-heavy Compose/Hilt/Oboe build path with Android framework APIs so the app can be compiled and installed in the restricted environment.

## [0.1.0] - 2026-05-24

### Added
- Initialized local release tracking and repository hygiene.
- Added implementation step logging under `docs/steps.md`.
- Added problem and solution documentation for workspace setup issues.

### Notes
- The workspace contains a read-only `.git` placeholder, so Git commands for this session use `GIT_DIR=/tmp/DualBT.git` and `GIT_WORK_TREE=/home/astra/codex/DualBT`.
- No non-English project content was found during initial setup review.
