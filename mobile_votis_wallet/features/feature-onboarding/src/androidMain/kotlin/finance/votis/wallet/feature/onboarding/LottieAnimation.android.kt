package finance.votis.wallet.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Android implementation using Lottie animations
 */
@Composable
actual fun LottieAnimationView(
    animationAsset: String,
    modifier: Modifier,
    size: Dp,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("files/$animationAsset"),
    )

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = modifier,
        )
    } else {
        // Fallback to placeholder while loading or if file not found
        LottieAnimationPlaceholder(
            animationAsset = animationAsset,
            modifier = modifier,
            size = size,
        )
    }
}
