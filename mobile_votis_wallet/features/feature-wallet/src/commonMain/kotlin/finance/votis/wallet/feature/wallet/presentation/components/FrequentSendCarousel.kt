package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
 * Horizontal scrolling carousel for frequent contacts in a card container.
 * Matches design specification with 4 contacts per page and page indicators.
 */
@Composable
fun FrequentSendCarousel(
    contacts: List<ContactUser>,
    onContactClick: (ContactUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    // State for tracking current page based on scroll position
    var currentPage by remember { mutableStateOf(0) }
    val contactsPerPage = 4
    val totalPages = if (contacts.isEmpty()) 1 else (contacts.size + contactsPerPage - 1) / contactsPerPage

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .wrapContentHeight(),
    ) {
        // Shadow layer
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(x = 2.dp, y = 2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                    ),
        )

        // Main card
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                // Section title
                Text(
                    text = stringResource(Res.string.frequent_send_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Horizontal scrolling row of contacts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Show current page contacts (4 per page)
                    val startIndex = currentPage * contactsPerPage
                    val endIndex = minOf(startIndex + contactsPerPage, contacts.size)
                    val pageContacts =
                        if (contacts.isNotEmpty()) {
                            contacts.subList(startIndex, endIndex)
                        } else {
                            emptyList()
                        }

                    pageContacts.forEach { contact ->
                        FrequentContactItem(
                            contact = contact,
                            onClick = { onContactClick(contact) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Fill empty spaces if less than 4 contacts
                    repeat(contactsPerPage - pageContacts.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }

                // Page indicators
                if (totalPages > 1) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                    ) {
                        repeat(totalPages) { page ->
                            PageIndicator(
                                isActive = page == currentPage,
                                onClick = { currentPage = page },
                                modifier = Modifier.padding(horizontal = 3.dp),
                            )
                        }
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Avatar circle with initials - matching design size
        ContactAvatar(
            initials = getContactInitials(contact.displayName ?: contact.username),
            modifier = Modifier.size(60.dp),
        )

        // Contact name - matching design typography
        Text(
            text = contact.displayName ?: contact.username,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContactAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    // Generate consistent color based on initials for visual variety
    val avatarColor = generateAvatarColor(initials)

    Box(
        modifier =
            modifier
                .background(
                    color = avatarColor,
                    shape = CircleShape,
                ).clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
        )
    }
}

// Generate consistent avatar colors based on initials
private fun generateAvatarColor(initials: String): androidx.compose.ui.graphics.Color {
    val colors =
        listOf(
            androidx.compose.ui.graphics
                .Color(0xFF6366F1), // Indigo
            androidx.compose.ui.graphics
                .Color(0xFF8B5CF6), // Violet
            androidx.compose.ui.graphics
                .Color(0xFF06B6D4), // Cyan
            androidx.compose.ui.graphics
                .Color(0xFF10B981), // Emerald
            androidx.compose.ui.graphics
                .Color(0xFFF59E0B), // Amber
            androidx.compose.ui.graphics
                .Color(0xFFEF4444), // Red
            androidx.compose.ui.graphics
                .Color(0xFF3B82F6), // Blue
            androidx.compose.ui.graphics
                .Color(0xFF84CC16), // Lime
        )

    val hash = initials.hashCode()
    return colors[kotlin.math.abs(hash) % colors.size]
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
                            MaterialTheme.colorScheme.outlineVariant
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
