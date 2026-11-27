package com.prathyushin.doomscroll.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.data.SessionManager
import com.prathyushin.doomscroll.model.InterventionState
import com.prathyushin.doomscroll.model.SocialPlatform
import com.prathyushin.doomscroll.model.WebConnectionState
import com.prathyushin.doomscroll.model.ZenDestination
import com.prathyushin.doomscroll.permissions.PermissionManager
import com.prathyushin.doomscroll.ui.components.FloatingGlassDock
import com.prathyushin.doomscroll.ui.components.GentlePauseOverlay
import com.prathyushin.doomscroll.ui.screens.AboutScreen
import com.prathyushin.doomscroll.ui.screens.AppSelectionScreen
import com.prathyushin.doomscroll.ui.screens.FocusScheduleScreen
import com.prathyushin.doomscroll.ui.screens.NotificationFilterScreen
import com.prathyushin.doomscroll.ui.screens.PrivacyDashboardScreen
import com.prathyushin.doomscroll.ui.screens.StoryControlsScreen
import com.prathyushin.doomscroll.ui.screens.ZenDashboardScreen
import com.prathyushin.doomscroll.ui.theme.ZenAmbientBackdrop
import com.prathyushin.doomscroll.ui.theme.ZenColors
import com.prathyushin.doomscroll.ui.theme.ZenGlassCard
import com.prathyushin.doomscroll.ui.theme.ZenTheme
import com.prathyushin.doomscroll.webview.PlatformWebViewPool
import kotlinx.coroutines.delay

@Composable
fun DoomScrollApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val store = remember { LocalPolicyStore(context.applicationContext) }
    val sessionManager = remember { SessionManager(store) }

    val behavioralConfig by sessionManager.behavioralConfig.collectAsState()
    val focusSchedules by sessionManager.focusSchedules.collectAsState()
    val focusEvaluation by sessionManager.focusEvaluation.collectAsState()
    val sessionState by sessionManager.sessionState.collectAsState()
    val mutedStories by sessionManager.mutedStories.collectAsState()
    val notificationConfig by sessionManager.notificationConfig.collectAsState()
    val notificationStats by sessionManager.notificationStats.collectAsState()
    val todayMinutes by sessionManager.todayMinutes.collectAsState()

    var destination by remember { mutableStateOf(ZenDestination.DASHBOARD) }
    var dockVisible by remember { mutableStateOf(true) }
    var selectedApps by remember { mutableStateOf(store.policies()) }
    var isAppResumed by remember { mutableStateOf(true) }

    val connectionStates = remember { mutableStateMapOf<SocialPlatform, WebConnectionState>() }
    val errorMessages = remember { mutableStateMapOf<SocialPlatform, String?>() }

    var pendingFileCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }
    var customFullscreenView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        pendingFileCallback?.onReceiveValue(if (uris.isEmpty()) null else uris.toTypedArray())
        pendingFileCallback = null
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val req = pendingPermissionRequest ?: return@rememberLauncherForActivityResult
        pendingPermissionRequest = null
        val grantedResources = req.resources.filter { res ->
            when (res) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> grants[Manifest.permission.CAMERA] == true
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> grants[Manifest.permission.RECORD_AUDIO] == true
                else -> false
            }
        }.toTypedArray()
        if (grantedResources.isNotEmpty()) {
            req.grant(grantedResources)
        } else {
            req.deny()
        }
    }

    val webViewPool = remember {
        PlatformWebViewPool(
            context = context,
            onConnectionChanged = { platform, state, err ->
                connectionStates[platform] = state
                errorMessages[platform] = err
            },
            onScrollEvent = { platform ->
                sessionManager.onWebViewScroll(platform)
            },
            onScrollDirection = { scrollingDown ->
                dockVisible = !scrollingDown
            },
            onStoryLongPress = { username ->
                sessionManager.toggleStoryMute(username)
            },
            onFileChooserRequested = { callback, params ->
                pendingFileCallback?.onReceiveValue(null)
                pendingFileCallback = callback
                val acceptTypes = params?.acceptTypes?.filter { it.isNotBlank() }?.toTypedArray()
                    ?: arrayOf("image/*", "video/*")
                fileChooserLauncher.launch(acceptTypes)
                true
            },
            onMediaPermissionRequested = { req ->
                val needed = mutableListOf<String>()
                if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in req.resources &&
                    !PermissionManager.hasCameraPermission(context)
                ) {
                    needed += Manifest.permission.CAMERA
                }
                if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in req.resources &&
                    !PermissionManager.hasMicrophonePermission(context)
                ) {
                    needed += Manifest.permission.RECORD_AUDIO
                }
                if (needed.isEmpty()) {
                    req.grant(req.resources)
                } else {
                    pendingPermissionRequest = req
                    mediaPermissionLauncher.launch(needed.toTypedArray())
                }
            },
            onCustomViewChanged = { view, callback ->
                customFullscreenView = view
                customViewCallback = callback
                dockVisible = view == null
            },
            isStrictFocusActive = { sessionManager.focusEvaluation.value.strictReelsBlock },
            getMutedUsernames = { sessionManager.mutedStories.value.map { it.username } }
        )
    }

    // Re-inject Instagram scripts whenever muted stories or focus evaluation changes
    LaunchedEffect(mutedStories, focusEvaluation) {
        webViewPool.reinjectScripts(SocialPlatform.INSTAGRAM)
    }


    // Refresh Focus Schedule immediately on system time or timezone changes
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                sessionManager.refreshFocusEvaluation()
            }
        }
        runCatching { context.registerReceiver(receiver, filter) }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    // Lifecycle synchronization
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    isAppResumed = true
                    webViewPool.onResume()
                    sessionManager.refreshFocusEvaluation()
                    sessionManager.refreshNotificationStats()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    isAppResumed = false
                    webViewPool.onPause()
                    sessionManager.onPlatformLeft()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    webViewPool.destroyAll()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Active platform tracking & 1-second session timer
    val activePlatform: SocialPlatform? = when (destination) {
        ZenDestination.INSTAGRAM -> SocialPlatform.INSTAGRAM
        ZenDestination.THREADS -> SocialPlatform.THREADS
        else -> null
    }

    LaunchedEffect(activePlatform, isAppResumed) {
        if (activePlatform != null && isAppResumed) {
            dockVisible = true
            sessionManager.onPlatformEntered(activePlatform)
            while (true) {
                delay(1000L)
                sessionManager.onForegroundSecondTick(activePlatform)
            }
        } else {
            dockVisible = true
            sessionManager.onPlatformLeft()
        }
    }

    BackHandler(enabled = customFullscreenView != null || destination != ZenDestination.DASHBOARD) {
        when {
            customFullscreenView != null -> {
                customViewCallback?.onCustomViewHidden()
                customFullscreenView = null
                customViewCallback = null
                dockVisible = true
            }
            activePlatform != null && webViewPool.canGoBack(activePlatform) -> {
                webViewPool.goBack(activePlatform)
            }
            else -> {
                destination = ZenDestination.DASHBOARD
            }
        }
    }

    ZenTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZenColors.Charcoal)
        ) {
            // Keep WebView containers persistent when switching between Instagram, Threads, and Dashboard
            if (activePlatform != null) {
                PersistentWebViewScreen(
                    platform = activePlatform,
                    webViewPool = webViewPool,
                    connectionState = connectionStates[activePlatform] ?: WebConnectionState.LOADING,
                    errorMessage = errorMessages[activePlatform],
                    onRetry = { webViewPool.reload(activePlatform) }
                )
            } else {
                ZenAmbientBackdrop {
                when (destination) {
                    ZenDestination.DASHBOARD -> ZenDashboardScreen(
                        config = behavioralConfig,
                        focusEvaluation = focusEvaluation,
                        sessionState = sessionState,
                        todayMinutes = todayMinutes,
                        mutedStoryCount = mutedStories.size,
                        onNavigate = { destination = it },
                        onUpdateConfig = { sessionManager.updateBehavioralConfig(it) }
                    )
                    ZenDestination.FOCUS -> FocusScheduleScreen(
                        schedules = focusSchedules,
                        evaluation = focusEvaluation,
                        onUpdateSchedules = { sessionManager.updateFocusSchedules(it) },
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.STORIES -> StoryControlsScreen(
                        mutedStories = mutedStories,
                        onMuteAccount = { sessionManager.setStoryMuted(it, true) },
                        onUnmuteAccount = { sessionManager.setStoryMuted(it, false) },
                        onClearAll = { sessionManager.clearMutedStories() },
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.NOTIFICATIONS -> NotificationFilterScreen(
                        config = notificationConfig,
                        stats = notificationStats,
                        onUpdateConfig = { sessionManager.updateNotificationConfig(it) },
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.APPS -> AppSelectionScreen(
                        selectedPackages = selectedApps,
                        onTogglePackage = { pkg ->
                            store.setEnabled(pkg, !store.isEnabled(pkg))
                            selectedApps = store.policies()
                        },
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.PRIVACY -> PrivacyDashboardScreen(
                        footprint = sessionManager.dataFootprint(),
                        sessionState = sessionState,
                        onClearUsageHistory = { sessionManager.clearUsageHistory() },
                        onClearMutedStories = { sessionManager.clearMutedStories() },
                        onClearAllData = {
                            sessionManager.clearAllLocalData()
                            selectedApps = store.policies()
                        },
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.ABOUT -> AboutScreen(
                        onBack = { destination = ZenDestination.DASHBOARD }
                    )
                    ZenDestination.INSTAGRAM,
                    ZenDestination.THREADS -> Unit
                }
                }
            }

            // Fullscreen WebView custom view (video/media)
            customFullscreenView?.let { view ->
                AndroidView(
                    factory = {
                        FrameLayout(context).apply {
                            setBackgroundColor(android.graphics.Color.BLACK)
                            (view.parent as? android.view.ViewGroup)?.removeView(view)
                            addView(view)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gentle pause intervention overlay
            if (sessionState.state == InterventionState.GENTLE_PAUSE && activePlatform != null) {
                GentlePauseOverlay(
                    reason = sessionState.pauseReason,
                    sessionMinutes = sessionState.sessionSeconds / 60L,
                    onContinueIntentionally = { sessionManager.continueIntentionally() },
                    onReturnToZen = {
                        sessionManager.endSessionAndReturnToZen()
                        destination = ZenDestination.DASHBOARD
                    }
                )
            }

            // Floating Glass Dock
            if (customFullscreenView == null && sessionState.state != InterventionState.GENTLE_PAUSE) {
                val sessionLabel = if (activePlatform != null && sessionState.sessionSeconds >= 60L) {
                    "${sessionState.sessionSeconds / 60L}m"
                } else null

                FloatingGlassDock(
                    currentDestination = destination,
                    visible = dockVisible,
                    sessionMinutesLabel = sessionLabel,
                    focusActive = focusEvaluation.isFocusActive,
                    onSelectDestination = { destination = it },
                    onRevealDock = { dockVisible = true },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun PersistentWebViewScreen(
    platform: SocialPlatform,
    webViewPool: PlatformWebViewPool,
    connectionState: WebConnectionState,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#101114"))
                }
            },
            update = { container ->
                val targetWebView = webViewPool.getOrCreate(platform)
                val currentChild = if (container.childCount > 0) container.getChildAt(0) else null
                if (currentChild !== targetWebView) {
                    container.removeAllViews()
                    (targetWebView.parent as? android.view.ViewGroup)?.removeView(targetWebView)
                    container.addView(targetWebView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (connectionState == WebConnectionState.LOADING) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter),
                color = ZenColors.Mint,
                trackColor = ZenColors.Charcoal
            )
        }

        if (connectionState == WebConnectionState.ERROR || connectionState == WebConnectionState.OFFLINE) {
            ZenGlassCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                highlighted = true
            ) {
                Text(
                    text = "${platform.displayName} couldn't load",
                    color = ZenColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = errorMessage ?: "Check your network connection and try again.",
                    color = ZenColors.TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenColors.Mint,
                            contentColor = ZenColors.Charcoal
                        )
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
