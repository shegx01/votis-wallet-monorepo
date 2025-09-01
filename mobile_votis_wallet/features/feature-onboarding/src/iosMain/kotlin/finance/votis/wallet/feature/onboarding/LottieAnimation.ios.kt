package finance.votis.wallet.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * iOS implementation - currently uses placeholder
 * TODO: Add iOS-specific Lottie support when available
 */
@Composable
actual fun LottieAnimationView(
    animationAsset: String,
    modifier: Modifier,
    size: Dp,
) {
    // For now, use placeholder on iOS
    LottieAnimationPlaceholder(
        animationAsset = animationAsset,
        modifier = modifier,
        size = size,
    )
}
