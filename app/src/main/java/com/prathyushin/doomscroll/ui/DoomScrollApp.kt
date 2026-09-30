package com.prathyushin.doomscroll.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.model.AppPolicy

@Composable
fun DoomScrollApp() {
    val context = LocalContext.current
    val store = remember { LocalPolicyStore(context) }
    var sessionLimit by remember { mutableStateOf(store.sessionLimitMinutes().toFloat()) }
    var cooldown by remember { mutableStateOf(store.cooldownMinutes().toFloat()) }
    var selected by remember { mutableStateOf(store.policies()) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(Ivory).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("Doom Scroll", style = MaterialTheme.typography.headlineLarge)
            Text("A local pause system for intentional digital use.", style = MaterialTheme.typography.bodyLarge)

            OutlinedButton(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enable Doom Scroll in Accessibility") }

            Text("Pause after ${{sessionLimit.toInt()} minutes", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = sessionLimit,
                onValueChange = { sessionLimit = it },
                valueRange = 5f..60f,
                onValueChangeFinished = { store.setSessionLimitMinutes(sessionLimit.toInt()) }
            )

            Text("Cooldown: ${{cooldown.toInt()} minutes", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = cooldown,
                onValueChange = { cooldown = it },
                valueRange = 1f..30f,
                onValueChangeFinished = { store.setCooldownMinutes(cooldown.toInt()) }
            )

            Text("Protected apps", style = MaterialTheme.typography.titleLarge)
            AppPicker(
                context = context,
                selected = selected,
                onToggle = { packageName ->
                    store.setEnabled(packageName, !selected.contains(packageName))
                    selected = store.policies()
                }
            )

            Spacer(Modifier.weight(1f))
            Text(
                "Behavior signals stay on this device. Doom Scroll does not collect passwords, message content, or remote behavioral telemetry.",
                style = MaterialTheme.typography.bodySmall
            )
        }
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
        modifier = Modifier.fillMaxWidth().height(220.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.displayName)
                Switch(checked = selected.contains(app.packageName), onCheckedChange = { onToggle(app.packageName) })
            }
        }
    }
}

private val Ivory = Color(0xFFFFF3E6)
