# Splash Screen Implementation

This document describes the custom splash screen implementation for the Votis Wallet KMP app, supporting both Android and iOS platforms with system theme awareness.

## Overview

The splash screen displays the VOTIS logo centered on a background that adapts to the system theme (light/dark mode) on both platforms.

## Android Implementation

### Dependencies
- `androidx.core:core-splashscreen:1.0.1` - Modern splash screen API with backward compatibility

### Key Components

1. **Theme Configuration** (`composeApp/src/androidMain/res/values/themes.xml`):
   - `Theme.VotisWallet.SplashScreen` - Splash screen theme with custom logo and timing
   - `Theme.VotisWallet` - Main app theme (AppCompat for compatibility)

2. **Theme-aware Colors**:
   - Light theme: `splash_background=#FFFFFF`, `splash_icon_tint=#000000`
   - Dark theme: `splash_background=#1A1A1A`, `splash_icon_tint=#FFFFFF`

3. **Assets**:
   - Multiple density splash logo images (`drawable-mdpi` through `drawable-xxxhdpi`)
   - Sizes: 108dp (mdpi) to 432dp (xxxhdpi)

4. **MainActivity Integration**:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       installSplashScreen()
       // ... rest of onCreate
   }
   ```

### Configuration
- Splash duration: 500ms
- Logo size: Default (adaptive based on screen)
- Background: System theme-aware

## iOS Implementation

### Key Components

1. **LaunchScreen.storyboard**:
   - Centered VOTIS logo with Auto Layout constraints
   - Theme-aware background color from asset catalog

2. **Asset Catalog Assets**:
   - `LaunchImage.imageset` - Logo images (1x, 2x, 3x scales)
   - `SplashBackgroundColor.colorset` - Theme-aware background color
   - Light theme: White (#FFFFFF)
   - Dark theme: Dark gray (#1A1A1A)

3. **Info.plist Configuration**:
   - `UILaunchStoryboardName` set to "LaunchScreen"

### Theme Support
iOS automatically switches between light and dark assets based on `UIUserInterfaceStyle` system preference.

## File Structure

```
Android:
├── composeApp/src/androidMain/res/
│   ├── drawable-{density}/splash_logo.png
│   ├── values/splash_colors.xml
│   ├── values-night/splash_colors.xml
│   └── values/themes.xml

iOS:
├── iosApp/iosApp/
│   ├── LaunchScreen.storyboard
│   └── Assets.xcassets/
│       ├── LaunchImage.imageset/
│       └── SplashBackgroundColor.colorset/
```

## Asset Requirements

- **Android**: PNG images at 5 densities (mdpi through xxxhdpi)
- **iOS**: PNG images at 3 scales (1x, 2x, 3x)
- **Source**: 270x270px PNG logo
- **Background**: Transparent (theme-aware background applied via code/storyboard)

## Testing

1. **Android**: Build and run on device/emulator
2. **iOS**: Build through Xcode and run on device/simulator
3. **Theme Testing**: Test both light and dark system themes
4. **Screen Sizes**: Verify on different device sizes and orientations

## Maintenance

To update the splash screen logo:
1. Replace source image in `/tmp/splash_assets/`
2. Run the asset generation commands for each platform
3. Replace the generated assets in the respective directories
4. Test on both platforms and themes

## Technical Notes

- Android uses the modern SplashScreen API (API 12+) with compat library for backward compatibility
- iOS uses LaunchScreen.storyboard for better flexibility and theme support
- Both implementations automatically adapt to system theme preferences
- Splash screen duration is optimized for quick app startup without being too brief
