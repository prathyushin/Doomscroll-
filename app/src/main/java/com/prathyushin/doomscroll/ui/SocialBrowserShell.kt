package com.prathyushin.doomscroll.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
private val AboutBlue = Color(0xFF3978F6)

@Composable
fun DoomScrollApp(onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    var provider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var mutedUsers by remember { mutableStateOf(emptySet<String>()) }
    var selectedStoryUser by remember { mutableStateOf<String?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var dockExpanded by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(true) }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(dockExpanded, dockVisible, isMediaViewer, isFullscreen, showAbout) {
        if (showAbout || isMediaViewer || isFullscreen) {
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

    BackHandler(enabled = showAbout || isFullscreen || canGoBack) {
        when {
            showAbout -> showAbout = false
            isFullscreen -> browserHost?.exitFullscreen()
            else -> browserHost?.goBack()
        }
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
                    .pointerInput(isMediaViewer, isFullscreen, showAbout) {
                        detectTapGestures {
                            if (!isMediaViewer && !isFullscreen && !showAbout) {
                                dockVisible = true
                                dockExpanded = true
                            }
                        }
                    }
            )

            // Translucent floating glass dock; it disappears after inactivity.
            AnimatedVisibility(
                visible = dockVisible && !isMediaViewer && !isFullscreen && !showAbout,
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
                            TextButton(
                                onClick = {
                                    showAbout = true
                                    dockExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "About",
                                    color = TextWhite.copy(alpha = 0.72f),
                                    style = MaterialTheme.typography.labelMedium
                                )
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

    if (showAbout) {
        DoomScrollAboutPage(onClose = { showAbout = false })
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
private fun DoomScrollAboutPage(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF183A70), Color(0xFF11151E), Color(0xFF08090B), Black)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(34.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("D", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        }
                        Text("DOOM SCROLL", color = TextWhite, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconTextButton(label = "×", onClick = onClose)
                }

                Spacer(Modifier.height(34.dp))
                Text(
                    "More Control",
                    color = TextWhite,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your social media. Your rules.",
                    color = TextWhite.copy(alpha = 0.62f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(26.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xE916171A))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    AboutFeatureRow(
                        title = "Instagram + Threads",
                        description = "Keep the platforms you use together in one simple app.",
                        status = "AVAILABLE",
                        available = true
                    )
                    AboutDivider()
                    AboutFeatureRow(
                        title = "Floating Glass Dock",
                        description = "Switch between your spaces with a dock that stays out of the way.",
                        status = "AVAILABLE",
                        available = true
                    )
                    AboutDivider()
                    AboutFeatureRow(
                        title = "Story Controls",
                        description = "Mute selected Stories inside Doom Scroll.",
                        status = "AVAILABLE",
                        available = true
                    )
                    AboutDivider()
                    AboutFeatureRow(
                        title = "Feed Firewall",
                        description = "More control over recommendations, accounts and content categories.",
                        status = "IN DEVELOPMENT",
                        available = false
                    )
                    AboutDivider()
                    AboutFeatureRow(
                        title = "Personal Feed Profiles",
                        description = "Choose how you want your feed to feel, whenever you want.",
                        status = "IN DEVELOPMENT",
                        available = false
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    "Built around your choices. Never a forced dashboard.",
                    color = TextWhite.copy(alpha = 0.62f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AboutBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Back to Doom Scroll", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Independent project • Version 2.0.0 preview",
                    color = TextWhite.copy(alpha = 0.42f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun AboutFeatureRow(
    title: String,
    description: String,
    status: String,
    available: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                color = TextWhite.copy(alpha = 0.58f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Text(
                status,
                color = if (available) Color(0xFF9BD6B0) else Color(0xFF9AB8F8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (available) Color.White else Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (available) "✓" else "＋",
                color = if (available) Color(0xFF111318) else Color(0xFFB8CCF8),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AboutDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.10f))
    )
}

@Composable
private fun IconTextButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Light)
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
