package finance.votis.wallet.core.auth

/**
 * iOS implementation of OAuth configuration provider
 */
actual object OAuthConfig {
    /**
     * Gets the Google OAuth client ID for iOS
     * This will be provided via build configuration or Info.plist
     */
    actual fun getGoogleClientId(): String {
        // For now, we'll need to get this from build configuration
        // or Info.plist. This will be injected later
        return "YOUR_IOS_CLIENT_ID"
    }

    /**
     * Gets the Apple Sign-In client ID (primarily for iOS)
     */
    actual fun getAppleClientId(): String = "finance.votis.wallet"
}
