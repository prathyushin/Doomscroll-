package com.prathyushin.doomscroll.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.prathyushin.doomscroll.permissions.PermissionManager
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard

private data class InstalledAppEntry(
    val packageName: String,
    val displayName: String,
    val icon: ImageBitmap?
)

@Composable
fun AppSelectionScreen(
    selectedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityEnabled = PermissionManager.isAccessibilityEnabled(context)
    var searchQuery by remember { mutableStateOf("") }

    val installedApps = remember(context) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val label = info.loadLabel(pm).toString()
                val bmp = runCatching {
                    info.loadIcon(pm).toBitmap(width = 96, height = 96).asImageBitmap()
                }.getOrNull()
                InstalledAppEntry(pkg, label, bmp)
            }
            .sortedBy { it.displayName.lowercase() }
    }

    val filteredApps = remember(installedApps, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) installedApps
        else installedApps.filter {
            it.displayName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            title = "App Selection",
            subtitle = "Select installed apps where Doom Scroll can offer gentle pause reminders.",
            onBack = onBack
        )

        ZenGlassCard(highlighted = accessibilityEnabled) {
            Text(
                text = if (accessibilityEnabled) "Accessibility monitoring active" else "Accessibility service is off",
                color = if (accessibilityEnabled) ZenColors.Mint else ZenColors.TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Only monitors apps you select below. Never reads message contents.",
                color = ZenColors.TextMuted,
                style = MaterialTheme.typography.bodySmall
            )
            if (!accessibilityEnabled) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { PermissionManager.openAccessibilitySettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenColors.Mint,
                        contentColor = ZenColors.Charcoal
                    )
                ) {
                    Text("Open Accessibility settings", fontWeight = FontWeight.Bold)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps…", color = ZenColors.TextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZenColors.Mint,
                unfocusedBorderColor = ZenColors.GlassBorder,
                focusedTextColor = ZenColors.TextPrimary,
                unfocusedTextColor = ZenColors.TextPrimary
            )
        )

        ZenGlassCard(
            modifier = Modifier.weight(1f),
            padding = 12.dp
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = "${app.displayName} icon",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.displayName,
                                color = ZenColors.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = app.packageName,
                                color = ZenColors.TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Switch(
                            checked = app.packageName in selectedPackages,
                            onCheckedChange = { onTogglePackage(app.packageName) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ZenColors.Charcoal,
                                checkedTrackColor = ZenColors.Mint
                            )
                        )
                    }
                }
            }
        }
    }
}
