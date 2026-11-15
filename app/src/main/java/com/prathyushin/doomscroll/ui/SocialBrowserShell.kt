package com.prathyushin.doomscroll.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.browser.SocialProvider
import kotlinx.coroutines.delay

private val Black = Color(0xFF000000)
private val SurfaceBlack = Color(0xFF0A0A0A)
private val ElevatedBlack = Color(0xFF171717)
private val TextWhite = Color(0xFFF5F5F5)
private val Accent = Color(0xFFB7A0A4)

@Composable
fun DoomScrollApp(isAppForeground: Boolean, onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var provider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var mutedUsers by remember { mutableStateOf(emptySet<String>()) }
    var selectedStoryUser by remember { mutableStateOf<String?>(null) }
    var dockExpanded by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(true) }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    LaunchedEffect(dockExpanded, dockVisible, isMediaViewer, isFullscreen) {
        if (isMediaViewer || isFullscreen) {
            dockExpanded = false
            dockVisible = false
        } else if (dockExpanded) {
            dockVisible = true
            delay(4200)
            dockExpanded = false
            dockVisible = false
        } else if (dockVisible) {
            delay(2200)
            dockVisible = false
        }
    }

    BackHandler(enabled = isFullscreen || canGoBack) {
        if (isFullscreen) browserHost?.exitFullscreen() else browserHost?.goBack()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Black,
            background = Black,
            surface = SurfaceBlack,
            surfaceVariant = ElevatedBlack,
            onSurface = TextWhite,
            onBackground = TextWhite
        )
    ) {
        Box(Modifier.fillMaxSize().background(Black).windowInsetsPadding(WindowInsets.systemBars)) {
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

            // A narrow bottom-edge gesture reveals the dock after it auto-hides.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(14.dp)
                    .pointerInput(isMediaViewer, isFullscreen) {
                        detectTapGestures {
                            if (!isMediaViewer && !isFullscreen) {
                                dockVisible = true
                                dockExpanded = true
                            }
                        }
                    }
            )

            // Translucent floating glass dock; it disappears after inactivity.
            AnimatedVisibility(
                visible = dockVisible && !isMediaViewer && !isFullscreen,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 22.dp)
                    .shadow(22.dp, RoundedCornerShape(30.dp))
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xD92A2A2A), Color(0xA6090909))),
                        RoundedCornerShape(30.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(30.dp))
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
                            dockVisible = false
                        }
                        DockProvider("Threads", provider == SocialProvider.THREADS) {
                            provider = SocialProvider.THREADS
                            dockExpanded = false
                            dockVisible = false
                        }
                    }
                }
                FilledIconButton(
                    onClick = {
                        dockVisible = true
                        dockExpanded = !dockExpanded
                    },
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Accent,
                        contentColor = Black
                    )
                ) {
                    Text("↔", style = MaterialTheme.typography.titleLarge, color = Black)
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
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Black),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) { Text(label, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium) }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) { Text(label) }
    }
}
