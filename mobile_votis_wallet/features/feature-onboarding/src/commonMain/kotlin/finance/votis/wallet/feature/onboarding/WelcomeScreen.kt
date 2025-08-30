package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.ui.theme.AppTheme
import finance.votis.wallet.ui.theme.dimensions
import finance.votis.wallet.ui.theme.votisColors
import mobilevotiswallet.composeapp.generated.resources.Res
import mobilevotiswallet.composeapp.generated.resources.app_name_display
import mobilevotiswallet.composeapp.generated.resources.votis_landing
import mobilevotiswallet.composeapp.generated.resources.votis_logo_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.votisColors.surface)
                .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
                .safeContentPadding()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top spacer
        Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingXXLarge))

        // Logo and branding section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingLarge),
        ) {
            Image(
                painter = painterResource(Res.drawable.votis_landing),
                contentDescription = stringResource(Res.string.votis_logo_description),
                modifier = Modifier.size(MaterialTheme.dimensions.logoSize),
            )

            Text(
                text = stringResource(Res.string.app_name_display),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.votisColors.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        // Features section
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Your Gateway to Digital Finance",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.votisColors.onSurface,
                textAlign = TextAlign.Center,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
            ) {
                FeatureItem(
                    title = "Secure Wallet",
                    description = "Store and manage your digital assets with bank-level security",
                )
                
                FeatureItem(
                    title = "Easy Transactions",
                    description = "Send, receive, and track payments with just a few taps",
                )
                
                FeatureItem(
                    title = "Portfolio Management",
                    description = "Monitor your investments and track performance in real-time",
                )
            }
        }

        // Continue button
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.votisColors.brand,
                    contentColor = MaterialTheme.votisColors.onPrimary,
                ),
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingMedium))
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingXSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.votisColors.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.votisColors.greyText,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
        )
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    AppTheme {
        WelcomeScreen()
    }
}
