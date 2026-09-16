#!/usr/bin/env bash
# Generates the ECDSA P-256 key pair used to sign remote patches.
# Keep private.pem OUT of the repo (e.g. next to keystore/). Paste the printed
# public key into PUBLIC_KEY_BASE64 in update/RemotePatchManager.kt.
set -euo pipefail
out="${1:-keystore/remote-patch}"
mkdir -p "$out"
openssl ecparam -name prime256v1 -genkey -noout -out "$out/private.pem"
openssl ec -in "$out/private.pem" -pubout -outform DER 2>/dev/null | base64 -w0
echo
