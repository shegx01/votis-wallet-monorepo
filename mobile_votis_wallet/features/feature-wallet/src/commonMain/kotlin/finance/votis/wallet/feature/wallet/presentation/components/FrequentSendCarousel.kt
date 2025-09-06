package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.ContactUser
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.frequent_send_title
import org.jetbrains.compose.resources.stringResource

/**
 * Card-based frequent send carousel with pagination showing 4 contacts per page.
 * Matches design specification with subtle border, proper spacing, and page indicators.
 */
@Composable
fun FrequentSendCarousel(
    contacts: List<ContactUser>,
    onContactClick: (ContactUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    // State for current page
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 4
    val totalPages = if (contacts.isEmpty()) 1 else (contacts.size + pageSize - 1) / pageSize

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .wrapContentHeight(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            // Section title with proper typography
            Text(
                text = stringResource(Res.string.frequent_send_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            // Current page contacts in 2x2 grid
            val startIndex = currentPage * pageSize
            val endIndex = minOf(startIndex + pageSize, contacts.size)
            val pageContacts =
                if (contacts.isNotEmpty()) {
                    contacts.subList(startIndex, endIndex)
                } else {
                    emptyList()
                }

            // 2x2 Grid layout
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // First row (up to 2 contacts)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    pageContacts.take(2).forEach { contact ->
                        FrequentContactItem(
                            contact = contact,
                            onClick = { onContactClick(contact) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill empty spaces if less than 2 contacts in first row
                    repeat(2 - minOf(2, pageContacts.size)) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }

                // Second row (contacts 3 and 4)
                if (pageContacts.size > 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        pageContacts.drop(2).take(2).forEach { contact ->
                            FrequentContactItem(
                                contact = contact,
                                onClick = { onContactClick(contact) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Fill empty spaces if less than 2 contacts in second row
                        repeat(2 - minOf(2, pageContacts.size - 2)) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Page indicators
            if (totalPages > 1) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                ) {
                    repeat(totalPages) { page ->
                        PageIndicator(
                            isActive = page == currentPage,
                            onClick = { currentPage = page },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequentContactItem(
    contact: ContactUser,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Avatar circle with initials
        ContactAvatar(
            initials = getContactInitials(contact.displayName ?: contact.username),
            modifier = Modifier.size(56.dp),
        )

        // Contact name (truncated if too long)
        Text(
            text = contact.displayName ?: contact.username,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun ContactAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PageIndicator(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    color =
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 4.dp),
    )
}

private fun getContactInitials(name: String): String =
    try {
        name
            .split(" ")
            .take(2)
            .mapNotNull { word ->
                // Find the first letter in each word, ignoring special characters
                word.firstOrNull { it.isLetter() }?.uppercaseChar()
            }.joinToString("")
            .take(2)
            .ifEmpty { "U" } // Default fallback
    } catch (e: Exception) {
        "U" // Safe fallback for any conversion issues
    }
