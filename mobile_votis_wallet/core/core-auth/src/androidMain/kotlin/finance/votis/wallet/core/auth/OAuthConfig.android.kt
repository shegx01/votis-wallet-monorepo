package finance.votis.wallet.core.auth

/**
 * Android implementation of OAuth configuration provider
 */
actual object OAuthConfig {
    /**
     * Gets the Google OAuth client ID for Android
     * This will be provided via build configuration or resources
     */
    actual fun getGoogleClientId(): String {
        // For now, we'll need to get this from build configuration
        // or resources. This will be injected later
        return "YOUR_ANDROID_CLIENT_ID"
    }

    /**
     * Gets the Apple Sign-In client ID (not typically used on Android)
     */
    actual fun getAppleClientId(): String = "finance.votis.wallet"
}
