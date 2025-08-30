package finance.votis.wallet

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import finance.votis.wallet.ui.components.SocialSignInButton
import finance.votis.wallet.ui.theme.AppTheme
import finance.votis.wallet.ui.theme.dimensions
import finance.votis.wallet.ui.theme.votisColors
import finance.votis.wallet.utils.PlatformUtils
import mobilevotiswallet.composeapp.generated.resources.Res
import mobilevotiswallet.composeapp.generated.resources.app_name_display
import mobilevotiswallet.composeapp.generated.resources.continue_with_apple
import mobilevotiswallet.composeapp.generated.resources.continue_with_google
import mobilevotiswallet.composeapp.generated.resources.ic_apple
import mobilevotiswallet.composeapp.generated.resources.ic_google
import mobilevotiswallet.composeapp.generated.resources.legal_text_middle
import mobilevotiswallet.composeapp.generated.resources.legal_text_prefix
import mobilevotiswallet.composeapp.generated.resources.privacy_policy_link_text
import mobilevotiswallet.composeapp.generated.resources.privacy_policy_url
import mobilevotiswallet.composeapp.generated.resources.terms_link_text
import mobilevotiswallet.composeapp.generated.resources.terms_url
import mobilevotiswallet.composeapp.generated.resources.votis_landing
import mobilevotiswallet.composeapp.generated.resources.votis_logo_description
import mobilevotiswallet.composeapp.generated.resources.welcome_prefix
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingScreen(onContinue: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current

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
        // Top spacer to push content down a bit
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingXXLarge))

        // Logo section
        Image(
            painter = painterResource(Res.drawable.votis_landing),
            contentDescription = stringResource(Res.string.votis_logo_description),
            modifier = Modifier.size(MaterialTheme.dimensions.logoSize),
        )

        // Content section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingXLarge),
        ) {
            // Welcome text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingSmall),
            ) {
                Text(
                    text = stringResource(Res.string.welcome_prefix),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.votisColors.greyText,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.app_name_display),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.votisColors.onSurface,
                    textAlign = TextAlign.Center,
                )
            }

            // Buttons section
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
                            onContinue()
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
                        onContinue()
                    },
                )
            }
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

@Preview
@Composable
fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen()
    }
}
