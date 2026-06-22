#!/bin/bash

set -e

REMOTE_HOST="dante.local"
REMOTE_PATH="~/Projects/lift-bro"
DEVICE_UDID="00008110-000E4C913EFB801E"

echo "=== Deploying Lift Bro to iOS device ==="
echo "Remote: $REMOTE_HOST"
echo "Device UDID: $DEVICE_UDID"
echo ""

echo "[1/2] Syncing project to $REMOTE_HOST..."
rsync -avz \
  --exclude '.git' \
  --exclude '.idea/' \
  --exclude '.gradle/' \https://www.youtube.com/watch?v=ujx8WcbTRRw&pp=ugUEEgJlbg%3D%3D
  --exclude 'build/' \
  --exclude '*.iml' \
  --exclude 'DerivedData/' \
  --exclude 'iosApp/build/' \
  --exclude 'iosApp/iosApp/GoogleService-Info.plist' \
  --exclude 'iosApp/Configuration.storekit' \
  --exclude 'iosApp/StoreKitTestCertificate.cer' \
  . "eholtrop@$REMOTE_HOST:$REMOTE_PATH/"

echo ""
echo "[2/2] Building iOS app on remote..."

# Fastlane
ssh "eholtrop@$REMOTE_HOST" "
   cd $REMOTE_PATH &&
   ~/.rbenv/shims/bundle exec fastlane ios build_debug_device device_udid:$DEVICE_UDID
"

echo ""
echo "=== Build complete ==="
