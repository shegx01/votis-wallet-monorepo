package finance.votis.wallet.feature.wallet.presentation.send

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.AppTheme

/**
 * Preview version of SendScreen that can be used for testing the UI design
 * without needing the full navigation context.
 */
@Composable
fun SendScreenPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            SendScreen(
                onBackClick = { /* Preview - no action */ },
                modifier = Modifier.padding(0.dp),
            )
        }
    }
}

/**
 * Simple standalone version for testing the amount section specifically
 */
@Composable
fun AmountSectionPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // This would show just the amount section for testing
            // Could be used in a demo app or testing environment
        }
    }
}
