package finance.votis.wallet.feature.onboarding.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Android implementation using Compottie for multiplatform compatibility
 * Configured to loop forever and use a placeholder on load failure
 */
@Composable
actual fun LottieAnimationView(
    animationAsset: String,
    modifier: Modifier,
    size: Dp,
) {
    // Get Android context for asset access
    val context = LocalContext.current

    // Create composition from Android assets
    val composition by rememberLottieComposition {
        try {
            // Read JSON content from Android assets
            val jsonString =
                context.assets
                    .open(animationAsset)
                    .bufferedReader()
                    .use { it.readText() }
            LottieCompositionSpec.JsonString(jsonString)
        } catch (e: Exception) {
            println("Failed to load Lottie animation from assets: $animationAsset - ${e.message}")
            // Return empty JSON as fallback to avoid null
            LottieCompositionSpec.JsonString("{}")
        }
    }

    if (composition != null) {
        // Create painter with infinite loop animation
        val painter =
            rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever,
            )

        // Render the Lottie animation as an Image
        Image(
            painter = painter,
            contentDescription = "Animation: $animationAsset",
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
