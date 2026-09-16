#!/usr/bin/env bash
# Signs a payload JSON and writes the envelope the app downloads.
# Usage: scripts/remote-patch/sign.sh payload.json keystore/remote-patch/private.pem remote-patch/patch.json
set -euo pipefail
payload="$1"; key="$2"; out="$3"
python3 -c 'import json,sys; json.load(open(sys.argv[1]))' "$payload"  # fail fast on bad JSON
sig="$(openssl dgst -sha256 -sign "$key" "$payload" | base64 -w0)"
mkdir -p "$(dirname "$out")"
printf '{\n  "payload": "%s",\n  "signature": "%s"\n}\n' "$(base64 -w0 < "$payload")" "$sig" > "$out"
echo "Wrote $out"
