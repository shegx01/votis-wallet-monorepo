package finance.votis.wallet

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    // Initialize Koin for iOS - this is safe to call multiple times
    initializeKoin()
    App()
}
