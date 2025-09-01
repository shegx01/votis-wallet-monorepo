#!/bin/bash

# Sync CocoaPods frameworks for Android Studio compatibility
# This script ensures that CocoaPods frameworks are available in both 
# standard Xcode and Android Studio DerivedData locations

set -e

echo "Syncing CocoaPods frameworks for cross-IDE compatibility..."

# Get the current build configuration and platform
CONFIGURATION=${CONFIGURATION:-Debug}
PLATFORM_NAME=${PLATFORM_NAME:-iphonesimulator}
EFFECTIVE_PLATFORM_NAME=${EFFECTIVE_PLATFORM_NAME:--iphonesimulator}

# Standard Xcode build directory
XCODE_BUILD_DIR="/Users/$USER/Library/Developer/Xcode/DerivedData"

# Android Studio cache directory pattern
AS_BUILD_DIR="/Users/$USER/Library/Caches/Google/AndroidStudio*/DerivedData"

# Framework names to sync
FRAMEWORKS=("AppAuth" "GTMAppAuth" "GTMSessionFetcher" "GoogleSignIn" "lottie-ios" "Pods_iosApp.framework")

# Find the actual Xcode derived data directory for this project
XCODE_PROJECT_DIR=$(find "$XCODE_BUILD_DIR" -name "iosApp-*" -type d 2>/dev/null | head -1)

if [ -z "$XCODE_PROJECT_DIR" ]; then
    echo "Warning: Standard Xcode build directory not found. Frameworks may be built in Android Studio location."
    exit 0
fi

XCODE_FRAMEWORKS_DIR="$XCODE_PROJECT_DIR/Build/Products/$CONFIGURATION$EFFECTIVE_PLATFORM_NAME"

# Find Android Studio derived data directories and sync both ways
for AS_DIR in $(find "/Users/$USER/Library/Caches/Google" -name "AndroidStudio*" -type d 2>/dev/null); do
    AS_PROJECT_DIR=$(find "$AS_DIR/DerivedData" -name "iosApp-*" -type d 2>/dev/null | head -1)
    
    if [ -n "$AS_PROJECT_DIR" ]; then
        AS_FRAMEWORKS_DIR="$AS_PROJECT_DIR/Build/Products/$CONFIGURATION$EFFECTIVE_PLATFORM_NAME"
        
        # Create directory if it doesn't exist
        mkdir -p "$AS_FRAMEWORKS_DIR"
        
        # Sync frameworks from Xcode to Android Studio
        for FRAMEWORK in "${FRAMEWORKS[@]}"; do
            XCODE_SOURCE="$XCODE_FRAMEWORKS_DIR/$FRAMEWORK"
            AS_TARGET="$AS_FRAMEWORKS_DIR/$FRAMEWORK"
            AS_SOURCE="$AS_FRAMEWORKS_DIR/$FRAMEWORK"
            XCODE_TARGET="$XCODE_FRAMEWORKS_DIR/$FRAMEWORK"
            
            # Link from Xcode to Android Studio if Xcode version exists
            if [ -d "$XCODE_SOURCE" ] && [ ! -e "$AS_TARGET" ]; then
                echo "Linking $FRAMEWORK framework from Xcode to Android Studio..."
                ln -sf "$XCODE_SOURCE" "$AS_TARGET"
            # Link from Android Studio to Xcode if Android Studio version exists
            elif [ -d "$AS_SOURCE" ] && [ ! -e "$XCODE_TARGET" ]; then
                echo "Linking $FRAMEWORK framework from Android Studio to Xcode..."
                mkdir -p "$(dirname "$XCODE_TARGET")"
                ln -sf "$AS_SOURCE" "$XCODE_TARGET"
            fi
        done
    fi
done

echo "Framework sync completed."
