# Votis Wallet - Kotlin Multiplatform Mobile

A modern cryptocurrency wallet application built with Kotlin Multiplatform Mobile (KMP), supporting both Android and iOS platforms.

## 🏗️ Architecture

- **Shared Logic**: Kotlin Multiplatform code in `composeApp/` and `shared/`
- **Android App**: Native Android implementation in `androidApp/`
- **iOS App**: Native iOS implementation in `iosApp/`

## 🚀 Quick Start

### Prerequisites

- **Android Development**:
  - Android Studio Arctic Fox or newer
  - Android SDK 34+
  - Java 11+ (configured in `gradle.properties`)

- **iOS Development**:
  - Xcode 16.2+
  - CocoaPods installed
  - macOS required for iOS development

### Building the Project

#### Android
1. Open the project in Android Studio
2. Select the `androidApp` configuration
3. Build and run on Android device/emulator

#### iOS
1. **First Time Setup**:
   ```bash
   cd iosApp
   pod install
   ./sync-frameworks.sh
   ```

2. **Open in Xcode**:
   - Open `iosApp/iosApp.xcworkspace` (NOT .xcodeproj)
   - Select your target device/simulator
   - Build and run

3. **Building from Android Studio**:
   ```bash
   cd iosApp
   ./sync-frameworks.sh  # Run this if you get framework errors
   ```

## 📱 Features

- **Cross-Platform UI**: Compose Multiplatform for shared UI components
- **Google Sign-In**: Authentication across both platforms
- **Wallet Functions**: Cryptocurrency wallet features (implementation in progress)

## 🔧 iOS Build System

This project includes a custom framework synchronization system for iOS builds that works seamlessly between Xcode and Android Studio.

### Key Components:
- **`sync-frameworks.sh`**: Automatically syncs CocoaPods frameworks between build directories
- **Cross-IDE Compatibility**: Build from either Xcode or Android Studio without issues
- **Google Sign-In Integration**: Properly configured for iOS with all dependencies

### Troubleshooting iOS Builds:
See detailed troubleshooting guide: [`iosApp/iOS_BUILD_SETUP.md`](iosApp/iOS_BUILD_SETUP.md)

## 📝 Project Structure

```
mobile_votis_wallet/
├── androidApp/                 # Android-specific code
├── iosApp/                     # iOS-specific code
│   ├── sync-frameworks.sh      # iOS framework sync utility
│   └── iOS_BUILD_SETUP.md      # Detailed iOS build guide
├── composeApp/                 # Shared Compose UI code
│   ├── commonMain/             # Common code for all targets
│   ├── iosMain/                # iOS-specific Kotlin code
│   └── androidMain/            # Android-specific Kotlin code
├── shared/                     # Shared business logic
└── gradle.properties           # Project configuration
```

## 🛠️ Development Workflow

1. **Make changes** to shared code in `composeApp/` or `shared/`
2. **Test on Android** using Android Studio
3. **Test on iOS**:
   - If using Xcode: Build directly
   - If using Android Studio: Run `./iosApp/sync-frameworks.sh` first
4. **Commit changes** following conventional commit format

## 📚 Additional Documentation

- [iOS Build Setup Guide](iosApp/iOS_BUILD_SETUP.md) - Comprehensive iOS build instructions
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)

## 🤝 Contributing

1. Clone the repository
2. Follow the Quick Start guide above
3. Make your changes
4. Ensure both Android and iOS builds work
5. Submit a pull request

## ⚠️ Important Notes

- Always use `iosApp.xcworkspace` (not `.xcodeproj`) for iOS development
- Run the sync script (`./sync-frameworks.sh`) when switching between IDEs for iOS
- The project requires Java 11+ for proper Gradle compatibility
- CocoaPods frameworks are automatically managed by the sync script

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|---------|
| "Framework not found" on iOS | Run `./iosApp/sync-frameworks.sh` |
| "Multiple commands produce" | ✅ Fixed in project configuration |
| "Missing bundle ID" | ✅ Fixed in project configuration |
| Java version conflicts | ✅ Fixed in `gradle.properties` |

---

Built with ❤️ using Kotlin Multiplatform Mobile
