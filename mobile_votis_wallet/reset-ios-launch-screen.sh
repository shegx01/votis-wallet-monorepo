#!/bin/bash

# iOS Launch Screen Cache Reset Script
# This script aggressively clears all iOS launch screen caches

echo "🧹 Resetting iOS Launch Screen Cache..."

# Step 1: Close Xcode and Simulator
echo "1️⃣  Closing Xcode and Simulator..."
killall "Xcode" 2>/dev/null
killall "Simulator" 2>/dev/null
sleep 2

# Step 2: Clear Xcode DerivedData
echo "2️⃣  Clearing Xcode DerivedData..."
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# Step 3: Clear iOS Simulator data
echo "3️⃣  Clearing iOS Simulator data..."
xcrun simctl shutdown all
xcrun simctl erase all

# Step 4: Clear iOS Device Support cache
echo "4️⃣  Clearing iOS Device Support cache..."
rm -rf ~/Library/Developer/Xcode/iOS\ DeviceSupport/*

# Step 5: Clear CocoaPods cache 
echo "5️⃣  Clearing CocoaPods cache..."
cd iosApp
pod cache clean --all
rm -rf Pods/
rm -f Podfile.lock
cd ..

# Step 6: Clean build directories
echo "6️⃣  Cleaning build directories..."
rm -rf iosApp/build/
rm -rf composeApp/build/

# Step 7: Reinstall pods
echo "7️⃣  Reinstalling CocoaPods..."
cd iosApp
pod install
cd ..

echo "✅ Cache reset complete!"
echo ""
echo "📱 Next steps:"
echo "   1. Open iosApp.xcworkspace in Xcode"
echo "   2. Product → Clean Build Folder (⌘⇧K)"
echo "   3. Build and Run"
echo "   4. In Simulator: Device → Appearance → Dark"
echo "   5. Force quit app and relaunch"
echo ""
echo "🚨 If dark mode still doesn't work:"
echo "   - Try on a physical device"
echo "   - Check that device is actually in dark mode"
echo "   - Verify app supports dark mode in Settings → Developer"
