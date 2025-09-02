package finance.votis.wallet.feature.onboarding.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * iOS implementation using Compottie for multiplatform compatibility
 * Configured to loop forever and use a placeholder on load failure
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LottieAnimationView(
    animationAsset: String,
    modifier: Modifier,
    size: Dp,
) {
    // Create composition from iOS bundle resources
    val composition by rememberLottieComposition {
        try {
            // Load JSON file from iOS app bundle
            val bundle = NSBundle.mainBundle
            val fileName = animationAsset.removeSuffix(".json")
            val filePath = bundle.pathForResource(fileName, "json")

            if (filePath != null) {
                val jsonString =
                    NSString.stringWithContentsOfFile(
                        filePath,
                        NSUTF8StringEncoding,
                        null,
                    )
                if (jsonString != null) {
                    LottieCompositionSpec.JsonString(jsonString as String)
                } else {
                    // Return empty JSON as fallback to avoid null
                    LottieCompositionSpec.JsonString("{}")
                }
            } else {
                // Return empty JSON as fallback to avoid null
                LottieCompositionSpec.JsonString("{}")
            }
        } catch (e: Exception) {
            println("Failed to load Lottie animation: $animationAsset - ${e.message}")
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
