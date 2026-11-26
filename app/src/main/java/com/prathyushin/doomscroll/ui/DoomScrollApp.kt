package com.prathyushin.doomscroll.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.model.AppPolicy

private val Ink = Color(0xFF101114)
private val Panel = Color(0xFF1B1A1F)
private val Muted = Color(0xFFAAA6B0)
private val Accent = Color(0xFFB8F2D0)
private val AccentDark = Color(0xFF183A2A)

@Composable
fun DoomScrollApp() {
    val context = LocalContext.current
    val store = remember { LocalPolicyStore(context) }
    var sessionLimit by remember { mutableStateOf(store.sessionLimitMinutes().toFloat()) }
    var cooldown by remember { mutableStateOf(store.cooldownMinutes().toFloat()) }
    var selected by remember { mutableStateOf(store.policies()) }
    var aboutExpanded by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Ink, surface = Panel, primary = Accent, secondary = Muted,
            onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Column(
            Modifier.fillMaxSize().background(Ink).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                DoomEmblem(Modifier.size(58.dp))
                Column {
                    Text("DOOM SCROLL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, letterSpacing = 1.6f)
                    Text("Make the scroll intentional.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Panel).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("YOUR FOCUS", color = Accent, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.2f)
                            Spacer(Modifier.height(6.dp))
                            Text("Pause with purpose", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Choose when a reminder should appear.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        DoomEmblem(Modifier.size(72.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Open Accessibility settings", fontWeight = FontWeight.Bold) }
                }

                SettingsCard(title = "Session reminder", subtitle = "Remind me after ${sessionLimit.toInt()} minutes") {
                    Slider(
                        value = sessionLimit,
                        onValueChange = { sessionLimit = it },
                        valueRange = 5f..60f,
                        steps = 10,
                        onValueChangeFinished = { store.setSessionLimitMinutes(sessionLimit.toInt()) }
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("5 min", color = Muted, style = MaterialTheme.typography.labelSmall)
                        Text("60 min", color = Muted, style = MaterialTheme.typography.labelSmall)
                    }
                }

                SettingsCard(title = "Reminder cooldown", subtitle = "Wait ${cooldown.toInt()} minutes before another pause") {
                    Slider(
                        value = cooldown,
                        onValueChange = { cooldown = it },
                        valueRange = 1f..30f,
                        steps = 28,
                        onValueChangeFinished = { store.setCooldownMinutes(cooldown.toInt()) }
                    )
                }

                SettingsCard(title = "Protected apps", subtitle = "Pick apps where Doom Scroll can offer a pause") {
                    AppPicker(
                        context = context,
                        selected = selected,
                        onToggle = { packageName ->
                            store.setEnabled(packageName, !selected.contains(packageName))
                            selected = store.policies()
                        }
                    )
                }

                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
                    Text("PRIVACY, BY DESIGN", color = Accent, fontWeight = FontWeight.Bold, letterSpacing = 1f)
                    Spacer(Modifier.height(8.dp))
                    Text("Your app-selection and reminder settings stay on this device. Doom Scroll does not send behavior data to a server.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }

                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
                    TextButton(onClick = { aboutExpanded = !aboutExpanded }) {
                        Text(if (aboutExpanded) "Hide about" else "About Doom Scroll", color = Accent)
                    }
                    if (aboutExpanded) {
                        Text("A small tool to help make digital time more deliberate.", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("Built by Pranav and Prathyush", color = Muted, style = MaterialTheme.typography.bodySmall)
                        Text("Version 2.0.0", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Panel).padding(18.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun DoomEmblem(modifier: Modifier = Modifier) {
    Canvas(modifier.clip(CircleShape).background(AccentDark).border(1.dp, Accent.copy(alpha = .65f), CircleShape)) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * .37f
        drawCircle(color = Accent, radius = radius, center = center, style = Stroke(width = size.minDimension * .055f))
        drawLine(Accent, androidx.compose.ui.geometry.Offset(center.x, center.y - radius * .55f), androidx.compose.ui.geometry.Offset(center.x, center.y + radius * .55f), strokeWidth = size.minDimension * .075f, cap = StrokeCap.Round)
        drawLine(Accent, androidx.compose.ui.geometry.Offset(center.x - radius * .35f, center.y - radius * .25f), androidx.compose.ui.geometry.Offset(center.x + radius * .35f, center.y - radius * .25f), strokeWidth = size.minDimension * .075f, cap = StrokeCap.Round)
        drawLine(Accent, androidx.compose.ui.geometry.Offset(center.x - radius * .35f, center.y + radius * .25f), androidx.compose.ui.geometry.Offset(center.x + radius * .35f, center.y + radius * .25f), strokeWidth = size.minDimension * .075f, cap = StrokeCap.Round)
    }
}

@Composable
private fun AppPicker(context: Context, selected: Set<String>, onToggle: (String) -> Unit) {
    val apps = remember(context) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { AppPolicy(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
            .take(80)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(app.displayName, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = selected.contains(app.packageName), onCheckedChange = { onToggle(app.packageName) })
            }
        }
    }
}
