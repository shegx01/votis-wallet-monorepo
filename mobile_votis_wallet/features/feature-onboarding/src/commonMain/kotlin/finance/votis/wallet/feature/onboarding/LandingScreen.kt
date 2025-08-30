package finance.votis.wallet.feature.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.ui.theme.AppTheme
import finance.votis.wallet.ui.theme.dimensions
import finance.votis.wallet.ui.theme.votisColors
import kotlinx.coroutines.delay
import mobilevotiswallet.composeapp.generated.resources.Res
import mobilevotiswallet.composeapp.generated.resources.app_name_display
import mobilevotiswallet.composeapp.generated.resources.votis_landing
import mobilevotiswallet.composeapp.generated.resources.votis_logo_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LandingScreen(
    onAnimationComplete: () -> Unit = {},
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Logo animations
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseOutBounce,
        ),
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = 200,
            easing = EaseOutQuart,
        ),
    )
    
    // Text animations
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 1000,
            easing = EaseOutQuart,
        ),
    )
    
    val textTranslationY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 1000,
            easing = EaseOutQuart,
        ),
    )

    // Background gradient animation
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 0.1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            delayMillis = 500,
            easing = EaseInOut,
        ),
    )

    // Start animation on composition
    LaunchedEffect(Unit) {
        startAnimation = true
        // Navigate to next screen after animations complete
        delay(2500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.votisColors.surface),
        contentAlignment = Alignment.Center,
    ) {
        // Animated background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(backgroundAlpha)
                .background(
                    MaterialTheme.votisColors.brand.copy(alpha = 0.1f)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Animated logo
            Image(
                painter = painterResource(Res.drawable.votis_landing),
                contentDescription = stringResource(Res.string.votis_logo_description),
                modifier = Modifier
                    .size(MaterialTheme.dimensions.logoSize * 1.2f)
                    .scale(logoScale)
                    .alpha(logoAlpha),
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingXLarge))

            // Animated app name
            Text(
                text = stringResource(Res.string.app_name_display),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.votisColors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .graphicsLayer {
                        translationY = textTranslationY
                    },
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingMedium))

            // Animated tagline
            Text(
                text = "Your Digital Finance Partner",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.votisColors.greyText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha * 0.8f)
                    .graphicsLayer {
                        translationY = textTranslationY + 20f
                    },
            )
        }

        // Animated loading indicator at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = MaterialTheme.dimensions.spacingXXLarge),
        ) {
            AnimatedLoadingIndicator(
                isVisible = startAnimation,
                modifier = Modifier.alpha(textAlpha),
            )
        }
    }
}

@Composable
private fun AnimatedLoadingIndicator(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
    )

    if (isVisible) {
        CircularProgressIndicator(
            modifier = modifier
                .size(24.dp)
                .graphicsLayer {
                    rotationZ = rotation
                },
            color = MaterialTheme.votisColors.brand,
            strokeWidth = 2.dp,
        )
    }
}

@Preview
@Composable
fun LandingScreenPreview() {
    AppTheme {
        LandingScreen()
    }
}
