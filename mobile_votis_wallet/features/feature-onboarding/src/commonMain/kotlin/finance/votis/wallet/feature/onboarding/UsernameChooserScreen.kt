package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import finance.votis.wallet.ui.theme.AppTheme
import finance.votis.wallet.ui.theme.dimensions
import finance.votis.wallet.ui.theme.votisColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsernameChooserScreen(
    userInfo: UserInfo? = null,
    onUsernameSelected: (String) -> Unit = {},
    onSkip: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    var username by remember { mutableStateOf("") }
    var isUsernameValid by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // Validate username in real-time
    LaunchedEffect(username) {
        isUsernameValid = isValidUsername(username)
        showError = username.isNotEmpty() && !isUsernameValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.votisColors.surface)
            .padding(horizontal = MaterialTheme.dimensions.screenHorizontalPadding)
            .safeContentPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Content section
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingLarge),
        ) {
            // Header
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Username Setup",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.votisColors.brand,
                )

                Text(
                    text = "Choose Your Username",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.votisColors.onSurface,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Select a unique username that others can use to find and send you payments. You can always change this later.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.votisColors.greyText,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                )
            }

            // User info display (if available)
            userInfo?.let { info ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.votisColors.brand.copy(alpha = 0.1f),
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MaterialTheme.dimensions.spacingMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Authenticated",
                            tint = MaterialTheme.votisColors.brand,
                        )
                        
                        Column {
                            Text(
                                text = "Signed in as ${info.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.votisColors.onSurface,
                            )
                            Text(
                                text = info.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.votisColors.greyText,
                            )
                        }
                    }
                }
            }

            // Username input section
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        // Only allow alphanumeric and underscore, max 20 characters
                        if (newValue.length <= 20 && newValue.all { it.isLetterOrDigit() || it == '_' }) {
                            username = newValue.lowercase()
                        }
                    },
                    label = { Text("Username") },
                    placeholder = { Text("e.g., john_doe") },
                    leadingIcon = {
                        Text(
                            text = "@",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.votisColors.greyText,
                        )
                    },
                    trailingIcon = {
                        if (isUsernameValid && username.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Valid",
                                tint = Color(0xFF4CAF50),
                            )
                        }
                    },
                    isError = showError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.votisColors.brand,
                        focusedLabelColor = MaterialTheme.votisColors.brand,
                    ),
                )

                // Error or helper text
                if (showError) {
                    Text(
                        text = "Username must be 3-20 characters, letters, numbers, and underscores only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (username.isNotEmpty()) {
                    Text(
                        text = "Available: @$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                    )
                } else {
                    Text(
                        text = "Username must be 3-20 characters, letters, numbers, and underscores only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.votisColors.greyText,
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingMedium))
        }

        // Action buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimensions.spacingMedium),
        ) {
            Button(
                onClick = {
                    if (isUsernameValid && username.isNotEmpty()) {
                        onUsernameSelected(username)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isUsernameValid && username.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.votisColors.brand,
                    contentColor = MaterialTheme.votisColors.onPrimary,
                ),
            ) {
                Text(
                    text = "Continue with @$username",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.votisColors.greyText,
                ),
            ) {
                Text(
                    text = "Skip for now",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.votisColors.greyText,
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimensions.spacingMedium))
        }
    }
}

private fun isValidUsername(username: String): Boolean {
    return username.length >= 3 && 
           username.length <= 20 && 
           username.all { it.isLetterOrDigit() || it == '_' } &&
           username.first().isLetter() // Must start with a letter
}

@Preview
@Composable
fun UsernameChooserScreenPreview() {
    AppTheme {
        UsernameChooserScreen(
            userInfo = UserInfo(
                id = "123",
                email = "john.doe@example.com",
                name = "John Doe",
            ),
        )
    }
}
