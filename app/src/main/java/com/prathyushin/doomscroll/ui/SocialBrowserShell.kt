package com.prathyushin.doomscroll.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.browser.SocialProvider

private val Paper = Color(0xFFFFF3E6)
private val Ink = Color(0xFF211D1B)
private val Burgundy = Color(0xFF63313A)
private const val PREFS = "doom_scroll_social_preferences"
private const val MUTED_KEY = "muted_story_usernames"

@Composable
fun DoomScrollApp(onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var provider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var mutedUsers by remember {
        mutableStateOf(prefs.getStringSet(MUTED_KEY, emptySet())?.toSet() ?: emptySet())
    }
    var showMuteDialog by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf("") }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler(enabled = canGoBack) {
        browserHost?.goBack()
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Burgundy,
            background = Paper,
            surface = Color(0xFFFFFCF7),
            onSurface = Ink
        )
    ) {
        Column(Modifier.fillMaxSize().background(Paper)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text("DOOM SCROLL", style = MaterialTheme.typography.labelLarge, color = Burgundy)
                    Text("Your time. Your attention.", style = MaterialTheme.typography.titleMedium, color = Ink)
                }
                TextButton(onClick = { showMuteDialog = true }) {
                    Text("Mute Stories · ${mutedUsers.size}", color = Burgundy)
                }
            }

            AndroidView(
                factory = { viewContext ->
                    SocialBrowserHostView(viewContext).also {
                        browserHost = it
                        it.setOnCanGoBackChanged { canGoBack = it }
                        onBrowserHostReady(it)
                        it.setMutedUsernames(mutedUsers)
                        it.showProvider(provider)
                    }
                },
                update = {
                    browserHost = it
                    it.setOnCanGoBackChanged { canGoBack = it }
                    it.setMutedUsernames(mutedUsers)
                    it.showProvider(provider)
                },
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFFFCF7)).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderTab(
                    label = "Instagram",
                    selected = provider == SocialProvider.INSTAGRAM,
                    modifier = Modifier.weight(1f)
                ) { provider = SocialProvider.INSTAGRAM }
                ProviderTab(
                    label = "Threads",
                    selected = provider == SocialProvider.THREADS,
                    modifier = Modifier.weight(1f)
                ) { provider = SocialProvider.THREADS }
            }
        }
    }

    if (showMuteDialog) {
        AlertDialog(
            onDismissRequest = { showMuteDialog = false },
            title = { Text("Hide Story rings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Instagram usernames. This preference stays on this device and only hides matching Story tiles inside Doom Scroll.")
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        singleLine = true,
                        label = { Text("Instagram username") },
                        placeholder = { Text("username") }
                    )
                    Button(
                        onClick = {
                            val name = usernameInput.trim().removePrefix("@").lowercase()
                            if (name.matches(Regex("[a-z0-9._]{1,30}"))) {
                                mutedUsers = mutedUsers + name
                                prefs.edit().putStringSet(MUTED_KEY, mutedUsers.toMutableSet()).apply()
                                browserHost?.setMutedUsernames(mutedUsers)
                                usernameInput = ""
                            }
                        },
                        enabled = usernameInput.trim().removePrefix("@").matches(Regex("[A-Za-z0-9._]{1,30}"))
                    ) { Text("Add username") }
                    if (mutedUsers.isNotEmpty()) {
                        Text("Muted locally", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(Modifier.heightIn(max = 180.dp)) {
                            items(mutedUsers.sorted()) { name ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text("@$name")
                                    TextButton(onClick = {
                                        mutedUsers = mutedUsers - name
                                        prefs.edit().putStringSet(MUTED_KEY, mutedUsers).apply()
                                        browserHost?.setMutedUsernames(mutedUsers)
                                    }) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMuteDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun ProviderTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp)) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp)) {
            Text(label)
        }
    }
}
