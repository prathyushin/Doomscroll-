package com.prathyushin.doomscroll.model

data class AppPolicy(
    val packageName: String,
    val displayName: String,
    val enabled: Boolean = true
)

data class BehaviorSnapshot(
    val packageName: String,
    val sessionSeconds: Long,
    val scrolls: Int,
    val reopenCount: Int
)

data class PolicyDecision(
    val intervene: Boolean,
    val reason: String
)

enum class InterventionState {
    IDLE,
    ACTIVE,
    GENTLE_PAUSE,
    COOLDOWN
}

data class BehavioralConfig(
    val sessionLimitMinutes: Int = 20,
    val scrollThreshold: Int = 80,
    val reopenThreshold: Int = 4,
    val cooldownMinutes: Int = 10,
    val pauseInterventionsEnabled: Boolean = true
)

data class SessionState(
    val platformId: String = "",
    val state: InterventionState = InterventionState.IDLE,
    val sessionSeconds: Long = 0L,
    val scrollCount: Int = 0,
    val reopenCount: Int = 0,
    val cooldownRemainingSeconds: Long = 0L,
    val pauseReason: String = "",
    val lastUpdatedElapsedMs: Long = 0L,
    val lastUpdatedWallMs: Long = 0L
)

data class PolicyTransition(
    val previousState: InterventionState,
    val nextState: InterventionState,
    val reason: String
)
