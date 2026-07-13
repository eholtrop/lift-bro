#!/bin/bash
# push-test-backup.sh
# Push test backup file to device/emulator for screenshot generation

set -e

PLATFORM=$1

# Resolve backup file relative to project root (parent of scripts/)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_FILE="$PROJECT_ROOT/build/generated/test-data/screenshot_test_backup.json"

if [ -z "$PLATFORM" ]; then
  echo "Usage: $0 <android|ios>"
  echo ""
  echo "Pushes test backup file to device/emulator for screenshot generation."
  exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Error: Backup file not found at $BACKUP_FILE"
  exit 1
fi

case $PLATFORM in
  android)
    echo "📱 Pushing backup to Android device/emulator..."
    
    # Check if adb is available
    if ! command -v adb &> /dev/null; then
      echo "Error: adb not found. Please install Android SDK platform-tools."
      exit 1
    fi
    
    # Wait for device
    echo "Waiting for device..."
    adb wait-for-device
    
    # Ensure Download directory exists
    adb shell mkdir -p /sdcard/Download
    
    # Push backup file to Downloads folder
    adb push "$BACKUP_FILE" /sdcard/Download/
    
    echo "✅ Backup pushed to /sdcard/Download/"
    echo "   File: screenshot_test_backup.json"
    ;;
    
  ios)
    echo "📱 Pushing backup to iOS simulator..."
    
    # Check if xcrun is available
    if ! command -v xcrun &> /dev/null; then
      echo "Error: xcrun not found. Please install Xcode."
      exit 1
    fi
    
    # Get booted simulator UDID
    SIMULATOR_ID=$(xcrun simctl list devices booted | grep -o '[A-F0-9-]\{36\}' | head -1)
    
    if [ -z "$SIMULATOR_ID" ]; then
      echo "Error: No iOS simulator is booted."
      echo "Start a simulator first: open -a Simulator"
      exit 1
    fi
    
    echo "Found simulator: $SIMULATOR_ID"
    
    # Get app container path
    CONTAINER_PATH=$(xcrun simctl get_app_container "$SIMULATOR_ID" com.lift.bro data 2>/dev/null || true)
    
    if [ -z "$CONTAINER_PATH" ]; then
      echo "App not installed on simulator. Creating Documents directory in simulator..."
      
      # Create a temporary app container path for the backup
      # The backup file will be accessible via the file picker
      TEMP_DIR=$(mktemp -d)
      cp "$BACKUP_FILE" "$TEMP_DIR/"
      
      echo "✅ Backup file prepared at: $TEMP_DIR/screenshot_test_backup.json"
      echo "   Note: You may need to manually copy this file to the simulator's Documents directory."
      echo "   Or use: xcrun simctl addmedia $SIMULATOR_ID $TEMP_DIR/screenshot_test_backup.json"
    else
      # Copy backup file to Documents directory
      mkdir -p "$CONTAINER_PATH/Documents" 2>/dev/null || true
      cp "$BACKUP_FILE" "$CONTAINER_PATH/Documents/"
      
      echo "✅ Backup pushed to simulator container"
      echo "   Path: $CONTAINER_PATH/Documents/screenshot_test_backup.json"
    fi
    ;;
    
  *)
    echo "Error: Unknown platform '$PLATFORM'"
    echo "Usage: $0 <android|ios>"
    exit 1
    ;;
esac

echo ""
echo "🎯 Next steps:"
echo "   1. Run Maestro screenshot flows:"
echo "      maestro test .maestro/screenshots/"
echo "   2. Or use Fastlane:"
echo "      fastlane android generate_play_store_screenshots"
