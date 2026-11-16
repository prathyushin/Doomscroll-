package com.prathyushin.doomscroll.feed

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DoomScrollPreferences(
    val instagram: FeedFirewallSettings = FeedFirewallSettings(),
    val threads: FeedFirewallSettings = FeedFirewallSettings(),
    val profile: FeedProfile = FeedProfile.NORMAL,
    val focus: FocusSettings = FocusSettings(),
    val learning: LearningFocusSettings = LearningFocusSettings(),
    val comfort: ComfortSettings = ComfortSettings(),
    val notifications: NotificationPreferences = NotificationPreferences(),
    val privacy: PrivacyPreferences = PrivacyPreferences()
)

class DoomScrollPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences("doom_scroll_preferences_v2", Context.MODE_PRIVATE)

    fun load(): DoomScrollPreferences {
        val root = runCatching { JSONObject(prefs.getString(KEY, "{}") ?: "{}") }.getOrDefault(JSONObject())
        return DoomScrollPreferences(
            instagram = root.optJSONObject("instagram").toFeedSettings(),
            threads = root.optJSONObject("threads").toFeedSettings(),
            profile = runCatching { FeedProfile.valueOf(root.optString("profile", "NORMAL")) }.getOrDefault(FeedProfile.NORMAL),
            focus = root.optJSONObject("focus").toFocusSettings(),
            learning = root.optJSONObject("learning").toLearningSettings(),
            comfort = root.optJSONObject("comfort").toComfortSettings(),
            notifications = root.optJSONObject("notifications").toNotificationSettings(),
            privacy = root.optJSONObject("privacy").toPrivacySettings()
        )
    }

    fun save(value: DoomScrollPreferences) {
        val root = JSONObject()
            .put("instagram", value.instagram.normalized().toJson())
            .put("threads", value.threads.normalized().toJson())
            .put("profile", value.profile.name)
            .put("focus", value.focus.normalized().toJson())
            .put("learning", value.learning.normalized().toJson())
            .put("comfort", value.comfort.toJson())
            .put("notifications", value.notifications.toJson())
            .put("privacy", value.privacy.toJson())
        prefs.edit().putString(KEY, root.toString()).apply()
    }

    private fun JSONObject?.toFeedSettings(): FeedFirewallSettings {
        val o = this ?: JSONObject()
        return FeedFirewallSettings(
            hideRecommendations = o.optBoolean("hideRecommendations"),
            hideSuggestedAccounts = o.optBoolean("hideSuggestedAccounts"),
            hideReelsEntryPoints = o.optBoolean("hideReelsEntryPoints"),
            hiddenAccounts = o.optStringSet("hiddenAccounts")
        ).normalized()
    }

    private fun JSONObject?.toFocusSettings(): FocusSettings {
        val o = this ?: JSONObject()
        return FocusSettings(
            sessionLimitMinutes = o.optInt("sessionLimitMinutes", 0),
            quietHoursEnabled = o.optBoolean("quietHoursEnabled"),
            quietHoursStart = o.optString("quietHoursStart", "22:00"),
            quietHoursEnd = o.optString("quietHoursEnd", "07:00"),
            reelsSpeedBump = o.optBoolean("reelsSpeedBump"),
            scrollCheckpoints = o.optBoolean("scrollCheckpoints", false)
        ).normalized()
    }

    private fun JSONObject?.toLearningSettings(): LearningFocusSettings {
        val o = this ?: JSONObject()
        return LearningFocusSettings(
            enabled = o.optBoolean("enabled"),
            topics = o.optStringSet("topics"),
            mode = runCatching { LearningFocusMode.valueOf(o.optString("mode", "TOPIC_FILTER")) }
                .getOrDefault(LearningFocusMode.TOPIC_FILTER)
        ).normalized()
    }

    private fun JSONObject?.toComfortSettings(): ComfortSettings {
        val o = this ?: JSONObject()
        return ComfortSettings(o.optBoolean("reducedMotion"), o.optBoolean("largerText"),
            o.optBoolean("highContrast"), o.optBoolean("oneHandedDock"))
    }

    private fun JSONObject?.toNotificationSettings(): NotificationPreferences {
        val o = this ?: JSONObject()
        return NotificationPreferences(o.optBoolean("importantOnly"), o.optBoolean("quietHoursEnabled"))
    }

    private fun JSONObject?.toPrivacySettings(): PrivacyPreferences {
        val o = this ?: JSONObject()
        return PrivacyPreferences(o.optBoolean("analyticsEnabled", false), o.optBoolean("adFilteringEnabled", false))
    }

    private fun JSONObject.optStringSet(key: String): Set<String> {
        val array = optJSONArray(key) ?: JSONArray()
        return buildSet { for (i in 0 until array.length()) add(array.optString(i)) }
    }

    private fun FeedFirewallSettings.toJson() = JSONObject()
        .put("hideRecommendations", hideRecommendations)
        .put("hideSuggestedAccounts", hideSuggestedAccounts)
        .put("hideReelsEntryPoints", hideReelsEntryPoints)
        .put("hiddenAccounts", JSONArray(hiddenAccounts.toList()))

    private fun FocusSettings.toJson() = JSONObject()
        .put("sessionLimitMinutes", sessionLimitMinutes)
        .put("quietHoursEnabled", quietHoursEnabled)
        .put("quietHoursStart", quietHoursStart)
        .put("quietHoursEnd", quietHoursEnd)
        .put("reelsSpeedBump", reelsSpeedBump)
        .put("scrollCheckpoints", scrollCheckpoints)

    private fun LearningFocusSettings.toJson() = JSONObject()
        .put("enabled", enabled).put("topics", JSONArray(topics.toList())).put("mode", mode.name)

    private fun ComfortSettings.toJson() = JSONObject()
        .put("reducedMotion", reducedMotion).put("largerText", largerText)
        .put("highContrast", highContrast).put("oneHandedDock", oneHandedDock)

    private fun NotificationPreferences.toJson() = JSONObject()
        .put("importantOnly", importantOnly).put("quietHoursEnabled", quietHoursEnabled)

    private fun PrivacyPreferences.toJson() = JSONObject()
        .put("analyticsEnabled", analyticsEnabled).put("adFilteringEnabled", adFilteringEnabled)

    private companion object { const val KEY = "settings_json" }
}
