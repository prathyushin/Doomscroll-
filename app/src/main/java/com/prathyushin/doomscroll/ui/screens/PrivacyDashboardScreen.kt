package com.prathyushin.doomscroll.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.model.LocalDataFootprint
import com.prathyushin.doomscroll.model.SessionState
import com.prathyushin.doomscroll.permissions.PermissionManager
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

@Composable
fun PrivacyDashboardScreen(
    footprint: LocalDataFootprint,
    sessionState: SessionState,
    onClearUsageHistory: () -> Unit,
    onClearMutedStories: () -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val permissions = PermissionManager.buildStatusList(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = "Privacy Dashboard",
            subtitle = "No Doom Scroll account. No cloud tracking. No advertising profiles. No DM content storage.",
            onBack = onBack
        )

        ZenGlassCard(highlighted = true) {
            Text(
                text = "LOCAL-ONLY ARCHITECTURE",
                color = ZenColors.Mint,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            PrivacyFactRow("Doom Scroll account", "None")
            PrivacyFactRow("Cloud telemetry", "Disabled")
            PrivacyFactRow("DM content storage", "Never stored")
            PrivacyFactRow("Notification text", "In-memory only")
            PrivacyFactRow("Session tracking", sessionState.state.name.lowercase().replace('_', ' '))
        }

        ZenGlassCard {
            Text(
                text = "Permissions",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            permissions.forEach { perm ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perm.title,
                            color = ZenColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (perm.granted) "Granted" else "Off",
                            color = if (perm.granted) ZenColors.Mint else ZenColors.TextMuted,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = perm.purpose,
                        color = ZenColors.TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { PermissionManager.openAppDetailsSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ZenColors.GlassBorder)
            ) {
                Text("Manage Android app permissions", color = ZenColors.TextPrimary)
            }
        }

        ZenGlassCard {
            Text(
                text = "Stored on this device",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            PrivacyFactRow("Today's usage", "${footprint.todayMinutes} min")
            PrivacyFactRow("7-day usage", "${footprint.weekMinutes} min")
            PrivacyFactRow("Muted Story accounts", "${footprint.mutedStoryCount}")
            PrivacyFactRow("Focus schedules", "${footprint.focusScheduleCount}")
            PrivacyFactRow("Monitored external apps", "${footprint.monitoredAppCount}")

            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onClearUsageHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ZenColors.GlassBorder)
                ) {
                    Text("Clear usage history", color = ZenColors.TextPrimary)
                }
                OutlinedButton(
                    onClick = onClearMutedStories,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ZenColors.GlassBorder)
                ) {
                    Text("Clear muted stories", color = ZenColors.TextPrimary)
                }
                OutlinedButton(
                    onClick = onClearAllData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ZenColors.Danger.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenColors.Danger)
                ) {
                    Text("Reset all local data", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PrivacyFactRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = ZenColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = ZenColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
