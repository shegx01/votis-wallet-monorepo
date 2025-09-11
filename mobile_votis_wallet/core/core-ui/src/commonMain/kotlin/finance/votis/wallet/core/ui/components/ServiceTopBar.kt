package finance.votis.wallet.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable top app bar component that accepts composable content.
 * Features a centered content area with back navigation.
 *
 * @param content The composable content to display in the center of the top bar
 * @param onBackClick Callback when back button is pressed
 * @param modifier Optional modifier for the top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTopBar(
    content: @Composable () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(x = (-24).dp),
                // Compensate for navigation icon space to center content
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content()
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        modifier = modifier,
    )
}
