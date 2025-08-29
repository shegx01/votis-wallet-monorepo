package finance.votis.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import mobilevotiswallet.composeapp.generated.resources.votis_landing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingScreen() {
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
            contentDescription = "Votis Logo",
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
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.votisColors.greyText,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Votis",
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
                        text = "Continue with Apple",
                        icon = Icons.Filled.AccountCircle,
                        iconSize = 24.dp,
                        onClick = {
                            // TODO: Implement Apple Sign-In
                            println("Apple Sign-In clicked")
                        },
                    )
                }

                SocialSignInButton(
                    text = "Continue with Google",
                    icon = Icons.AutoMirrored.Filled.Login,
                    onClick = {
                        // TODO: Implement Google Sign-In
                        println("Google Sign-In clicked")
                    },
                )
            }
        }

        // Footer with terms and privacy policy
        val annotatedText =
            buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.votisColors.greyText)) {
                    append("By using Votis Wallet, you agree to the ")
                }

                pushStringAnnotation(tag = "terms", annotation = "https://votis.app/terms")
                withStyle(SpanStyle(color = MaterialTheme.votisColors.brand)) {
                    append("terms")
                }
                pop()

                withStyle(SpanStyle(color = MaterialTheme.votisColors.greyText)) {
                    append(" and ")
                }

                pushStringAnnotation(tag = "privacy", annotation = "https://votis.app/privacy")
                withStyle(SpanStyle(color = MaterialTheme.votisColors.brand)) {
                    append("privacy policy")
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
