# Changelog

All notable changes to DualBT will be documented in this file.

## [0.2.9] - 2026-05-24

### Added
- Added a tested Codex watchdog polling utility that records one-minute heartbeat lines for long-running work.
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
