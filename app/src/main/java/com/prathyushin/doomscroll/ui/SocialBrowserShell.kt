package com.prathyushin.doomscroll.ui

import androidx.activity.compose.BackHandler
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.prathyushin.doomscroll.feed.DoomScrollPreferences
import com.prathyushin.doomscroll.feed.DoomScrollPreferencesStore
import com.prathyushin.doomscroll.feed.FeedProfile
import com.prathyushin.doomscroll.feed.FeedFirewallSettings
import com.prathyushin.doomscroll.feed.LearningFocusMode
import kotlinx.coroutines.delay

private val Black = Color(0xFF000000)
private val SurfaceBlack = Color(0xFF0A0A0A)
private val ElevatedBlack = Color(0xFF171717)
private val TextWhite = Color(0xFFF5F5F5)
private val Accent = Color(0xFFB7A0A4)
private val AboutBlue = Color(0xFF3978F6)

@Composable
fun DoomScrollApp(onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    val context = LocalContext.current
    val preferencesStore = remember { DoomScrollPreferencesStore(context) }
    var preferences by remember { mutableStateOf(preferencesStore.load()) }
    var provider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var mutedUsers by remember { mutableStateOf(emptySet<String>()) }
    var selectedStoryUser by remember { mutableStateOf<String?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(false) }
    var showSessionReminder by remember { mutableStateOf(false) }
    var dockExpanded by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(true) }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    LaunchedEffect(preferences) {
        preferencesStore.save(preferences)
        browserHost?.setFeedFirewallSettings(SocialProvider.INSTAGRAM, preferences.instagram)
        browserHost?.setFeedFirewallSettings(SocialProvider.THREADS, preferences.threads)
        browserHost?.setLearningFocusSettings(preferences.learning)
    }

    LaunchedEffect(preferences.focus.sessionLimitMinutes) {
        val minutes = preferences.focus.sessionLimitMinutes
        if (minutes > 0) {
            delay(minutes * 60_000L)
            showSessionReminder = true
        }
    }

    LaunchedEffect(dockExpanded, dockVisible, isMediaViewer, isFullscreen, showAbout, showControls) {
        if (showAbout || showControls || isMediaViewer || isFullscreen) {
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

    BackHandler(enabled = showAbout || showControls || isFullscreen || canGoBack) {
        when {
            showAbout -> showAbout = false
            showControls -> showControls = false
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
        CompositionLocalProvider(
            LocalDensity provides LocalDensity.current.copy(
                fontScale = if (preferences.comfort.largerText) 1.15f else LocalDensity.current.fontScale
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
                        host.setFeedFirewallSettings(SocialProvider.INSTAGRAM, preferences.instagram)
                        host.setFeedFirewallSettings(SocialProvider.THREADS, preferences.threads)
                        host.setLearningFocusSettings(preferences.learning)
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
                    host.setFeedFirewallSettings(SocialProvider.INSTAGRAM, preferences.instagram)
                    host.setFeedFirewallSettings(SocialProvider.THREADS, preferences.threads)
                    host.setLearningFocusSettings(preferences.learning)
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
                            if (!isMediaViewer && !isFullscreen && !showAbout && !showControls) {
                                dockVisible = true
                                dockExpanded = true
                            }
                        }
                    }
            )

            // Translucent floating glass dock; it disappears after inactivity.
            AnimatedVisibility(
                visible = dockVisible && !isMediaViewer && !isFullscreen && !showAbout && !showControls,
                modifier = Modifier.align(if (preferences.comfort.oneHandedDock) Alignment.BottomEnd else Alignment.BottomCenter)
                    .padding(end = if (preferences.comfort.oneHandedDock) 12.dp else 0.dp),
                enter = if (preferences.comfort.reducedMotion) EnterTransition.None else fadeIn() + slideInVertically { it / 2 },
                exit = if (preferences.comfort.reducedMotion) ExitTransition.None else fadeOut() + slideOutVertically { it / 2 }
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
                                    showControls = true
                                    dockExpanded = false
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) { Text("Controls", color = TextWhite.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium) }
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
    }

    if (showAbout) {
        DoomScrollAboutPage(onClose = { showAbout = false })
    }
    if (showControls) {
        DoomScrollControlsPage(
            preferences = preferences,
            onPreferencesChange = { preferences = it },
            onClose = { showControls = false }
        )
    }
    if (showSessionReminder) {
        AlertDialog(
            onDismissRequest = { showSessionReminder = false },
            title = { Text("Your session reminder") },
            text = { Text("You set a ${preferences.focus.sessionLimitMinutes}-minute reminder. Take a moment to decide whether you want to continue.") },
            confirmButton = { TextButton(onClick = { showSessionReminder = false }) { Text("Continue") } },
            dismissButton = { TextButton(onClick = { showSessionReminder = false }) { Text("Close reminder") } }
        )
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
private fun DoomScrollControlsPage(
    preferences: DoomScrollPreferences,
    onPreferencesChange: (DoomScrollPreferences) -> Unit,
    onClose: () -> Unit
) {
    var platform by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var accountDraft by remember { mutableStateOf("") }
    var topicDraft by remember { mutableStateOf("") }
    val context = LocalContext.current
    val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    val microphoneGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    val settings = if (platform == SocialProvider.INSTAGRAM) preferences.instagram else preferences.threads
    fun updateSettings(next: FeedFirewallSettings) {
        onPreferencesChange(
            if (platform == SocialProvider.INSTAGRAM) preferences.copy(instagram = next.normalized())
            else preferences.copy(threads = next.normalized())
        )
    }
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(12.dp)
                .clip(RoundedCornerShape(30.dp)).background(Color(0xFF101216))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(30.dp))
                .verticalScroll(rememberScrollState()).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your Controls", color = TextWhite, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                    Text("Local preferences for your social feeds", color = TextWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                IconTextButton("×", onClose)
            }
            Text("FEED PROFILE", color = Color(0xFF9AB8F8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            FeedProfile.values().forEach { profile ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(if (preferences.profile == profile) Color(0xFF253A60) else Color(0xFF1B1E24))
                        .clickable {
                            onPreferencesChange(preferences.copy(
                                profile = profile,
                                instagram = profile.defaults,
                                threads = profile.defaults
                            ))
                        }.padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(profile.title, color = TextWhite, fontWeight = FontWeight.SemiBold)
                        Text(profile.description, color = TextWhite.copy(alpha = 0.58f), fontSize = 11.sp)
                    }
                    RadioButton(selected = preferences.profile == profile, onClick = {
                        onPreferencesChange(preferences.copy(profile = profile, instagram = profile.defaults, threads = profile.defaults))
                    })
                }
            }
            Divider(color = Color.White.copy(alpha = 0.12f))
            Text("FEED FIREWALL", color = Color(0xFF9AB8F8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(SocialProvider.INSTAGRAM, SocialProvider.THREADS).forEach { item ->
                    FilterChip(
                        selected = platform == item,
                        onClick = { platform = item },
                        label = { Text(if (item == SocialProvider.INSTAGRAM) "Instagram" else "Threads") }
                    )
                }
            }
            FirewallSwitch("Hide recommendation modules", settings.hideRecommendations) {
                updateSettings(settings.copy(hideRecommendations = it))
            }
            FirewallSwitch("Hide suggested accounts", settings.hideSuggestedAccounts) {
                updateSettings(settings.copy(hideSuggestedAccounts = it))
            }
            FirewallSwitch("Hide Reels navigation entry points", settings.hideReelsEntryPoints) {
                updateSettings(settings.copy(hideReelsEntryPoints = it))
            }
            Text("HIDDEN ACCOUNTS", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = accountDraft,
                    onValueChange = { accountDraft = it.take(31) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("@username") }
                )
                Button(onClick = {
                    val normalized = (settings.hiddenAccounts + accountDraft).let {
                        FeedFirewallSettings(hiddenAccounts = it).normalized().hiddenAccounts
                    }
                    updateSettings(settings.copy(hiddenAccounts = normalized))
                    accountDraft = ""
                }, enabled = accountDraft.isNotBlank()) { Text("Add") }
            }
            settings.hiddenAccounts.sorted().forEach { account ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("@$account", color = TextWhite, modifier = Modifier.weight(1f))
                    TextButton(onClick = { updateSettings(settings.copy(hiddenAccounts = settings.hiddenAccounts - account)) }) { Text("Remove") }
                }
            }
            Divider(color = Color.White.copy(alpha = 0.12f))
            Text("LEARNING FOCUS", color = Color(0xFF9AB8F8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            FirewallSwitch("Enable topic-based feed filtering", preferences.learning.enabled) {
                onPreferencesChange(preferences.copy(learning = preferences.learning.copy(enabled = it)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = preferences.learning.mode == LearningFocusMode.TOPIC_FILTER,
                    onClick = { onPreferencesChange(preferences.copy(learning = preferences.learning.copy(mode = LearningFocusMode.TOPIC_FILTER))) },
                    label = { Text("Topic filter") })
                FilterChip(selected = preferences.learning.mode == LearningFocusMode.TOPIC_SESSION,
                    onClick = { onPreferencesChange(preferences.copy(learning = preferences.learning.copy(mode = LearningFocusMode.TOPIC_SESSION))) },
                    label = { Text("Topic session") })
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = topicDraft, onValueChange = { topicDraft = it.take(40) },
                    modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("e.g. Kotlin, design") })
                Button(onClick = {
                    val normalized = (preferences.learning.topics + topicDraft).let {
                        preferences.learning.copy(topics = it).normalized().topics
                    }
                    onPreferencesChange(preferences.copy(learning = preferences.learning.copy(topics = normalized)))
                    topicDraft = ""
                }, enabled = topicDraft.isNotBlank()) { Text("Add") }
            }
            preferences.learning.topics.sorted().forEach { topic ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(topic, color = TextWhite, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onPreferencesChange(preferences.copy(
                        learning = preferences.learning.copy(topics = preferences.learning.topics - topic)
                    )) }) { Text("Remove") }
                }
            }
            Text("Simple on-device text matching on visible post cards; not an AI classifier. It may miss relevant posts or hide unrelated ones.",
                color = TextWhite.copy(alpha = 0.55f), fontSize = 11.sp, lineHeight = 15.sp)
            Divider(color = Color.White.copy(alpha = 0.12f))
            Text("FOCUS & COMFORT", color = Color(0xFF9AB8F8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Optional session reminder: ${preferences.focus.sessionLimitMinutes} minutes (0 = off)", color = TextWhite, fontSize = 13.sp)
            Slider(value = preferences.focus.sessionLimitMinutes.toFloat(), onValueChange = { value ->
                onPreferencesChange(preferences.copy(focus = preferences.focus.copy(sessionLimitMinutes = value.toInt())))
            }, valueRange = 0f..240f, steps = 15)
            FirewallSwitch("Scroll checkpoints (off by default)", preferences.focus.scrollCheckpoints) {
                onPreferencesChange(preferences.copy(focus = preferences.focus.copy(scrollCheckpoints = it)))
            }
            FirewallSwitch("Reduced motion preference", preferences.comfort.reducedMotion) {
                onPreferencesChange(preferences.copy(comfort = preferences.comfort.copy(reducedMotion = it)))
            }
            FirewallSwitch("Larger text preference", preferences.comfort.largerText) {
                onPreferencesChange(preferences.copy(comfort = preferences.comfort.copy(largerText = it)))
            }
            FirewallSwitch("Move dock to right for one-handed reach", preferences.comfort.oneHandedDock) {
                onPreferencesChange(preferences.copy(comfort = preferences.comfort.copy(oneHandedDock = it)))
            }
            Divider(color = Color.White.copy(alpha = 0.12f))
            Text("PRIVACY & PERMISSIONS", color = Color(0xFF9AB8F8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Feed preferences are stored on this device. Analytics are off. These controls do not collect private messages, passwords, contacts or browsing history.", color = TextWhite.copy(alpha = 0.62f), fontSize = 12.sp, lineHeight = 17.sp)
            Text("Camera permission: ${if (cameraGranted) "Granted" else "Not granted"}", color = TextWhite, fontSize = 12.sp)
            Text("Microphone permission: ${if (microphoneGranted) "Granted" else "Not granted"}", color = TextWhite, fontSize = 12.sp)
            Text("Camera and microphone are requested only when an official social page asks to use them. Internet access is required for Instagram and Threads.", color = TextWhite.copy(alpha = 0.55f), fontSize = 11.sp, lineHeight = 15.sp)
            Text("Not active yet: ad filtering, native notification summaries, community voting and quiet-hour notification scheduling. Doom Scroll does not request notification-listener access.", color = Color(0xFFE0C38A), fontSize = 11.sp, lineHeight = 15.sp)
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(26.dp)) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun FirewallSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
