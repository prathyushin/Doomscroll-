package com.prathyushin.doomscroll.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.os.SystemClock
import org.json.JSONObject
import java.time.LocalDate
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
private const val PREFS = "doom_scroll_social_preferences"
private const val MUTED_KEY = "muted_story_usernames"
private const val USAGE_KEY = "local_daily_usage_v1"

@Composable
fun DoomScrollApp(isAppForeground: Boolean, onBrowserHostReady: (SocialBrowserHostView) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var provider by remember { mutableStateOf(SocialProvider.INSTAGRAM) }
    var mutedUsers by remember {
        mutableStateOf(prefs.getStringSet(MUTED_KEY, emptySet())?.toSet() ?: emptySet())
    }
    var selectedStoryUser by remember { mutableStateOf<String?>(null) }
    var dockExpanded by remember { mutableStateOf(false) }
    var dockVisible by remember { mutableStateOf(true) }
    var browserHost by remember { mutableStateOf<SocialBrowserHostView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var isMediaViewer by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showUsage by remember { mutableStateOf(false) }
    var usageData by remember { mutableStateOf(readUsageData(prefs)) }
    var notificationFilterEnabled by remember { mutableStateOf(prefs.getBoolean("notification_filter_enabled", false)) }
    var instagramNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notification_filter_instagram", true)) }
    var threadsNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notification_filter_threads", true)) }
    var notificationAccessEnabled by remember { mutableStateOf(isNotificationAccessEnabled(context)) }

    LaunchedEffect(isAppForeground) {
        if (isAppForeground) notificationAccessEnabled = isNotificationAccessEnabled(context)
    }

    LaunchedEffect(provider, isAppForeground) {
        if (!isAppForeground) return@LaunchedEffect
        var lastTick = SystemClock.elapsedRealtime()
        addUsageInterval(prefs, provider, 0L, startSession = true)
        usageData = readUsageData(prefs)
        try {
            while (true) {
                delay(1000)
                val now = SystemClock.elapsedRealtime()
                val elapsed = (now - lastTick).coerceAtLeast(0L)
                lastTick = now
                if (elapsed > 0L) {
                    addUsageInterval(prefs, provider, elapsed, startSession = false)
                    usageData = readUsageData(prefs)
                }
            }
        } finally {
            val now = SystemClock.elapsedRealtime()
            val elapsed = (now - lastTick).coerceAtLeast(0L)
            if (elapsed > 0L) addUsageInterval(prefs, provider, elapsed, startSession = false)
            usageData = readUsageData(prefs)
        }
    }

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
                        OutlinedButton(
                            onClick = {
                                usageData = readUsageData(prefs)
                                showUsage = true
                                dockExpanded = false
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                        ) { Text("Usage") }
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

    if (showUsage) {
        AlertDialog(
            onDismissRequest = { showUsage = false },
            title = { Text("Usage Activity") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today · inside Doom Scroll", fontWeight = FontWeight.SemiBold)
                    Text("Instagram  ${formatUsageDuration(usageData.instagramTodayMs)}")
                    Text("Threads  ${formatUsageDuration(usageData.threadsTodayMs)}")
                    Text("Combined  ${formatUsageDuration(usageData.instagramTodayMs + usageData.threadsTodayMs)}")
                    Text("Sessions  ${usageData.instagramTodaySessions + usageData.threadsTodaySessions} · Avg. ${formatUsageDuration(usageData.averageSessionMs)}")
                    Divider(color = TextWhite.copy(alpha = 0.15f))
                    Text("Last 7 days", fontWeight = FontWeight.SemiBold)
                    usageData.week.forEach { day ->
                        Text("${day.first}  ${formatUsageDuration(day.second)}")
                    }
                    Divider(color = TextWhite.copy(alpha = 0.15f))
                    Text("Important-only notifications", fontWeight = FontWeight.SemiBold)
                    Text("Optional filter for notifications posted by the separate official apps. It does not receive notifications from the embedded websites.", style = MaterialTheme.typography.bodySmall, color = TextWhite.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Enable filter")
                        Switch(checked = notificationFilterEnabled, onCheckedChange = {
                            notificationFilterEnabled = it
                            prefs.edit().putBoolean("notification_filter_enabled", it).apply()
                        })
                    }
                    if (notificationFilterEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Instagram")
                            Switch(checked = instagramNotificationsEnabled, onCheckedChange = {
                                instagramNotificationsEnabled = it
                                prefs.edit().putBoolean("notification_filter_instagram", it).apply()
                            })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Threads")
                            Switch(checked = threadsNotificationsEnabled, onCheckedChange = {
                                threadsNotificationsEnabled = it
                                prefs.edit().putBoolean("notification_filter_threads", it).apply()
                            })
                        }
                        Text(if (notificationAccessEnabled) "Android notification access is enabled." else "Android notification access is not enabled yet.", style = MaterialTheme.typography.bodySmall, color = TextWhite.copy(alpha = 0.7f))
                        TextButton(onClick = {
                            runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                        }) { Text("Open notification access settings") }
                    }
                    Text("These totals measure Doom Scroll foreground time, not usage in the separate Instagram or Threads apps.", style = MaterialTheme.typography.bodySmall, color = TextWhite.copy(alpha = 0.7f))
                }
            },
            confirmButton = { TextButton(onClick = { showUsage = false }) { Text("Done") } }
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



private fun isNotificationAccessEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
    return enabled.split(':').any { component -> component.contains(context.packageName) }
}

private data class UsageData(
    val instagramTodayMs: Long,
    val threadsTodayMs: Long,
    val instagramTodaySessions: Int,
    val threadsTodaySessions: Int,
    val averageSessionMs: Long,
    val week: List<Pair<String, Long>>
)

private fun addUsageInterval(
    prefs: android.content.SharedPreferences,
    provider: SocialProvider,
    elapsedMs: Long,
    startSession: Boolean
) {
    val root = runCatching { JSONObject(prefs.getString(USAGE_KEY, "{}") ?: "{}") }
        .getOrElse { JSONObject() }
    val date = LocalDate.now().toString()
    val day = root.optJSONObject(date) ?: JSONObject()
    val prefix = if (provider == SocialProvider.INSTAGRAM) "instagram" else "threads"
    val timeKey = "${prefix}_ms"
    val sessionsKey = "${prefix}_sessions"
    day.put(timeKey, day.optLong(timeKey) + elapsedMs)
    if (startSession) day.put(sessionsKey, day.optInt(sessionsKey) + 1)
    root.put(date, day)
    prefs.edit().putString(USAGE_KEY, root.toString()).apply()
}

private fun readUsageData(prefs: android.content.SharedPreferences): UsageData {
    val root = runCatching { JSONObject(prefs.getString(USAGE_KEY, "{}") ?: "{}") }
        .getOrElse { JSONObject() }
    val today = root.optJSONObject(LocalDate.now().toString()) ?: JSONObject()
    val instagramMs = today.optLong("instagram_ms")
    val threadsMs = today.optLong("threads_ms")
    val instagramSessions = today.optInt("instagram_sessions")
    val threadsSessions = today.optInt("threads_sessions")
    val sessions = instagramSessions + threadsSessions
    val totalMs = instagramMs + threadsMs
    val week = (0..6).map { offset ->
        val date = LocalDate.now().minusDays(offset.toLong())
        val day = root.optJSONObject(date.toString()) ?: JSONObject()
        val label = if (offset == 0) "Today" else date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        label to (day.optLong("instagram_ms") + day.optLong("threads_ms"))
    }.reversed()
    return UsageData(
        instagramMs, threadsMs, instagramSessions, threadsSessions,
        if (sessions > 0) totalMs / sessions else 0L, week
    )
}

private fun formatUsageDuration(milliseconds: Long): String {
    val minutes = milliseconds.coerceAtLeast(0L) / 60_000L
    val hours = minutes / 60L
    val remainder = minutes % 60L
    return if (hours > 0L) "${hours}h ${remainder}m" else "${remainder}m"
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
