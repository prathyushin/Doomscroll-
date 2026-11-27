package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.prathyushin.doomscroll.ui.theme.ZenGlassMetricPill
import com.prathyushin.doomscroll.ui.theme.ZenGlassPillRow

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
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Identity & Quick Launch Glass Card
        ZenGlassCard(cornerRadius = 30.dp, padding = 20.dp, highlighted = true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DoomEmblem(modifier = Modifier.size(54.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Doom Scroll",
                            color = ZenColors.TextPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ZenColors.Mint.copy(alpha = 0.14f))
                                .border(1.dp, ZenColors.Mint.copy(alpha = 0.30f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v2.1.3",
                                color = ZenColors.Mint,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (focusEvaluation.isFocusActive) {
                            "Focus active • ${focusEvaluation.summaryLabel}"
                        } else {
                            "Reels restricted • Search & DMs open"
                        },
                        color = if (focusEvaluation.isFocusActive) ZenColors.Mint else ZenColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onNavigate(ZenDestination.INSTAGRAM) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ZenColors.Mint,
                    contentColor = ZenColors.Charcoal
                )
            ) {
                Text("Open Instagram", fontWeight = FontWeight.Bold)
            }
        }

        // 3-Up Frosted Metric Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ZenGlassMetricPill(
                label = "Today",
                value = "${todayMinutes}m",
                modifier = Modifier.weight(1f)
            )
            ZenGlassMetricPill(
                label = "Limit",
                value = "${config.sessionLimitMinutes}m",
                modifier = Modifier.weight(1f)
            )
            ZenGlassMetricPill(
                label = "Focus",
                value = if (focusEvaluation.isFocusActive) "On" else "Off",
                accent = focusEvaluation.isFocusActive,
                modifier = Modifier.weight(1f)
            )
        }

        // Behavioral Controls Card
        ZenGlassCard(cornerRadius = 28.dp, padding = 18.dp) {
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
                        checkedTrackColor = ZenColors.Mint,
                        uncheckedThumbColor = ZenColors.TextMuted,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }

            if (config.pauseInterventionsEnabled) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Session limit: ${config.sessionLimitMinutes} min",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
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
                        activeTrackColor = ZenColors.Mint,
                        inactiveTrackColor = Color.White.copy(alpha = 0.14f)
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cooldown after continue: ${config.cooldownMinutes} min",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
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
                        activeTrackColor = ZenColors.Mint,
                        inactiveTrackColor = Color.White.copy(alpha = 0.14f)
                    )
                )
            }

            if (sessionState.sessionSeconds > 0L) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Current session: ${sessionState.sessionSeconds / 60}m ${sessionState.sessionSeconds % 60}s",
                    color = ZenColors.Mint,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // Section: Controls (individual frosted capsule rows)
        Text(
            text = "CONTROLS",
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ZenGlassPillRow(
                title = "Smart Focus Schedule",
                subtitle = if (focusEvaluation.isFocusActive) focusEvaluation.summaryLabel else "Quiet hours & strict Reels blocks",
                badgeGlyph = "◐",
                onClick = { onNavigate(ZenDestination.FOCUS) }
            )
            ZenGlassPillRow(
                title = "Story Controls",
                subtitle = if (mutedStoryCount == 0) "No locally muted stories" else "$mutedStoryCount account(s) muted locally",
                badgeGlyph = "◎",
                onClick = { onNavigate(ZenDestination.STORIES) }
            )
            ZenGlassPillRow(
                title = "Notification Preferences",
                subtitle = "Local category preferences",
                badgeGlyph = "◌",
                onClick = { onNavigate(ZenDestination.NOTIFICATIONS) }
            )
        }

        // Section: Privacy & App
        Text(
            text = "PRIVACY & APP",
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ZenGlassPillRow(
                title = "Privacy Dashboard",
                subtitle = "Permissions, local data & deletion",
                badgeGlyph = "◈",
                onClick = { onNavigate(ZenDestination.PRIVACY) }
            )
            ZenGlassPillRow(
                title = "About Doom Scroll",
                subtitle = "v2.1.3 Zen • Built by Pranav and Prathyush",
                badgeGlyph = "✦",
                onClick = { onNavigate(ZenDestination.ABOUT) }
            )
        }
    }
}
