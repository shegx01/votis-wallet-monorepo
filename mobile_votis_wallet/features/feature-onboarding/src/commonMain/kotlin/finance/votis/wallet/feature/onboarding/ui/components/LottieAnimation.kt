package finance.votis.wallet.feature.onboarding.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Multiplatform Lottie animation component
 * Currently shows emoji placeholders, will be enhanced with actual Lottie support
 */
@Composable
expect fun LottieAnimationView(
    animationAsset: String,
    modifier: Modifier = Modifier,
    size: Dp,
)

/**
 * Common placeholder implementation for when Lottie is not available
 * Returns blank space to avoid initial flash
 */
@Composable
fun LottieAnimationPlaceholder(
    animationAsset: String,
    modifier: Modifier = Modifier,
    size: Dp,
) {
    // Return blank space instead of emoji to prevent initial flash
    Box(
        modifier = modifier.size(size),
    )
}
