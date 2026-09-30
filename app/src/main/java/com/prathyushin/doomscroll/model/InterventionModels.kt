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
