package finance.votis.wallet.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.*

/**
 * Single clean onboarding page that matches the exact design spec
 * Optionally cycles through animations while keeping the same clean text
 */
@Composable
fun SingleOnboardingPage(
    modifier: Modifier = Modifier,
    enableAnimationCycling: Boolean = true,
    autoAdvanceIntervalMs: Long = 4000L,
) {
    // Get the pages for animation cycling
    val pages = OnboardingPages.getPages()
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var isInitialLoad by remember { mutableStateOf(true) }

    // Handle initial load delay to let first animation load
    LaunchedEffect(Unit) {
        delay(300) // Small delay to let first Lottie load
        isInitialLoad = false
    }

    // Auto-advance through animations if enabled
    if (enableAnimationCycling && !isInitialLoad) {
        LaunchedEffect(currentPageIndex) {
            delay(autoAdvanceIntervalMs)
            currentPageIndex = (currentPageIndex + 1) % pages.size
        }
    }

    // Use the appropriate page content based on cycling or default
    val currentPage = if (enableAnimationCycling) {
        pages[currentPageIndex]
    } else {
        OnboardingPage(
            headline = stringResource(Res.string.onboarding_page1_headline),
            subtitle = stringResource(Res.string.onboarding_page1_subtitle),
            animationAsset = "WALLET.json",
            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
        )
    }

    // Balanced layout with flexible animation and centered content
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = MaterialTheme.dimensions.spacingXLarge,
                vertical = MaterialTheme.dimensions.spacingLarge,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, // Center the overall content
    ) {
        // Brand color resolution with iOS-safe fallbacks
        val greyTextColor = runCatching {
            MaterialTheme.votisColors.greyText
        }.getOrElse {
            Color(0xFF9E9E9E) // Fallback to direct color for iOS compatibility
        }
        val onSurfaceColor = runCatching {
            MaterialTheme.votisColors.onSurface
        }.getOrElse {
            MaterialTheme.colorScheme.onSurface // Fallback to Material3 color
        }

        // Flexible animation area - adapts to available space
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false), // Takes available space but doesn't force fill
            contentAlignment = Alignment.Center
        ) {
            // Dynamic animation size based on available space
            LottieAnimationView(
                animationAsset = currentPage.animationAsset,
                modifier = Modifier
                    .fillMaxSize(0.8f) // Uses 80% of available space
                    .aspectRatio(1f), // Maintain square aspect ratio
                size = 300.dp, // Max size reference for aspect ratio
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingXXLarge)) // Brand spacing between animation and text

        // Text section - with iOS-safe fade animation on content changes
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Animate text alpha with proper fade-in on changes
            var shouldAnimate by remember { mutableStateOf(false) }
            
            // Trigger animation on page changes
            LaunchedEffect(currentPageIndex) {
                if (!isInitialLoad) {
                    shouldAnimate = false // Reset to trigger fade
                    delay(50) // Small delay to ensure reset
                    shouldAnimate = true // Trigger fade-in
                }
            }
            
            // Initialize animation on first load
            LaunchedEffect(isInitialLoad) {
                if (!isInitialLoad) {
                    shouldAnimate = true
                }
            }
            
            // Simple alpha animation that fades in
            val textAlpha by animateFloatAsState(
                targetValue = if (shouldAnimate) 1f else 0.3f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = EaseInOutCubic
                ),
                label = "TextAlphaAnimation"
            )
            
            // Create continuous text flow using AnnotatedString
            val annotatedText = buildAnnotatedString {
                // Headline part - bold and primary color
                withStyle(
                    style = SpanStyle(
                        color = onSurfaceColor,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(currentPage.headline)
                }
                
                // Add space between headline and subtitle
                append(" ")
                
                // Subtitle part - normal weight and grey color
                withStyle(
                    style = SpanStyle(
                        color = greyTextColor,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    append(currentPage.subtitle)
                }
            }
            
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = textAlpha }
            )
        }
    }
}
