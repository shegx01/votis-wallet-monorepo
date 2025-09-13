package finance.votis.wallet.feature.wallet.demo

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import finance.votis.wallet.feature.wallet.WalletScreen

/**
 * Demo function to showcase the ReceiveScreen navigation.
 * This demonstrates how clicking the "Receive" button in WalletScreen
 * navigates to the ReceiveScreen and back.
 */
@Composable
fun WalletReceiveDemo(
    modifier: Modifier = Modifier,
) {
    var copiedText by remember { mutableStateOf<String?>(null) }

    WalletScreen(
        username = "shegx01",
        onCopyToClipboard = { text ->
            copiedText = text
            println("Copied to clipboard: $text")
            // In a real app, this would use platform-specific clipboard functionality
        },
        modifier = modifier,
    )

    // Show feedback when text is copied
    copiedText?.let { text ->
        LaunchedEffect(text) {
            kotlinx.coroutines.delay(3000)
            copiedText = null
        }
    }
}
