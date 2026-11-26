package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathyushin.doomscroll.model.BehavioralConfig
import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.SessionState
import com.prathyushin.doomscroll.model.ZenDestination
import com.prathyushin.doomscroll.ui.components.DoomEmblem
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

@Composable
fun ZenDashboardScreen(
    config: BehavioralConfig,
    focusEvaluation: FocusEvaluation,
    sessionState: SessionState,
    todayMinutes: Int,
    mutedStoryCount: Int,
    onNavigate: (ZenDestination) -> Unit,
    onUpdateConfig: ((BehavioralConfig) -> BehavioralConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DoomEmblem(modifier = Modifier.size(54.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DOOM SCROLL ZEN",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = if (focusEvaluation.isFocusActive) {
                        "Focus active • ${focusEvaluation.summaryLabel}"
                    } else {
                        "Today: ${todayMinutes}m • Intentional mode"
                    },
                    color = if (focusEvaluation.isFocusActive) ZenColors.Mint else ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Platform Access
        ZenGlassCard(highlighted = true) {
            Text(
                text = "ENVIRONMENTS",
                color = ZenColors.Mint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Open a controlled feed",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (focusEvaluation.strictReelsBlock) {
                    "Reels & Explore restricted • Focus strict mode on"
                } else {
                    "Reels & Explore restricted • DM Reels limited to one"
                },
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(ZenDestination.INSTAGRAM) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenColors.Mint,
                        contentColor = ZenColors.Charcoal
                    )
                ) {
                    Text("Instagram", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onNavigate(ZenDestination.THREADS) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ZenColors.GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenColors.TextPrimary)
                ) {
                    Text("Threads", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Behavioral Controls
        ZenGlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gentle pause reminders",
                        color = ZenColors.TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pause after ${config.sessionLimitMinutes}m or ${config.scrollThreshold} scrolls",
                        color = ZenColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = config.pauseInterventionsEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateConfig { it.copy(pauseInterventionsEnabled = enabled) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ZenColors.Charcoal,
                        checkedTrackColor = ZenColors.Mint
                    )
                )
            }

            if (config.pauseInterventionsEnabled) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Session limit: ${config.sessionLimitMinutes} min",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = config.sessionLimitMinutes.toFloat(),
                    onValueChange = { value ->
                        onUpdateConfig { it.copy(sessionLimitMinutes = value.toInt()) }
                    },
                    valueRange = 5f..60f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenColors.Mint,
                        activeTrackColor = ZenColors.Mint
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Cooldown after continue: ${config.cooldownMinutes} min",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = config.cooldownMinutes.toFloat(),
                    onValueChange = { value ->
                        onUpdateConfig { it.copy(cooldownMinutes = value.toInt()) }
                    },
                    valueRange = 1f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenColors.Mint,
                        activeTrackColor = ZenColors.Mint
                    )
                )
            }

            if (sessionState.sessionSeconds > 0L) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Current session: ${sessionState.sessionSeconds / 60}m ${sessionState.sessionSeconds % 60}s",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Controls & Modules
        ZenGlassCard(padding = 8.dp) {
            DashboardRow(
                title = "Smart Focus Schedule",
                subtitle = if (focusEvaluation.isFocusActive) focusEvaluation.summaryLabel else "Configure quiet hours & strict Reels blocks",
                onClick = { onNavigate(ZenDestination.FOCUS) }
            )
            DashboardRow(
                title = "Story Controls",
                subtitle = if (mutedStoryCount == 0) "No locally muted stories" else "$mutedStoryCount account(s) muted locally",
                onClick = { onNavigate(ZenDestination.STORIES) }
            )
            DashboardRow(
                title = "Notification Filtering",
                subtitle = "Keep DMs & replies, silence engagement noise",
                onClick = { onNavigate(ZenDestination.NOTIFICATIONS) }
            )
            DashboardRow(
                title = "App Selection & Monitoring",
                subtitle = "Choose external apps for pause reminders",
                onClick = { onNavigate(ZenDestination.APPS) }
            )
            DashboardRow(
                title = "Privacy Dashboard",
                subtitle = "Permissions, local data & deletion controls",
                onClick = { onNavigate(ZenDestination.PRIVACY) }
            )
            DashboardRow(
                title = "About Doom Scroll Zen",
                subtitle = "v2.0.0 • Built by Pranav and Prathyush",
                onClick = { onNavigate(ZenDestination.ABOUT) }
            )
        }
    }
}

@Composable
private fun DashboardRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "›",
            color = ZenColors.Mint,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
