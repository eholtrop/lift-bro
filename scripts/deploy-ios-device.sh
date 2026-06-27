#!/bin/bash

set -e

REMOTE_HOST="dante.local"
REMOTE_PATH="~/Projects/lift-bro"
DEVICE_UDID="00008110-000E4C913EFB801E"

echo "=== Deploying Lift Bro to iOS device ==="
echo "Remote: $REMOTE_HOST"
echo "Device UDID: $DEVICE_UDID"
echo ""

# Pre-warm Gradle daemon on remote while we sync files
echo "[1/3] Pre-warming Gradle daemon on $REMOTE_HOST..."
ssh "eholtrop@$REMOTE_HOST" \
  "cd $REMOTE_PATH && ./gradlew --stop > /dev/null 2>&1; ./gradlew --version > /dev/null 2>&1 &"

echo "[2/3] Syncing project to $REMOTE_HOST..."
rsync -a --info=progress2 \
  --delete \
  --exclude '.git' \
  --exclude '.idea/' \
  --exclude '.gradle/' \
  --exclude 'build/' \
  --exclude 'fastlane/build/' \
  --exclude '*.iml' \
  --exclude 'DerivedData/' \
  --exclude 'iosApp/build/' \
  --exclude 'node_modules/' \
  --exclude 'marketing-website/' \
  --exclude '.maestro/' \
  --exclude 'maestro_tests/' \
  --exclude 'hooks/' \
  --exclude 'iosApp/iosApp/GoogleService-Info.plist' \
  --exclude 'iosApp/Configuration.storekit' \
  --exclude 'iosApp/StoreKitTestCertificate.cer' \
  --exclude 'local.properties' \
  --exclude 'xcuserdata/' \
  . "eholtrop@$REMOTE_HOST:$REMOTE_PATH/"

echo ""
echo "[3/3] Building iOS app on remote..."

ssh "eholtrop@$REMOTE_HOST" "
   cd $REMOTE_PATH &&
   ./gradlew --stop > /dev/null 2>&1 &&
   ~/.rbenv/shims/bundle exec fastlane ios build_debug_device device_udid:$DEVICE_UDID
"

echo ""
echo "=== Build complete ==="
