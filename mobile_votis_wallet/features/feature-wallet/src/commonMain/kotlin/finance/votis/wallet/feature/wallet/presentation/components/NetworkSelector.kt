package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.flexible.bottomsheet.material3.FlexibleBottomSheet
import com.skydoves.flexible.core.rememberFlexibleBottomSheetState
import finance.votis.wallet.core.domain.model.Network
import finance.votis.wallet.core.ui.components.TabCard
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_copy
import mobilevotiswallet.features.feature_wallet.generated.resources.ic_scan
import org.jetbrains.compose.resources.painterResource

/**
 * Network selector component matching the design specification.
 * Shows a modal with user info and network selection with addresses.
 */
@Composable
fun NetworkSelector(
    selectedNetwork: Network,
    onNetworkSelected: (Network) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddressSelector by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Label
        Text(
            text = "Select network",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // Network selector button
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { showAddressSelector = true },
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NetworkIcon(
                        network = selectedNetwork,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = selectedNetwork.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select address",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // Address selector using FlexibleBottomSheet with proper state management for edge-to-edge support
    if (showAddressSelector) {
        val sheetState =
            rememberFlexibleBottomSheetState(
                isModal = true, // Enable modal behavior with backdrop
                containSystemBars = true, // Enable edge-to-edge support
            )

        FlexibleBottomSheet(
            onDismissRequest = { showAddressSelector = false },
            sheetState = sheetState, // Control state with sheetState parameter
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            windowInsets = WindowInsets(0), // Remove all insets for true edge-to-edge
            dragHandle = null, // We'll add our own custom handle
            scrimColor = Color.Black.copy(alpha = 0.2f), // Semi-transparent backdrop
        ) {
            TabCard(
                content = {
                    AddressSelectorContent(
                        selectedNetwork = selectedNetwork,
                        onNetworkSelected = { network ->
                            onNetworkSelected(network)
                            showAddressSelector = false
                        },
                        onDismiss = { showAddressSelector = false },
                    )
                },
            )
        }
    }
}

/**
 * Bottom sheet content showing the address selector as per design specification.
 */
@Composable
private fun AddressSelectorContent(
    selectedNetwork: Network,
    onNetworkSelected: (Network) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
    ) {
        // Bottom sheet handle
        Box(
            modifier =
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp),
                    ).align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header: "Select address" with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select address",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // User section: Avatar, username, "Share wallet username" with copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // User avatar (placeholder)
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(Color(0xFF4A90E2), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "👤",
                        fontSize = 20.sp,
                    )
                }

                Column {
                    Text(
                        text = "@shegx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Share wallet username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Copy button
            IconButton(
                onClick = { /* TODO: Copy username */ },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = "Copy username",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Network list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(getNetworkDataList()) { networkData ->
                NetworkAddressItem(
                    networkData = networkData,
                    isSelected = networkData.network == selectedNetwork,
                    onNetworkClick = { onNetworkSelected(networkData.network) },
                    onCopyClick = { /* TODO: Copy address */ },
                    onQrClick = { /* TODO: Show QR */ },
                    onCreateClick = { /* TODO: Create address */ },
                )
            }
        }
    }
}

/**
 * Individual network item in the address selector.
 */
@Composable
private fun NetworkAddressItem(
    networkData: NetworkAddressData,
    isSelected: Boolean,
    onNetworkClick: () -> Unit,
    onCopyClick: () -> Unit,
    onQrClick: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) Color(0xFF00BFA5) else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                .clickable { onNetworkClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                // Network icon
                NetworkIcon(
                    network = networkData.network,
                    modifier = Modifier.size(32.dp),
                )

                // Network info
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = networkData.network.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (networkData.hasAddress && networkData.address != null) {
                        Text(
                            text = networkData.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (networkData.hasAddress && networkData.address != null) {
                    // QR Code button
                    if (networkData.hasQrCode) {
                        IconButton(
                            onClick = onQrClick,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_scan),
                                contentDescription = "Show QR code",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Copy button
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_copy),
                            contentDescription = "Copy address",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else {
                    // Create address button
                    Button(
                        onClick = onCreateClick,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00BFA5),
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Text(
                            text = "Create address",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Network icon component with proper colors for each network.
 */
@Composable
private fun NetworkIcon(
    network: Network,
    modifier: Modifier = Modifier,
) {
    val (iconColor, iconText) =
        when (network) {
            Network.EVM_CHAINS -> Color(0xFF627EEA) to "⟐"
            Network.BITCOIN -> Color(0xFFF7931A) to "₿"
            Network.SOLANA -> Color(0xFF14F195) to "◎"
            Network.TRON -> Color(0xFFFF0013) to "T"
            Network.LITECOIN -> Color(0xFFBFBFBF) to "Ł"
        }

    Box(
        modifier =
            modifier
                .background(iconColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iconText,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * Data class for network address information.
 */
private data class NetworkAddressData(
    val network: Network,
    val address: String?,
    val hasAddress: Boolean,
    val hasQrCode: Boolean,
)

/**
 * Mock data for network addresses as shown in design.
 */
private fun getNetworkDataList(): List<NetworkAddressData> =
    listOf(
        NetworkAddressData(
            network = Network.EVM_CHAINS,
            address = "0x3B90c1e...8301",
            hasAddress = true,
            hasQrCode = true,
        ),
        NetworkAddressData(
            network = Network.BITCOIN,
            address = null,
            hasAddress = false,
            hasQrCode = false,
        ),
        NetworkAddressData(
            network = Network.SOLANA,
            address = null,
            hasAddress = false,
            hasQrCode = false,
        ),
        NetworkAddressData(
            network = Network.TRON,
            address = null,
            hasAddress = false,
            hasQrCode = false,
        ),
        NetworkAddressData(
            network = Network.LITECOIN,
            address = null,
            hasAddress = false,
            hasQrCode = true,
        ),
    )
