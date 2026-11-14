package com.prathyushin.doomscroll.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.model.AppPolicy

private val Paper = Color(0xFFFFF3E6)
private val CardPaper = Color(0xFFFFFCF7)
private val Ink = Color(0xFF211D1B)
private val MutedInk = Color(0xFF6F625B)
private val Burgundy = Color(0xFF63313A)

@Composable
fun DoomScrollApp() {
    val context = LocalContext.current
    val store = remember { LocalPolicyStore(context) }
    var sessionLimit by remember { mutableStateOf(store.sessionLimitMinutes().toFloat()) }
    var cooldown by remember { mutableStateOf(store.cooldownMinutes().toFloat()) }
    var selected by remember { mutableStateOf(store.policies()) }
    var disclosureAccepted by remember { mutableStateOf(store.disclosureAccepted()) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Burgundy,
            onPrimary = Color.White,
            background = Paper,
            surface = CardPaper,
            onSurface = Ink
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Paper) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DOOM SCROLL", style = MaterialTheme.typography.labelLarge, color = Burgundy, fontWeight = FontWeight.Bold)
                    Text("Your time. Your attention. Your control.", style = MaterialTheme.typography.headlineSmall, color = Ink, fontWeight = FontWeight.SemiBold)
                    Text("A quiet pause for the moments when scrolling takes over.", style = MaterialTheme.typography.bodyMedium, color = MutedInk)
                }

                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardPaper)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Your protection", style = MaterialTheme.typography.titleLarge, color = Ink)
                        Text(
                            if (selected.isEmpty()) "Choose the apps where you want more control."
                            else selected.size.toString() + " app(s) selected for intentional-use pauses.",
                            style = MaterialTheme.typography.bodyMedium, color = MutedInk
                        )
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (disclosureAccepted) "Manage Accessibility permission" else "Review permission and continue") }
                        if (!disclosureAccepted) {
                            Text("Read the disclosure below and confirm that you understand how Android Accessibility is used.", style = MaterialTheme.typography.bodySmall, color = MutedInk)
                        }
                    }
                }

                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardPaper)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pause settings", style = MaterialTheme.typography.titleLarge, color = Ink)
                        Text("Session limit · " + sessionLimit.toInt() + " minutes", color = Ink)
                        Slider(value = sessionLimit, onValueChange = { sessionLimit = it }, valueRange = 5f..60f,
                            onValueChangeFinished = { store.setSessionLimitMinutes(sessionLimit.toInt()) })
                        Text("Cooldown · " + cooldown.toInt() + " minutes", color = Ink)
                        Slider(value = cooldown, onValueChange = { cooldown = it }, valueRange = 1f..30f,
                            onValueChangeFinished = { store.setCooldownMinutes(cooldown.toInt()) })
                        Text("When a threshold is reached, Doom Scroll asks you to pause for 15 seconds before continuing intentionally.",
                            style = MaterialTheme.typography.bodySmall, color = MutedInk)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    Text("Choose protected apps", style = MaterialTheme.typography.titleLarge, color = Ink)
                    AppPicker(context = context, selected = selected, onToggle = { packageName, displayName ->
                        store.setEnabled(packageName, !selected.contains(packageName), displayName)
                        selected = store.policies()
                    })
                }

                if (!disclosureAccepted) {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E3DF))) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Accessibility disclosure", style = MaterialTheme.typography.titleMedium, color = Ink)
                            Text(
                                "Doom Scroll uses Android AccessibilityService only to identify selected-app session changes and scroll-event frequency, then show a pause screen. It does not read screen text, messages, passwords or typed content; it does not perform taps or swipes; and it does not send behavioral data to a server.",
                                style = MaterialTheme.typography.bodySmall, color = MutedInk
                            )
                            Button(onClick = {
                                store.setDisclosureAccepted(true)
                                disclosureAccepted = true
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            }, modifier = Modifier.fillMaxWidth()) { Text("I understand — open Accessibility settings") }
                        }
                    }
                }

                Text("Private by design · Behavior signals and settings stay on this device.",
                    style = MaterialTheme.typography.labelSmall, color = MutedInk)
            }
        }
    }
}

@Composable
private fun AppPicker(context: Context, selected: Set<String>, onToggle: (String, String) -> Unit) {
    val apps = remember(context) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .map { AppPolicy(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
    }
    var search by remember { mutableStateOf("") }
    val visibleApps = remember(apps, search) {
        if (search.isBlank()) apps else apps.filter {
            it.displayName.contains(search, ignoreCase = true) || it.packageName.contains(search, ignoreCase = true)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, label = { Text("Search installed apps") })
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(visibleApps, key = { it.packageName }) { app ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.displayName, color = Ink)
                        Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MutedInk)
                    }
                    Switch(checked = selected.contains(app.packageName), onCheckedChange = { onToggle(app.packageName, app.displayName) })
                }
            }
            if (visibleApps.isEmpty()) {
                item { Text("No matching apps found.", color = MutedInk, modifier = Modifier.padding(12.dp)) }
            }
        }
    }
}
