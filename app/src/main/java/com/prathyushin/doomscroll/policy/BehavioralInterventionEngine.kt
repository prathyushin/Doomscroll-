package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehavioralConfig
import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.InterventionState
import com.prathyushin.doomscroll.model.PolicyTransition
import com.prathyushin.doomscroll.model.SessionState

/**
 * Centralized, state-driven behavioral policy engine.
 * Pure deterministic transitions: IDLE -> ACTIVE -> GENTLE_PAUSE -> COOLDOWN -> ACTIVE / IDLE.
 */
class BehavioralInterventionEngine(
    initialState: SessionState = SessionState()
) {
    private var currentState: SessionState = initialState
    private val listeners = mutableListOf<(SessionState, PolicyTransition?) -> Unit>()

    fun state(): SessionState = currentState

    fun addListener(listener: (SessionState, PolicyTransition?) -> Unit) {
        listeners += listener
    }

    fun startOrResumeSession(
        platformId: String,
        elapsedMs: Long,
        wallMs: Long,
        reopenWindowMs: Long = 30_000L
    ): SessionState {
        val wasDifferentPlatform = currentState.platformId.isNotEmpty() && currentState.platformId != platformId
        val isQuickReopen = currentState.lastUpdatedElapsedMs > 0L &&
            (elapsedMs - currentState.lastUpdatedElapsedMs) in 1L..reopenWindowMs &&
            (wasDifferentPlatform || currentState.state == InterventionState.IDLE)

        val nextReopenCount = if (isQuickReopen) currentState.reopenCount + 1 else currentState.reopenCount
        val nextSessionSeconds = if (wasDifferentPlatform) 0L else currentState.sessionSeconds
        val nextScrollCount = if (wasDifferentPlatform) 0 else currentState.scrollCount

        val nextInterventionState = when (currentState.state) {
            InterventionState.GENTLE_PAUSE -> InterventionState.GENTLE_PAUSE
            InterventionState.COOLDOWN -> if (currentState.cooldownRemainingSeconds > 0L) InterventionState.COOLDOWN else InterventionState.ACTIVE
            InterventionState.IDLE,
            InterventionState.ACTIVE -> InterventionState.ACTIVE
        }

        val transition = if (currentState.state != nextInterventionState) {
            PolicyTransition(currentState.state, nextInterventionState, "Session started")
        } else null

        currentState = currentState.copy(
            platformId = platformId,
            state = nextInterventionState,
            sessionSeconds = nextSessionSeconds,
            scrollCount = nextScrollCount,
            reopenCount = nextReopenCount,
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        notifyListeners(transition)
        return currentState
    }

    fun pauseSession(elapsedMs: Long, wallMs: Long): SessionState {
        if (currentState.state == InterventionState.IDLE) return currentState
        val transition = PolicyTransition(currentState.state, InterventionState.IDLE, "Session paused")
        currentState = currentState.copy(
            state = InterventionState.IDLE,
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        notifyListeners(transition)
        return currentState
    }

    fun recordScroll(
        config: BehavioralConfig,
        focus: FocusEvaluation,
        elapsedMs: Long,
        wallMs: Long
    ): SessionState {
        if (currentState.state != InterventionState.ACTIVE && currentState.state != InterventionState.COOLDOWN) {
            return currentState
        }
        currentState = currentState.copy(
            scrollCount = currentState.scrollCount + 1,
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        return evaluate(config, focus, elapsedMs, wallMs)
    }

    fun tickSeconds(
        deltaSeconds: Long,
        config: BehavioralConfig,
        focus: FocusEvaluation,
        elapsedMs: Long,
        wallMs: Long
    ): SessionState {
        if (deltaSeconds <= 0L) return currentState
        if (currentState.state == InterventionState.IDLE || currentState.state == InterventionState.GENTLE_PAUSE) {
            return currentState
        }

        if (currentState.state == InterventionState.COOLDOWN) {
            val remaining = (currentState.cooldownRemainingSeconds - deltaSeconds).coerceAtLeast(0L)
            if (remaining == 0L) {
                val transition = PolicyTransition(InterventionState.COOLDOWN, InterventionState.ACTIVE, "Cooldown finished")
                currentState = currentState.copy(
                    state = InterventionState.ACTIVE,
                    sessionSeconds = 0L,
                    cooldownRemainingSeconds = 0L,
                    lastUpdatedElapsedMs = elapsedMs,
                    lastUpdatedWallMs = wallMs
                )
                notifyListeners(transition)
                return currentState
            } else {
                currentState = currentState.copy(
                    cooldownRemainingSeconds = remaining,
                    lastUpdatedElapsedMs = elapsedMs,
                    lastUpdatedWallMs = wallMs
                )
                notifyListeners(null)
                return currentState
            }
        }

        currentState = currentState.copy(
            sessionSeconds = currentState.sessionSeconds + deltaSeconds,
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        return evaluate(config, focus, elapsedMs, wallMs)
    }

    fun evaluate(
        config: BehavioralConfig,
        focus: FocusEvaluation,
        elapsedMs: Long,
        wallMs: Long
    ): SessionState {
        if (!config.pauseInterventionsEnabled || currentState.state != InterventionState.ACTIVE) {
            notifyListeners(null)
            return currentState
        }

        val effectiveLimitMinutes = focus.effectiveSessionLimitMinutes ?: config.sessionLimitMinutes
        val effectiveLimitSeconds = effectiveLimitMinutes.coerceAtLeast(1) * 60L

        val reason = when {
            currentState.sessionSeconds >= effectiveLimitSeconds ->
                if (focus.isFocusActive) "Focus session limit reached ($effectiveLimitMinutes min)"
                else "Session limit reached ($effectiveLimitMinutes min)"
            currentState.scrollCount >= config.scrollThreshold && currentState.sessionSeconds >= 5 * 60L ->
                "Sustained scrolling (${currentState.scrollCount} scrolls)"
            currentState.reopenCount >= config.reopenThreshold && currentState.sessionSeconds >= 60L ->
                "Repeated reopening (${currentState.reopenCount} times)"
            else -> null
        }

        if (reason != null) {
            val transition = PolicyTransition(InterventionState.ACTIVE, InterventionState.GENTLE_PAUSE, reason)
            currentState = currentState.copy(
                state = InterventionState.GENTLE_PAUSE,
                pauseReason = reason,
                lastUpdatedElapsedMs = elapsedMs,
                lastUpdatedWallMs = wallMs
            )
            notifyListeners(transition)
        } else {
            notifyListeners(null)
        }
        return currentState
    }

    fun acknowledgeAndContinue(
        config: BehavioralConfig,
        focus: FocusEvaluation,
        elapsedMs: Long,
        wallMs: Long
    ): SessionState {
        val cooldownMinutes = (focus.effectiveCooldownMinutes ?: config.cooldownMinutes).coerceAtLeast(1)
        val cooldownSeconds = cooldownMinutes * 60L
        val transition = PolicyTransition(currentState.state, InterventionState.COOLDOWN, "Continue intentionally")
        currentState = currentState.copy(
            state = InterventionState.COOLDOWN,
            sessionSeconds = 0L,
            cooldownRemainingSeconds = cooldownSeconds,
            scrollCount = 0,
            reopenCount = 0,
            pauseReason = "",
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        notifyListeners(transition)
        return currentState
    }

    fun endSessionAndReset(elapsedMs: Long, wallMs: Long): SessionState {
        val transition = if (currentState.state != InterventionState.IDLE) {
            PolicyTransition(currentState.state, InterventionState.IDLE, "Ended intentionally")
        } else null
        currentState = SessionState(
            platformId = currentState.platformId,
            state = InterventionState.IDLE,
            sessionSeconds = 0L,
            scrollCount = 0,
            reopenCount = 0,
            cooldownRemainingSeconds = 0L,
            pauseReason = "",
            lastUpdatedElapsedMs = elapsedMs,
            lastUpdatedWallMs = wallMs
        )
        notifyListeners(transition)
        return currentState
    }

    fun recoverState(saved: SessionState, currentElapsedMs: Long, currentWallMs: Long): SessionState {
        val wallDeltaSeconds = if (saved.lastUpdatedWallMs > 0L && currentWallMs >= saved.lastUpdatedWallMs) {
            (currentWallMs - saved.lastUpdatedWallMs) / 1000L
        } else {
            0L
        }
        // If process was away for > 15 minutes, treat as fresh session while keeping IDLE state
        currentState = if (wallDeltaSeconds > 15 * 60L) {
            SessionState(
                platformId = saved.platformId,
                state = InterventionState.IDLE,
                lastUpdatedElapsedMs = currentElapsedMs,
                lastUpdatedWallMs = currentWallMs
            )
        } else {
            val adjustedCooldown = (saved.cooldownRemainingSeconds - wallDeltaSeconds).coerceAtLeast(0L)
            val recoveredState = when {
                saved.state == InterventionState.COOLDOWN && adjustedCooldown == 0L -> InterventionState.IDLE
                saved.state == InterventionState.ACTIVE -> InterventionState.IDLE
                else -> saved.state
            }
            saved.copy(
                state = recoveredState,
                cooldownRemainingSeconds = adjustedCooldown,
                lastUpdatedElapsedMs = currentElapsedMs,
                lastUpdatedWallMs = currentWallMs
            )
        }
        notifyListeners(null)
        return currentState
    }

    private fun notifyListeners(transition: PolicyTransition?) {
        listeners.forEach { it(currentState, transition) }
    }
}
