package com.prathyushin.doomscroll.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.browser.SocialProvider
import kotlinx.coroutines.delay

private val PureBlack = Color(0xFF000000)
private val Accent = Color(0xFFB7A7FF)

/** Main browsing surface. The app launches straight into Instagram. */
@Composable
fun DoomScrollApp(onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var selectedProvider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(true) }
    var dockPulse by remember { mutableStateOf(0) }
    var storyUser by remember { mutableStateOf<String?>(null) }
    var mutedUsers by remember { mutableStateOf(emptySet<String>()) }

    BackHandler(enabled = isFullscreen || canGoBack) {
        if (isFullscreen) browserHost?.exitFullscreen() else browserHost?.goBack()
    }

    LaunchedEffect(dockPulse, isMediaViewer, isFullscreen) {
        if (!isMediaViewer && !isFullscreen) {
            delay(3200)
            dockVisible = false
        } else {
            dockVisible = false
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = PureBlack,
            surface = Color(0xFF111114),
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Accent
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(PureBlack)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    SocialBrowserHostView(viewContext).also { host ->
                        browserHost = host
                        host.setOnCanGoBackChanged { canGoBack = it }
                        host.setOnStoryLongPress { username -> storyUser = username }
                        host.setOnMediaViewerChanged { isMediaViewer = it }
                        host.setOnFullscreenChanged { isFullscreen = it }
                        host.setMutedUsernames(mutedUsers)
                        onBrowserHostReady(host)
                    }
                }
            )

            if (!isMediaViewer && !isFullscreen) {
                if (!dockVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(28.dp)
                            .clickable {
                                dockVisible = true
                                dockPulse++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.42f))
                        )
                    }
                }

                AnimatedVisibility(
                    visible = dockVisible,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 22.dp),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE51D1D22), Color(0xD91A1A20), Color(0xE51D1D22))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(28.dp))
                            .padding(horizontal = 9.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformDockItem(
                            symbol = "◎",
                            label = "Instagram",
                            selected = selectedProvider == SocialProvider.INSTAGRAM,
                            onClick = {
                                selectedProvider = SocialProvider.INSTAGRAM
                                browserHost?.showProvider(SocialProvider.INSTAGRAM)
                                dockPulse++
                            }
                        )
                        PlatformDockItem(
                            symbol = "@",
                            label = "Threads",
                            selected = selectedProvider == SocialProvider.THREADS,
                            onClick = {
                                selectedProvider = SocialProvider.THREADS
                                browserHost?.showProvider(SocialProvider.THREADS)
                                dockPulse++
                            }
                        )
                    }
                }
            }
        }

        storyUser?.let { username ->
            val isMuted = mutedUsers.contains(username)
            AlertDialog(
                onDismissRequest = { storyUser = null },
                containerColor = Color(0xFF19191E),
                titleContentColor = Color.White,
                textContentColor = Color.White.copy(alpha = 0.78f),
                title = { Text(if (isMuted) "Story hidden" else "Hide this Story?") },
                text = {
                    Text(
                        if (isMuted) "@$username is currently hidden in this Doom Scroll session."
                        else "Hide @${username}'s Story in this Doom Scroll session? This does not change Instagram's account settings."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            mutedUsers = if (isMuted) mutedUsers - username else mutedUsers + username
                            browserHost?.setMutedUsernames(mutedUsers)
                            storyUser = null
                        }
                    ) { Text(if (isMuted) "Restore Story" else "Hide Story") }
                },
                dismissButton = {
                    TextButton(onClick = { storyUser = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun PlatformDockItem(
    symbol: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick),
        color = if (selected) Color.White.copy(alpha = 0.13f) else Color.Transparent,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(symbol, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Text(
                label,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
