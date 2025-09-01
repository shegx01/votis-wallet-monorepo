package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 */
@Composable
fun LottieAnimationPlaceholder(
    animationAsset: String,
    modifier: Modifier = Modifier,
    size: Dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        val emoji =
            when (animationAsset) {
                "WALLET.json" -> "💰"
                "ONCHAIN1.json" -> "⛓️"
                "SWAP1.json" -> "🔄"
                else -> "🎬"
            }
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
        )
    }
}
