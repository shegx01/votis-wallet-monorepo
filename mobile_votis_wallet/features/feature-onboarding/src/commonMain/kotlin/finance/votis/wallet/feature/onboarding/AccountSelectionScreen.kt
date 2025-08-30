package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AccountSelectionScreen(
    onGoogleSignIn: () -> Unit = {},
    onAppleSignIn: () -> Unit = {},
) {
    // Votis brand colors (matching the original design)
    val brandColor = Color(0xFF00B8B0)
    val greyText = Color(0xFF9E9E9E)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(surfaceColor)
                .padding(horizontal = 24.dp)
                .safeContentPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top spacer to push content down a bit
        Spacer(modifier = Modifier.height(60.dp))

        // Logo section - Using a large Votis-branded icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Votis Logo",
                modifier = Modifier.size(240.dp),
                tint = brandColor,
            )

            // Welcome text section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.headlineSmall,
                    color = greyText,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Votis Wallet",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Buttons section - Styled like social sign-in buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Apple Sign-In Button - Show on all platforms for demo, but typically iOS only
            OutlinedButton(
                onClick = onAppleSignIn,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = surfaceColor,
                        contentColor = onSurfaceColor,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "🍎", // Apple icon as emoji
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        text = "Continue with Apple",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Google Sign-In Button
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = surfaceColor,
                        contentColor = onSurfaceColor,
                    ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "🔍", // Google icon as emoji
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Footer with terms and privacy policy - fully clickable
        val annotatedText =
            buildAnnotatedString {
                withStyle(SpanStyle(color = greyText)) {
                    append("By continuing, you agree to our ")
                }
                pushStringAnnotation(tag = "terms", annotation = "https://votis.finance/terms")
                withStyle(SpanStyle(color = brandColor, textDecoration = TextDecoration.Underline)) {
                    append("Terms of Service")
                }
                pop()
                withStyle(SpanStyle(color = greyText)) {
                    append(" and ")
                }
                pushStringAnnotation(tag = "privacy", annotation = "https://votis.finance/privacy")
                withStyle(SpanStyle(color = brandColor, textDecoration = TextDecoration.Underline)) {
                    append("Privacy Policy")
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
                    horizontal = 16.dp,
                    vertical = 24.dp,
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
fun AccountSelectionScreenPreview() {
    MaterialTheme {
        AccountSelectionScreen()
    }
}
