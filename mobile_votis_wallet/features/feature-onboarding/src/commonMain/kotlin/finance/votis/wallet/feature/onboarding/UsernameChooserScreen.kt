package finance.votis.wallet.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UsernameChooserScreen(
    userInfo: UserInfo? = null,
    onUsernameSelected: (String) -> Unit = {},
    onSkip: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Choose Username",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Username selection will be implemented with the final UI design.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { onUsernameSelected("test_user") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue with Test Username")
            }
            
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Skip for now")
            }
            
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back")
            }
        }
    }
}
