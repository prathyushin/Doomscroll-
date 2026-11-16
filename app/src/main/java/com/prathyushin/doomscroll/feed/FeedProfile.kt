package com.prathyushin.doomscroll.feed

/**
 * Feed presets are lightweight preferences, not separate destinations.
 * Platform-specific overrides remain independent.
 */
enum class FeedProfile(
    val title: String,
    val description: String,
    val defaults: FeedFirewallSettings
) {
    FRIENDS_FIRST(
        "Friends First",
        "Reduce suggested content and prioritise followed accounts.",
        FeedFirewallSettings(hideRecommendations = true, hideSuggestedAccounts = true)
    ),
    CLEAN_FEED(
        "Clean Feed",
        "Reduce recommendation modules and short-video navigation.",
        FeedFirewallSettings(hideRecommendations = true, hideSuggestedAccounts = true, hideReelsEntryPoints = true)
    ),
    PHOTO_MODE(
        "Photo Mode",
        "Reduce short-video destinations while keeping ordinary posts.",
        FeedFirewallSettings(hideReelsEntryPoints = true)
    ),
    FOCUS_MODE(
        "Focus Mode",
        "Apply your strongest optional feed restrictions.",
        FeedFirewallSettings(hideRecommendations = true, hideSuggestedAccounts = true, hideReelsEntryPoints = true)
    ),
    NORMAL(
        "Normal Mode",
        "Use the platform's normal browsing experience.",
        FeedFirewallSettings()
    )
}

/**
 * Local, best-effort controls for visible web UI elements only.
 * These settings cannot change server-side ranking or guarantee removal
 * when a platform changes its markup.
 */
data class FeedFirewallSettings(
    val hideRecommendations: Boolean = false,
    val hideSuggestedAccounts: Boolean = false,
    val hideReelsEntryPoints: Boolean = false,
    val hiddenAccounts: Set<String> = emptySet()
) {
    fun normalized(): FeedFirewallSettings = copy(
        hiddenAccounts = hiddenAccounts.map { it.trim().removePrefix("@").lowercase() }
            .filter { it.matches(Regex("[a-z0-9._]{1,30}")) }.toSet()
    )
}

enum class LearningFocusMode { TOPIC_FILTER, TOPIC_SESSION }

data class LearningFocusSettings(
    val enabled: Boolean = false,
    val topics: Set<String> = emptySet(),
    val mode: LearningFocusMode = LearningFocusMode.TOPIC_FILTER
) {
    fun normalized(): LearningFocusSettings = copy(
        topics = topics.map { it.trim().lowercase() }
            .filter { it.length in 2..40 && it.matches(Regex("[a-z0-9 #+.-]+")) }
            .toSet()
    )
}

data class FocusSettings(
    val sessionLimitMinutes: Int = 0,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val reelsSpeedBump: Boolean = false,
    val scrollCheckpoints: Boolean = false
) {
    fun normalized(): FocusSettings = copy(sessionLimitMinutes = sessionLimitMinutes.coerceIn(0, 240))
}

data class ComfortSettings(
    val reducedMotion: Boolean = false,
    val largerText: Boolean = false,
    val highContrast: Boolean = false,
    val oneHandedDock: Boolean = false
)

data class NotificationPreferences(
    val importantOnly: Boolean = false,
    val quietHoursEnabled: Boolean = false
)

data class PrivacyPreferences(
    val analyticsEnabled: Boolean = false,
    val adFilteringEnabled: Boolean = false
)
