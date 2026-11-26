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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.model.NotificationCategory
import com.prathyushin.doomscroll.model.NotificationFilterConfig
import com.prathyushin.doomscroll.model.NotificationStats
import com.prathyushin.doomscroll.permissions.PermissionManager
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

@Composable
fun NotificationFilterScreen(
    config: NotificationFilterConfig,
    stats: NotificationStats,
    onUpdateConfig: (NotificationFilterConfig) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val listenerGranted = PermissionManager.isNotificationListenerEnabled(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(
            title = "Notification Filtering",
            subtitle = "Optional local filtering for official Instagram and Threads app notifications. Processed in memory only.",
            onBack = onBack
        )

        ZenGlassCard(highlighted = listenerGranted && config.masterEnabled) {
            Text(
                text = if (listenerGranted) "Notification access granted" else "Notification access required",
                color = if (listenerGranted) ZenColors.Mint else ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Doom Scroll never stores or uploads notification text.",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            if (!listenerGranted) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { PermissionManager.openNotificationListenerSettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenColors.Mint,
                        contentColor = ZenColors.Charcoal
                    )
                ) {
                    Text("Grant notification access", fontWeight = FontWeight.Bold)
                }
            }
        }

        ZenGlassCard {
            ToggleRow(
                title = "Enable local notification filter",
                subtitle = "Master toggle",
                checked = config.masterEnabled,
                onCheckedChange = { onUpdateConfig(config.copy(masterEnabled = it)) }
            )
            ToggleRow(
                title = "Filter Instagram app",
                subtitle = "com.instagram.android",
                checked = config.filterInstagramApp,
                onCheckedChange = { onUpdateConfig(config.copy(filterInstagramApp = it)) }
            )
            ToggleRow(
                title = "Filter Threads app",
                subtitle = "com.instagram.barcelona",
                checked = config.filterThreadsApp,
                onCheckedChange = { onUpdateConfig(config.copy(filterThreadsApp = it)) }
            )
        }

        ZenGlassCard {
            Text(
                text = "Allowed notification categories",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            NotificationCategory.entries.forEach { category ->
                val enabled = category in config.enabledCategories
                ToggleRow(
                    title = category.title,
                    subtitle = category.description,
                    checked = enabled,
                    onCheckedChange = { checked ->
                        val updated = if (checked) {
                            config.enabledCategories + category
                        } else {
                            config.enabledCategories - category
                        }
                        onUpdateConfig(config.copy(enabledCategories = updated))
                    }
                )
            }
        }

        ZenGlassCard {
            Text(
                text = "Local filter summary",
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Kept: ${stats.keptCount} • Filtered: ${stats.filteredCount}",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 52.dp)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = ZenColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ZenColors.Charcoal,
                checkedTrackColor = ZenColors.Mint
            )
        )
    }
}
