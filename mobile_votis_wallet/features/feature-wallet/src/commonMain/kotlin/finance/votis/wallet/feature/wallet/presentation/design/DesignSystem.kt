package finance.votis.wallet.feature.wallet.presentation.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Design system utilities for consistent styling across wallet components.
 * Provides centralized colors, spacing, and styling patterns.
 */
object WalletDesignSystem {
    /**
     * Consistent surface color used across wallet components like:
     * - Action button backgrounds
     * - Card containers
     * - Secondary surfaces
     */
    @Composable
    fun surfaceContainer(): Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    /**
     * Outline color for borders and dividers with subtle appearance
     */
    @Composable
    fun subtleBorder(): Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
}
