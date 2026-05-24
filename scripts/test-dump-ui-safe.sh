#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d /tmp/dualbt-dump-ui-safe-test.XXXXXX)"
trap 'rm -rf "$TMP_DIR"' EXIT

FAKE_BIN="$TMP_DIR/bin"
mkdir -p "$FAKE_BIN"

cat > "$FAKE_BIN/adb" <<'ADB'
#!/usr/bin/env bash
set -euo pipefail

if [[ "$1" != "-s" || "$2" != "test-device" ]]; then
  echo "unexpected adb serial" >&2
  exit 2
fi
shift 2

case "$1" in
  shell)
    shift
    if [[ "$1" == "uiautomator" && "$2" == "dump" ]]; then
      echo "java.io.FileNotFoundException: /data/system/theme_config/theme_compatibility.xml" >&2
      echo "UI hierchary dumped to: $3"
      exit 0
    fi
    ;;
  pull)
    local_xml="$3"
    printf "%s\n" "<?xml version='1.0' encoding='UTF-8'?><hierarchy><node text='DualBT'/></hierarchy>" > "$local_xml"
    exit 0
    ;;
esac

echo "unexpected adb command: $*" >&2
exit 2
ADB
chmod +x "$FAKE_BIN/adb"

OUT_XML="$TMP_DIR/ui.xml"
ERR_LOG="$TMP_DIR/ui.err"

PATH="$FAKE_BIN:$PATH" "$ROOT_DIR/scripts/dump-ui-safe.sh" test-device "$OUT_XML" 2> "$ERR_LOG"

if ! grep -q "DualBT" "$OUT_XML"; then
  echo "expected pulled XML to contain DualBT" >&2
  exit 1
fi

if ! grep -q "theme_compatibility.xml" "$ERR_LOG"; then
  echo "expected MIUI warning to be preserved in stderr log" >&2
  exit 1
fi
