package com.prathyushin.doomscroll.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification

/**
 * Optional, on-device filter for notifications posted by the separately installed
 * official Instagram and Threads apps. No notification content is stored or sent.
 */
class ImportantNotificationFilterService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val platformKey = when (packageName) {
            INSTAGRAM_PACKAGE -> PREF_INSTAGRAM
            THREADS_PACKAGE -> PREF_THREADS
            else -> return
        }

        val prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE)
        if (!prefs.getBoolean(PREF_MASTER, false) || !prefs.getBoolean(platformKey, true)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val content = "$title $text $bigText".lowercase()

        // Fail closed only while the user has explicitly enabled this filter.
        // The allowlist is deliberately conservative and depends on notification wording.
        if (!isImportantEvent(content)) {
            runCatching { cancelNotification(sbn.key) }
        }
    }

    private fun isImportantEvent(content: String): Boolean {
        val importantSignals = listOf(
            "sent you a message", "sent a message", "new message", "message request",
            "sent you a photo", "sent you a video", "sent you a reel",
            "shared a post with you", "shared a reel with you", "shared a photo with you",
            "started following you", "started following", "followed you",
            "requested to follow you",
            "security alert", "suspicious login", "new login", "login attempt",
            "password changed", "two-factor", "verification code", "security code",
            "account security", "unusual activity",
            "liked your post", "liked your story", "liked your note", "liked your comment",
            "replied to your comment", "replied to your story", "replied to your note",
            "mentioned you", "mentioned you in", "tagged you", "tagged you in",
            "shared a post", "shared a reel", "shared a photo", "shared a video"
        )
        return importantSignals.any(content::contains)
    }

    companion object {
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        private const val THREADS_PACKAGE = "com.instagram.barcelona"
        private const val PREFERENCES = "doom_scroll_social_preferences"
        private const val PREF_MASTER = "notification_filter_enabled"
        private const val PREF_INSTAGRAM = "notification_filter_instagram"
        private const val PREF_THREADS = "notification_filter_threads"
    }
}
