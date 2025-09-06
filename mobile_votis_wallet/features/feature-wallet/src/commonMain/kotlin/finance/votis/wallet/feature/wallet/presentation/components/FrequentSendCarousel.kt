package finance.votis.wallet.feature.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import finance.votis.wallet.core.domain.model.ContactUser
import mobilevotiswallet.features.feature_wallet.generated.resources.Res
import mobilevotiswallet.features.feature_wallet.generated.resources.contact_angel_lubin
import mobilevotiswallet.features.feature_wallet.generated.resources.contact_ann_baptista
import mobilevotiswallet.features.feature_wallet.generated.resources.contact_craig_septimus
import mobilevotiswallet.features.feature_wallet.generated.resources.contact_makenna_t
import mobilevotiswallet.features.feature_wallet.generated.resources.frequent_send_title
import org.jetbrains.compose.resources.stringResource

/**
 * Horizontal carousel showing frequent contacts for quick send operations.
 * Matches the original design with user avatars and names.
 */
@Composable
fun FrequentSendCarousel(
    contacts: List<ContactUser>,
    onContactClick: (ContactUser) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Section title
        Text(
            text = stringResource(Res.string.frequent_send_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        // Horizontal scrolling list of contacts
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(contacts) { contact ->
                FrequentContactItem(
                    contact = contact,
                    onClick = { onContactClick(contact) },
                )
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

private fun getContactInitials(name: String): String =
    name
        .split(" ")
        .take(2)
        .map { it.firstOrNull()?.uppercaseChar() ?: "" }
        .joinToString("")
        .take(2)

/**
 * Creates mock frequent contacts data matching the original design
 */
@Composable
fun getMockFrequentContacts(): List<ContactUser> =
    listOf(
        ContactUser(
            id = "1",
            username = "angel_lubin",
            walletAddress = "0x1234...5678",
            displayName = stringResource(Res.string.contact_angel_lubin),
        ),
        ContactUser(
            id = "2",
            username = "ann_baptista",
            walletAddress = "0x2345...6789",
            displayName = stringResource(Res.string.contact_ann_baptista),
        ),
        ContactUser(
            id = "3",
            username = "makenna_t",
            walletAddress = "0x3456...7890",
            displayName = stringResource(Res.string.contact_makenna_t),
        ),
        ContactUser(
            id = "4",
            username = "craig_septimus",
            walletAddress = "0x4567...8901",
            displayName = stringResource(Res.string.contact_craig_septimus),
        ),
    )
