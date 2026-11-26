package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.model.MutedStoryAccount
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

@Composable
fun StoryControlsScreen(
    mutedStories: List<MutedStoryAccount>,
    onMuteAccount: (String) -> Unit,
    onUnmuteAccount: (String) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = "Story Controls",
            subtitle = "Mute individual Stories locally inside Doom Scroll. Your Instagram account settings are never modified.",
            onBack = onBack
        )

        ZenGlassCard {
            Text(
                text = "Mute a Story account",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                placeholder = { Text("@username", color = ZenColors.TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZenColors.Mint,
                    unfocusedBorderColor = ZenColors.GlassBorder,
                    focusedTextColor = ZenColors.TextPrimary,
                    unfocusedTextColor = ZenColors.TextPrimary
                )
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (usernameInput.isNotBlank()) {
                        onMuteAccount(usernameInput)
                        usernameInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZenColors.Mint,
                    contentColor = ZenColors.Charcoal
                )
            ) {
                Text("Mute locally", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tip: You can also long-press a Story tile in Instagram to mute or unmute it.",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        ZenGlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Locally muted (${mutedStories.size})",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (mutedStories.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("Unmute all", color = ZenColors.Mint)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (mutedStories.isEmpty()) {
                Text(
                    text = "No accounts are locally muted.",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                mutedStories.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${item.username}",
                            color = ZenColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onUnmuteAccount(item.username) }) {
                            Text("Unmute", color = ZenColors.Mint)
                        }
                    }
                }
            }
        }
    }
}
