package com.prathyushin.doomscroll.feed

/**
 * User-facing feed modes are presets, not separate browsing destinations.
 * A selected preset can be overridden by the user's per-platform settings.
 */
enum class FeedProfile(
    val title: String,
    val description: String,
    val defaults: FeedFirewallSettings
) {
    FRIENDS_FIRST(
        title = "Friends First",
        description = "Prioritise followed accounts and suppress clearly labelled recommendations.",
        defaults = FeedFirewallSettings(
            hideRecommendations = true,
            hideSuggestedAccounts = true,
            hideReels = false
        )
    ),
    CLEAN_FEED(
        title = "Clean Feed",
        description = "Reduce recommendation modules and Reels entry points.",
        defaults = FeedFirewallSettings(
            hideRecommendations = true,
            hideSuggestedAccounts = true,
            hideReels = true
        )
    ),
    PHOTO_MODE(
        title = "Photo Mode",
        description = "Reduce short-video destinations while keeping ordinary posts available.",
        defaults = FeedFirewallSettings(
            hideRecommendations = false,
            hideSuggestedAccounts = false,
            hideReels = true
        )
    ),
    FOCUS_MODE(
        title = "Focus Mode",
        description = "Use the strongest optional feed restrictions.",
        defaults = FeedFirewallSettings(
            hideRecommendations = true,
            hideSuggestedAccounts = true,
            hideReels = true
        )
    ),
    NORMAL(
        title = "Normal Mode",
        description = "Use the platform's normal browsing experience.",
        defaults = FeedFirewallSettings()
    )
}

/**
 * Local, deterministic preferences for the Feed Firewall.
 *
 * These values describe desired local UI filtering only. They cannot change
 * server-side recommendation ranking or guarantee that platform content is
 * removed when the website does not expose a stable, identifiable UI element.
 */
data class FeedFirewallSettings(
    val hideRecommendations: Boolean = false,
    val hideSuggestedAccounts: Boolean = false,
    val hideReels: Boolean = false,
    val hiddenAccounts: Set<String> = emptySet()
) {
    fun normalized(): FeedFirewallSettings = copy(
        hiddenAccounts = hiddenAccounts
            .map { it.trim().removePrefix("@").lowercase() }
            .filter { it.matches(Regex("[a-z0-9._]{1,30}")) }
            .toSet()
    )
}
