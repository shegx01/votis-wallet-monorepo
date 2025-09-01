package finance.votis.wallet.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import kotlinx.coroutines.delay

/**
 * Animated onboarding carousel that displays pages with auto-advance and manual swipe
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingCarousel(
    pages: List<OnboardingPage>,
    modifier: Modifier = Modifier,
    autoAdvanceIntervalMs: Long = 4000L,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Auto-advance logic
    LaunchedEffect(pagerState.currentPage) {
        delay(autoAdvanceIntervalMs)
        val nextPage = (pagerState.currentPage + 1) % pages.size
        pagerState.animateScrollToPage(nextPage)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Carousel area with pages
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(320.dp),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { pageIndex ->
                val page = pages[pageIndex]

                // Animated page content
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = 300,
                                    easing = EaseOut,
                                ),
                        ).togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 300,
                                        easing = EaseIn,
                                    ),
                            ),
                        )
                    },
                    label = "page_content_animation",
                ) { targetPage ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(
                                    color = targetPage.backgroundColor,
                                    shape = MaterialTheme.shapes.large,
                                ).padding(MaterialTheme.dimensions.spacingLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    MaterialTheme.dimensions.spacingMedium,
                                ),
                        ) {
                            // Placeholder for illustration/icon
                            Box(
                                modifier =
                                    Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                            )

                            // Page content
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        MaterialTheme.dimensions.spacingSmall,
                                    ),
                            ) {
                                Text(
                                    text = targetPage.headline,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                )

                                Text(
                                    text = targetPage.subtitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier.padding(
                                            horizontal = MaterialTheme.dimensions.spacingMedium,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))

        // Page indicators
        PageIndicators(
            pageCount = pages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(bottom = MaterialTheme.dimensions.spacingMedium),
        )
    }
}

/**
 * Dot indicators for the carousel
 */
@Composable
private fun PageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            val isSelected = currentPage == index
            val animatedSize by animateDpAsState(
                targetValue = if (isSelected) 12.dp else 8.dp,
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = EaseInOut,
                    ),
                label = "indicator_size_animation",
            )
            val animatedColor by animateColorAsState(
                targetValue =
                    if (isSelected) {
                        MaterialTheme.votisColors.brand
                    } else {
                        MaterialTheme.votisColors.greyText.copy(alpha = 0.4f)
                    },
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = EaseInOut,
                    ),
                label = "indicator_color_animation",
            )

            Box(
                modifier =
                    Modifier
                        .size(animatedSize)
                        .clip(CircleShape)
                        .background(animatedColor),
            )
        }
    }
}
