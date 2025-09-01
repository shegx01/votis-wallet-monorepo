#!/bin/bash

# Automated build and framework sync script for iOS development
# This script builds the project and automatically syncs frameworks between Xcode and Android Studio

set -e

echo "🚀 Starting automated iOS build and framework sync..."
echo

# Change to iOS project directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Step 1: Clean and build the iOS project
echo "📱 Building iOS project..."
xcodebuild -workspace iosApp.xcworkspace \
           -scheme iosApp \
           -configuration Debug \
           -sdk iphonesimulator \
           -destination 'platform=iOS Simulator,name=iPhone 16,OS=latest' \
           build

if [ $? -eq 0 ]; then
    echo "✅ iOS build completed successfully"
else
    echo "❌ iOS build failed"
    exit 1
fi

echo

# Step 2: Run framework sync
echo "🔄 Syncing frameworks between Xcode and Android Studio..."
./sync-frameworks.sh

if [ $? -eq 0 ]; then
    echo "✅ Framework sync completed successfully"
else
    echo "❌ Framework sync failed"
    exit 1
fi

echo
echo "🎉 Build and sync process completed successfully!"
echo "   You can now develop in both Xcode and Android Studio."
