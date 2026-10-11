#!/usr/bin/env bash
# Runs every planted-bug repro once and prints one line per bug.
cd "$(dirname "$0")"
for bug in rotation process_death network_loss permission_revoke; do
  out=$(./$bug.sh 2>&1)
  if   grep -q "RESULT: CRASHED" <<<"$out"; then verdict="crashed: $(grep -m1 -o -E '[a-zA-Z.]+(Exception|Error)[^:]*' <<<"$out")"
  elif grep -q "^-- after" <<<"$out" && grep -A6 "^-- after" <<<"$out" | grep -q -E "^field_name=$"; then verdict="lost state: checkout form empty after relaunch"
  else verdict="survived"; fi
  printf '%-18s %s\n' "$bug" "$verdict"
done
