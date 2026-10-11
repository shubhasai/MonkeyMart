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

# Launch exactly like a launcher icon tap (MAIN/LAUNCHER, NEW_TASK|RESET_TASK_IF_NEEDED), so a
# later relaunch brings back the same task and its saved state, as returning from recents does.
launch() {
  adb shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER \
    -f 0x10200000 -n $ACT >/dev/null
}

fresh_start() {
  set_rotation 0
  adb shell svc wifi enable; adb shell svc data enable
  adb shell am force-stop $PKG
  adb logcat -b all -c
  launch
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

# Shop -> "Mango Box" detail -> dismiss the deal popup -> checkout.
goto_checkout() {
  tap_id product_3 10 || return 1
  tap_id android:id/button1 10 || return 1   # "Nice" on the deal popup
  tap_id buy_now 5 || return 1
  wait_for field_name 5
}

# Fill the six checkout fields, moving between them with Tab so the keyboard never hides a field.
fill_checkout() {
  tap_id field_name 5 || return 1
  local first=1 v
  for v in "Ravi Kumar" "ravi@example.com" "9876543210" "12 MG Road" "Bengaluru" "560001"; do
    [ $first = 1 ] || { adb shell input keyevent 61; sleep 0.4; }   # KEYCODE_TAB
    first=0
    adb shell input text "$(printf '%s' "$v" | sed 's/ /%s/g')"
  done
  adb shell input keyevent 111   # KEYCODE_ESCAPE hides the keyboard
}

# Print "<id>=<text>" for each checkout field.
checkout_values() {
  local d id
  d=$(dump | tr '>' '\n')
  for id in field_name field_email field_phone field_address field_city field_pincode; do
    printf '%s=%s\n' "$id" "$(printf '%s\n' "$d" | grep -A3 "resource-id=\"$id\"" | grep -m1 -o 'text="[^"]*"' | sed 's/text="//; s/"$//')"
  done
}

relaunch_from_launcher() { launch; }

# Send the app to the background and kill its process the way low-memory killing does.
# Fails loudly if the process is still alive, so a "survived" result is never a no-op kill.
kill_in_background() {
  local tries=10
  adb shell input keyevent 3   # HOME
  while [ $tries -gt 0 ]; do
    sleep 1
    adb shell am kill $PKG
    [ -z "$(adb shell pidof $PKG)" ] && return 0
    tries=$((tries - 1))
  done
  echo "!! process still alive after am kill" >&2; return 1
}
