package com.prathyushin.doomscroll.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.browser.SocialProvider
import kotlinx.coroutines.delay

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
    var selectedStoryUser by remember { mutableStateOf<String?>(null) }
    var dockExpanded by remember { mutableStateOf(false) }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(dockExpanded) {
        if (dockExpanded) {
            delay(4200)
            dockExpanded = false
        }
    }

    BackHandler(enabled = isFullscreen || canGoBack) {
        if (isFullscreen) browserHost?.exitFullscreen() else browserHost?.goBack()
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Burgundy,
            background = Paper,
            surface = Color(0xFFFFFCF7),
            onSurface = Ink
        )
    ) {
        Box(Modifier.fillMaxSize().background(Paper).windowInsetsPadding(WindowInsets.systemBars)) {
            AndroidView(
                factory = { viewContext ->
                    SocialBrowserHostView(viewContext).also { host ->
                        browserHost = host
                        host.setOnCanGoBackChanged { canGoBack = it }
                        host.setOnStoryLongPress { username -> selectedStoryUser = username }
                        host.setOnMediaViewerChanged { isMediaViewer = it }
                        host.setOnFullscreenChanged { isFullscreen = it }
                        onBrowserHostReady(host)
                        host.setMutedUsernames(mutedUsers)
                        host.showProvider(provider)
                    }
                },
                update = { host ->
                    browserHost = host
                    host.setOnCanGoBackChanged { canGoBack = it }
                    host.setOnStoryLongPress { username -> selectedStoryUser = username }
                    host.setOnMediaViewerChanged { isMediaViewer = it }
                    host.setOnFullscreenChanged { isFullscreen = it }
                    host.setMutedUsernames(mutedUsers)
                    host.showProvider(provider)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Small floating handle stays available; the full switcher appears on demand.
            AnimatedVisibility(
                visible = !isMediaViewer && !isFullscreen,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .background(Color(0xFFFDF9F4), RoundedCornerShape(28.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(
                    visible = dockExpanded,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DockProvider("Instagram", provider == SocialProvider.INSTAGRAM) {
                            provider = SocialProvider.INSTAGRAM
                            dockExpanded = false
                        }
                        DockProvider("Threads", provider == SocialProvider.THREADS) {
                            provider = SocialProvider.THREADS
                            dockExpanded = false
                        }
                    }
                }
                FilledIconButton(
                    onClick = { dockExpanded = !dockExpanded },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Burgundy,
                        contentColor = Color.White
                    )
                ) {
                    Text("↔", style = MaterialTheme.typography.titleLarge, color = Color.White)
                }
            }
            }
        }
    }

    selectedStoryUser?.let { username ->
        val isMuted = username in mutedUsers
        AlertDialog(
            onDismissRequest = { selectedStoryUser = null },
            title = { Text(if (isMuted) "Unmute story?" else "Mute this story?") },
            text = {
                Text(
                    if (isMuted) "Stories from @$username will appear again inside Doom Scroll."
                    else "Hide @$username's Story from the Story tray inside Doom Scroll. This does not unfollow or change the Instagram account."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mutedUsers = if (isMuted) mutedUsers - username else mutedUsers + username
                    prefs.edit().putStringSet(MUTED_KEY, mutedUsers.toMutableSet()).apply()
                    browserHost?.setMutedUsernames(mutedUsers)
                    selectedStoryUser = null
                }) { Text(if (isMuted) "Unmute" else "Mute") }
            },
            dismissButton = {
                TextButton(onClick = { selectedStoryUser = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DockProvider(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) { Text(label) }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) { Text(label) }
    }
}
