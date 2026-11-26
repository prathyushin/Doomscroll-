package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.NotificationCategory
import com.prathyushin.doomscroll.model.NotificationFilterConfig
import com.prathyushin.doomscroll.model.NotificationFilterDecision

/**
 * In-memory, privacy-first notification classifier for Instagram and Threads.
 * Never stores or uploads notification text.
 */
class NotificationFilterEngine {

    companion object {
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        const val THREADS_PACKAGE = "com.instagram.barcelona"
    }

    fun evaluate(
        packageName: String,
        title: String,
        text: String,
        config: NotificationFilterConfig
    ): NotificationFilterDecision {
        if (!config.masterEnabled) {
            return NotificationFilterDecision(true, null, "Master filter disabled")
        }

        val isTargetApp = when (packageName) {
            INSTAGRAM_PACKAGE -> config.filterInstagramApp
            THREADS_PACKAGE -> config.filterThreadsApp
            else -> false
        }
        if (!isTargetApp) {
            return NotificationFilterDecision(true, null, "App not filtered")
        }

        val combined = "$title $text".lowercase()

        // Always keep account security / login alerts
        if (isSecurityAlert(combined)) {
            return NotificationFilterDecision(true, null, "Account security alert")
        }

        val category = classify(combined)
        if (category == null) {
            // Unclassified promotional/digest/engagement push -> suppress
            return NotificationFilterDecision(false, null, "Unclassified promotional or engagement alert")
        }

        val allowed = category in config.enabledCategories
        return NotificationFilterDecision(
            shouldKeep = allowed,
            category = category,
            reason = if (allowed) "Allowed category: ${category.title}" else "Suppressed category: ${category.title}"
        )
    }

    fun classify(normalizedText: String): NotificationCategory? {
        return when {
            containsAny(normalizedText, "sent a reel", "shared a reel", "sent you a reel", "shared a video") ->
                NotificationCategory.DM_SHARED_REELS

            containsAny(normalizedText, "sent you a message", "sent a message", "message request", "replied to your message", "messaged you", "direct message") ->
                NotificationCategory.DIRECT_MESSAGES

            containsAny(normalizedText, "mentioned you", "replied to you", "replied to your thread", "replied:", "tagged you") ->
                NotificationCategory.MENTIONS_REPLIES

            containsAny(normalizedText, "commented:", "commented on your", "left a comment") ->
                NotificationCategory.COMMENTS

            containsAny(normalizedText, "liked your", "likes your", "liked a reply") ->
                NotificationCategory.LIKES

            containsAny(normalizedText, "started following you", "followed you", "requested to follow") ->
                NotificationCategory.FOLLOWING_ACTIVITY

            containsAny(normalizedText, "added to their story", "posted a story", "shared a note", "left a note", "mentioned you in their story") ->
                NotificationCategory.STORIES_NOTES

            else -> null
        }
    }

    private fun isSecurityAlert(normalizedText: String): Boolean =
        containsAny(
            normalizedText,
            "security code",
            "verification code",
            "login attempt",
            "new login",
            "two-factor",
            "password",
            "suspicious"
        )

    private fun containsAny(haystack: String, vararg needles: String): Boolean =
        needles.any { haystack.contains(it) }
}
