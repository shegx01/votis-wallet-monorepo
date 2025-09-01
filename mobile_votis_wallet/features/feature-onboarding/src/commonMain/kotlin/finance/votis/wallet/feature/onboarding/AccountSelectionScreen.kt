package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.ui.components.SocialSignInButton
import finance.votis.wallet.core.ui.theme.AppTheme
import finance.votis.wallet.core.ui.theme.dimensions
import finance.votis.wallet.core.ui.theme.votisColors
import finance.votis.wallet.core.ui.utils.PlatformUtils
import mobilevotiswallet.features.feature_onboarding.generated.resources.Res
import mobilevotiswallet.features.feature_onboarding.generated.resources.continue_with_apple
import mobilevotiswallet.features.feature_onboarding.generated.resources.continue_with_google
import mobilevotiswallet.features.feature_onboarding.generated.resources.ic_apple
import mobilevotiswallet.features.feature_onboarding.generated.resources.ic_google
import mobilevotiswallet.features.feature_onboarding.generated.resources.legal_text_middle
import mobilevotiswallet.features.feature_onboarding.generated.resources.legal_text_prefix
import mobilevotiswallet.features.feature_onboarding.generated.resources.privacy_policy_link_text
import mobilevotiswallet.features.feature_onboarding.generated.resources.privacy_policy_url
import mobilevotiswallet.features.feature_onboarding.generated.resources.terms_link_text
import mobilevotiswallet.features.feature_onboarding.generated.resources.terms_url
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSelectionScreen(
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {},
) {
    OnboardingScreen(
        onGoogleSignIn = onGoogleSignIn,
        onAppleSignIn = onAppleSignIn,
    )
}

@Composable
fun OnboardingScreen(
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val onboardingPages = OnboardingPages.getPages()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.votisColors.surface)
                .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
                .safeContentPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top spacer
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingLarge))

        // Animated onboarding carousel
        OnboardingCarousel(
            pages = onboardingPages,
            modifier = Modifier.weight(1f),
        )

        // Fixed buttons and footer section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingLarge),
        ) {
            // Sign-in buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Only show Apple Sign-In on iOS devices
                if (PlatformUtils.isIos) {
                    SocialSignInButton(
                        text = stringResource(Res.string.continue_with_apple),
                        icon = painterResource(Res.drawable.ic_apple),
                        iconSize = 34.dp,
                        onClick = {
                            // TODO: Implement Apple Sign-In
                            println("Apple Sign-In clicked")
                            onAppleSignIn()
                        },
                    )
                }

                SocialSignInButton(
                    text = stringResource(Res.string.continue_with_google),
                    icon = painterResource(Res.drawable.ic_google),
                    iconTint = Color.Unspecified,
                    onClick = {
                        // TODO: Implement Google Sign-In
                        println("Google Sign-In clicked")
                        onGoogleSignIn()
                    },
                )
            }

            // Footer with terms and privacy policy
            val annotatedText =
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.votisColors.greyText)) {
                        append(stringResource(Res.string.legal_text_prefix))
                    }

                    pushStringAnnotation(tag = "terms", annotation = stringResource(Res.string.terms_url))
                    withStyle(SpanStyle(color = MaterialTheme.votisColors.brand)) {
                        append(stringResource(Res.string.terms_link_text))
                    }
                    pop()

                    withStyle(SpanStyle(color = MaterialTheme.votisColors.greyText)) {
                        append(stringResource(Res.string.legal_text_middle))
                    }

                    pushStringAnnotation(tag = "privacy", annotation = stringResource(Res.string.privacy_policy_url))
                    withStyle(SpanStyle(color = MaterialTheme.votisColors.brand)) {
                        append(stringResource(Res.string.privacy_policy_link_text))
                    }
                    pop()
                }

            @Suppress("DEPRECATION")
            ClickableText(
                text = annotatedText,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                    ),
                modifier =
                    Modifier.padding(
                        horizontal = MaterialTheme.dimensions.footerHorizontalPadding,
                        vertical = MaterialTheme.dimensions.footerVerticalPadding,
                    ),
                onClick = { offset ->
                    annotatedText
                        .getStringAnnotations(tag = "terms", start = offset, end = offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                    annotatedText
                        .getStringAnnotations(tag = "privacy", start = offset, end = offset)
                        .firstOrNull()
                        ?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                },
            )
        }
    }
}

@Preview
@Composable
fun AccountSelectionScreenPreview() {
    AppTheme {
        AccountSelectionScreen()
    }
}
