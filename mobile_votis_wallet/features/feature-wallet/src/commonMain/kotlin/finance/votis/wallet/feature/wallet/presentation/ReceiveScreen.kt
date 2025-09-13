package finance.votis.wallet.feature.wallet.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import finance.votis.wallet.core.domain.model.Network
import finance.votis.wallet.core.ui.components.ServiceTopBar
import finance.votis.wallet.core.ui.components.TabCard
import finance.votis.wallet.core.ui.components.WalletAddressCard
import finance.votis.wallet.feature.wallet.presentation.components.NetworkSelector
import finance.votis.wallet.feature.wallet.presentation.components.QrCodeDisplay

@Composable
fun ReceiveScreen(
    onNavigateBack: () -> Unit,
    onCopyToClipboard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedNetwork by remember { mutableStateOf(Network.EVM_CHAINS) }

    // Generate wallet address based on selected network
    val walletAddress =
        remember(selectedNetwork) {
            generateMockWalletAddress(selectedNetwork)
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        // Top bar using ServiceTopBar
        ServiceTopBar(
            content = {
                Text(
                    text = "Receive",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            onBackClick = onNavigateBack,
        )

        // Main content
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Network Selector
            NetworkSelector(
                selectedNetwork = selectedNetwork,
                onNetworkSelected = { selectedNetwork = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(34.dp))

            // QR Code Display
            QrCodeDisplay(
                data = walletAddress,
                size = 280.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Wallet Address Card wrapped in TabCard
            TabCard(
                content = {
                    WalletAddressCard(
                        address = walletAddress,
                        onCopyAddress = onCopyToClipboard,
                    )
                },
                contentDescription = "Wallet address for ${selectedNetwork.displayName} network",
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info text - matching the design spec
            Text(
                text =
                    "Send only ${selectedNetwork.displayName} network tokens to this address. " +
                        "All tokens sent to this address will appear in your wallet automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * Generate a mock wallet address based on the selected network.
 * In a real implementation, this would come from the wallet service.
 */
private fun generateMockWalletAddress(network: Network): String =
    when (network) {
        Network.EVM_CHAINS -> "0x742d35Cc6634C0532925a3b8D2B4E3DD8E5E9AaB"
        Network.SOLANA -> "0x8ba1f109551bD432803012645Hac136c82C4a4B1"
        Network.TRON -> "0x1A1ec25DC08e98e5E93F1104B5e5cd27D2ce17d5"
        Network.BITCOIN -> "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
        Network.LITECOIN -> "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
    }
