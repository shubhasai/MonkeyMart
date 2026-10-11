# Shared helpers for the planted-bug repro scripts. Source this file; needs adb on PATH.
PKG=com.shubhasai.monkeymart
ACT=$PKG/.MainActivity

dump() { adb exec-out uiautomator dump /dev/tty 2>/dev/null; }

# Centre of the first node whose resource-id matches $1.
center_of() {
  dump | tr '>' '\n' | grep "resource-id=\"$1\"" | head -1 \
    | sed -E 's/.*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]".*/\1 \2 \3 \4/' \
    | awk '{ printf "%d %d", ($1+$3)/2, ($2+$4)/2 }'
}

# Wait up to ${2:-10}s for resource-id $1 to appear, then tap it.
tap_id() {
  local id=$1 tries=$(( ${2:-10} * 2 )) xy
  while [ $tries -gt 0 ]; do
    xy=$(center_of "$id")
    if [ -n "$xy" ]; then adb shell input tap $xy; return 0; fi
    sleep 0.5; tries=$((tries - 1))
  done
  echo "!! $id not found" >&2; return 1
}

# Wait up to ${2:-10}s for resource-id $1 to be on screen.
wait_for() {
  local tries=$(( ${2:-10} * 2 ))
  while [ $tries -gt 0 ]; do
    [ -n "$(center_of "$1")" ] && return 0
    sleep 0.5; tries=$((tries - 1))
  done
  echo "!! $1 never appeared" >&2; return 1
}

# Tap a text field by resource-id and type into it (spaces become %s for adb).
type_into() { tap_id "$1" && adb shell input text "$(printf '%s' "$2" | sed 's/ /%s/g')"; }

field_text() {
  dump | tr '>' '\n' | grep "resource-id=\"$1\"" | head -1 | sed -E 's/.* text="([^"]*)".*/\1/'
}

current_rotation() {
  adb shell dumpsys window displays | grep -m1 -o 'mCurrentRotation=ROTATION_[0-9]*' | sed 's/.*ROTATION_//'
}

# Set the display rotation (0 = portrait, 1 = landscape) and wait until it has applied.
set_rotation() {
  local want=$1 degrees tries=20
  degrees=$(( want * 90 ))
  adb shell settings put system accelerometer_rotation 0
  adb shell settings put system user_rotation "$want"
  while [ $tries -gt 0 ]; do
    [ "$(current_rotation)" = "$degrees" ] && return 0
    sleep 0.25; tries=$((tries - 1))
  done
  echo "!! display never reached rotation $degrees" >&2; return 1
}

fresh_start() {
  set_rotation 0
  adb shell svc wifi enable; adb shell svc data enable
  adb shell am force-stop $PKG
  adb logcat -b all -c
  adb shell am start -W -n $ACT >/dev/null
  set_rotation 0   # Android 16 may restore the app's last orientation on launch; force portrait
}

crashed() { adb logcat -d -b crash | grep -q -E "FATAL EXCEPTION|$PKG"; }

report() {
  if crashed; then
    echo "RESULT: CRASHED"
    adb logcat -d -b crash | grep -E "Exception|Error|at com\.shubhasai" | head -8
  else
    echo "RESULT: no crash"
  fi
}
