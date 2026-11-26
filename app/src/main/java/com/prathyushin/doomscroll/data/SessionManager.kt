package com.prathyushin.doomscroll.data

import android.os.SystemClock
import com.prathyushin.doomscroll.model.BehavioralConfig
import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.FocusSchedule
import com.prathyushin.doomscroll.model.LocalDataFootprint
import com.prathyushin.doomscroll.model.MutedStoryAccount
import com.prathyushin.doomscroll.model.NotificationFilterConfig
import com.prathyushin.doomscroll.model.NotificationStats
import com.prathyushin.doomscroll.model.SessionState
import com.prathyushin.doomscroll.model.SocialPlatform
import com.prathyushin.doomscroll.policy.BehavioralInterventionEngine
import com.prathyushin.doomscroll.policy.FocusScheduleEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates local storage, FocusScheduleEngine, and BehavioralInterventionEngine
 * with reactive StateFlows for the Zen UI and WebView runtime.
 */
class SessionManager(private val store: LocalPolicyStore) {

    private val focusEngine = FocusScheduleEngine()
    private val interventionEngine = BehavioralInterventionEngine()

    private val _behavioralConfig = MutableStateFlow(store.behavioralConfig())
    val behavioralConfig: StateFlow<BehavioralConfig> = _behavioralConfig.asStateFlow()

    private val _focusSchedules = MutableStateFlow(store.focusSchedules())
    val focusSchedules: StateFlow<List<FocusSchedule>> = _focusSchedules.asStateFlow()

    private val _focusEvaluation = MutableStateFlow(focusEngine.evaluate(_focusSchedules.value))
    val focusEvaluation: StateFlow<FocusEvaluation> = _focusEvaluation.asStateFlow()

    private val _sessionState = MutableStateFlow(
        interventionEngine.recoverState(
            saved = store.loadSessionState(),
            currentElapsedMs = SystemClock.elapsedRealtime(),
            currentWallMs = System.currentTimeMillis()
        )
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _mutedStories = MutableStateFlow(store.mutedStories())
    val mutedStories: StateFlow<List<MutedStoryAccount>> = _mutedStories.asStateFlow()

    private val _notificationConfig = MutableStateFlow(store.notificationFilterConfig())
    val notificationConfig: StateFlow<NotificationFilterConfig> = _notificationConfig.asStateFlow()

    private val _notificationStats = MutableStateFlow(store.notificationStats())
    val notificationStats: StateFlow<NotificationStats> = _notificationStats.asStateFlow()

    private val _todayMinutes = MutableStateFlow((store.todayUsageSeconds() / 60L).toInt())
    val todayMinutes: StateFlow<Int> = _todayMinutes.asStateFlow()

    init {
        interventionEngine.addListener { state, _ ->
            _sessionState.value = state
            store.saveSessionState(state)
        }
    }

    fun refreshFocusEvaluation() {
        _focusEvaluation.value = focusEngine.evaluate(_focusSchedules.value)
    }

    fun onPlatformEntered(platform: SocialPlatform) {
        refreshFocusEvaluation()
        interventionEngine.startOrResumeSession(
            platformId = platform.name,
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun onPlatformLeft() {
        interventionEngine.pauseSession(
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun onWebViewScroll(platform: SocialPlatform) {
        refreshFocusEvaluation()
        interventionEngine.recordScroll(
            config = _behavioralConfig.value,
            focus = _focusEvaluation.value,
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun onForegroundSecondTick(platform: SocialPlatform) {
        refreshFocusEvaluation()
        store.addUsageSeconds(platform.name, 1L)
        _todayMinutes.value = (store.todayUsageSeconds() / 60L).toInt()
        interventionEngine.tickSeconds(
            deltaSeconds = 1L,
            config = _behavioralConfig.value,
            focus = _focusEvaluation.value,
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun continueIntentionally() {
        refreshFocusEvaluation()
        interventionEngine.acknowledgeAndContinue(
            config = _behavioralConfig.value,
            focus = _focusEvaluation.value,
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun endSessionAndReturnToZen() {
        interventionEngine.endSessionAndReset(
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun updateBehavioralConfig(transform: (BehavioralConfig) -> BehavioralConfig) {
        val updated = transform(_behavioralConfig.value)
        store.setSessionLimitMinutes(updated.sessionLimitMinutes)
        store.setCooldownMinutes(updated.cooldownMinutes)
        store.setScrollThreshold(updated.scrollThreshold)
        store.setReopenThreshold(updated.reopenThreshold)
        store.setPauseInterventionsEnabled(updated.pauseInterventionsEnabled)
        _behavioralConfig.value = store.behavioralConfig()
    }

    fun updateFocusSchedules(schedules: List<FocusSchedule>) {
        store.saveFocusSchedules(schedules)
        _focusSchedules.value = schedules
        refreshFocusEvaluation()
    }

    fun toggleStoryMute(username: String) {
        val currentlyMuted = store.isStoryMuted(username)
        store.setStoryMuted(username, !currentlyMuted)
        _mutedStories.value = store.mutedStories()
    }

    fun setStoryMuted(username: String, muted: Boolean) {
        store.setStoryMuted(username, muted)
        _mutedStories.value = store.mutedStories()
    }

    fun clearMutedStories() {
        store.clearMutedStories()
        _mutedStories.value = emptyList()
    }

    fun updateNotificationConfig(config: NotificationFilterConfig) {
        store.saveNotificationFilterConfig(config)
        _notificationConfig.value = config
    }

    fun refreshNotificationStats() {
        _notificationStats.value = store.notificationStats()
    }

    fun clearNotificationStats() {
        store.clearNotificationStats()
        _notificationStats.value = NotificationStats()
    }

    fun clearUsageHistory() {
        store.clearUsageStats()
        _todayMinutes.value = 0
        interventionEngine.endSessionAndReset(
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun clearAllLocalData() {
        store.clear()
        _behavioralConfig.value = store.behavioralConfig()
        _focusSchedules.value = store.focusSchedules()
        _mutedStories.value = emptyList()
        _notificationConfig.value = store.notificationFilterConfig()
        _notificationStats.value = NotificationStats()
        _todayMinutes.value = 0
        refreshFocusEvaluation()
        interventionEngine.endSessionAndReset(
            elapsedMs = SystemClock.elapsedRealtime(),
            wallMs = System.currentTimeMillis()
        )
    }

    fun dataFootprint(): LocalDataFootprint = LocalDataFootprint(
        mutedStoryCount = _mutedStories.value.size,
        focusScheduleCount = _focusSchedules.value.size,
        monitoredAppCount = store.policies().size,
        todayMinutes = (store.todayUsageSeconds() / 60L).toInt(),
        weekMinutes = (store.sevenDayUsageSeconds() / 60L).toInt()
    )
}
