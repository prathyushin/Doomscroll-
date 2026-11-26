package com.prathyushin.doomscroll.data

import android.content.Context
import com.prathyushin.doomscroll.model.BehavioralConfig
import com.prathyushin.doomscroll.model.FocusSchedule
import com.prathyushin.doomscroll.model.InterventionState
import com.prathyushin.doomscroll.model.MutedStoryAccount
import com.prathyushin.doomscroll.model.NotificationCategory
import com.prathyushin.doomscroll.model.NotificationFilterConfig
import com.prathyushin.doomscroll.model.NotificationStats
import com.prathyushin.doomscroll.model.SessionState
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate

class LocalPolicyStore(context: Context) {
    private val prefs = context.getSharedPreferences("doomscroll_policy", Context.MODE_PRIVATE)

    // --- Monitored external apps (Accessibility service) ---
    fun isEnabled(packageName: String): Boolean =
        prefs.getBoolean("enabled_$packageName", false)

    fun setEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("enabled_$packageName", enabled).apply()
    }

    fun policies(): Set<String> =
        prefs.all.keys.filter { it.startsWith("enabled_") && prefs.getBoolean(it, false) }
            .map { it.removePrefix("enabled_") }.toSet()

    // --- Behavioral config ---
    fun sessionLimitMinutes(): Int = prefs.getInt("session_limit_minutes", 20)

    fun setSessionLimitMinutes(value: Int) {
        prefs.edit().putInt("session_limit_minutes", value.coerceIn(5, 120)).apply()
    }

    fun cooldownMinutes(): Int = prefs.getInt("cooldown_minutes", 10)

    fun setCooldownMinutes(value: Int) {
        prefs.edit().putInt("cooldown_minutes", value.coerceIn(1, 60)).apply()
    }

    fun scrollThreshold(): Int = prefs.getInt("scroll_threshold", 80)

    fun setScrollThreshold(value: Int) {
        prefs.edit().putInt("scroll_threshold", value.coerceIn(20, 300)).apply()
    }

    fun reopenThreshold(): Int = prefs.getInt("reopen_threshold", 4)

    fun setReopenThreshold(value: Int) {
        prefs.edit().putInt("reopen_threshold", value.coerceIn(2, 15)).apply()
    }

    fun pauseInterventionsEnabled(): Boolean = prefs.getBoolean("pause_interventions_enabled", true)

    fun setPauseInterventionsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pause_interventions_enabled", enabled).apply()
    }

    fun behavioralConfig(): BehavioralConfig = BehavioralConfig(
        sessionLimitMinutes = sessionLimitMinutes(),
        scrollThreshold = scrollThreshold(),
        reopenThreshold = reopenThreshold(),
        cooldownMinutes = cooldownMinutes(),
        pauseInterventionsEnabled = pauseInterventionsEnabled()
    )

    // --- Session recovery state ---
    fun saveSessionState(state: SessionState) {
        prefs.edit()
            .putString("rec_platform", state.platformId)
            .putString("rec_state", state.state.name)
            .putLong("rec_session_sec", state.sessionSeconds)
            .putInt("rec_scrolls", state.scrollCount)
            .putInt("rec_reopens", state.reopenCount)
            .putLong("rec_cooldown_sec", state.cooldownRemainingSeconds)
            .putString("rec_reason", state.pauseReason)
            .putLong("rec_elapsed_ms", state.lastUpdatedElapsedMs)
            .putLong("rec_wall_ms", state.lastUpdatedWallMs)
            .apply()
    }

    fun loadSessionState(): SessionState {
        val stateName = prefs.getString("rec_state", InterventionState.IDLE.name) ?: InterventionState.IDLE.name
        val parsedState = runCatching { InterventionState.valueOf(stateName) }.getOrDefault(InterventionState.IDLE)
        return SessionState(
            platformId = prefs.getString("rec_platform", "") ?: "",
            state = parsedState,
            sessionSeconds = prefs.getLong("rec_session_sec", 0L),
            scrollCount = prefs.getInt("rec_scrolls", 0),
            reopenCount = prefs.getInt("rec_reopens", 0),
            cooldownRemainingSeconds = prefs.getLong("rec_cooldown_sec", 0L),
            pauseReason = prefs.getString("rec_reason", "") ?: "",
            lastUpdatedElapsedMs = prefs.getLong("rec_elapsed_ms", 0L),
            lastUpdatedWallMs = prefs.getLong("rec_wall_ms", 0L)
        )
    }

    // --- Smart Focus Schedules ---
    fun focusSchedules(): List<FocusSchedule> {
        val raw = prefs.getString("focus_schedules_json", null)
        if (raw.isNullOrBlank()) {
            return defaultFocusSchedules()
        }
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                val daysArray = obj.optJSONArray("days") ?: JSONArray()
                val days = (0 until daysArray.length()).mapNotNull { dIdx ->
                    runCatching { DayOfWeek.valueOf(daysArray.getString(dIdx)) }.getOrNull()
                }.toSet()
                FocusSchedule(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    enabled = obj.optBoolean("enabled", true),
                    startHour = obj.optInt("startHour", 9),
                    startMinute = obj.optInt("startMinute", 0),
                    endHour = obj.optInt("endHour", 17),
                    endMinute = obj.optInt("endMinute", 0),
                    daysOfWeek = days,
                    strictReelsBlock = obj.optBoolean("strictReelsBlock", true),
                    overrideSessionLimitMinutes = if (obj.has("overrideLimit") && !obj.isNull("overrideLimit")) obj.getInt("overrideLimit") else null,
                    overrideCooldownMinutes = if (obj.has("overrideCooldown") && !obj.isNull("overrideCooldown")) obj.getInt("overrideCooldown") else null,
                    timezoneId = if (obj.has("timezoneId") && !obj.isNull("timezoneId")) obj.getString("timezoneId") else null
                )
            }
        }.getOrElse { defaultFocusSchedules() }
    }

    fun saveFocusSchedules(schedules: List<FocusSchedule>) {
        val array = JSONArray()
        schedules.forEach { schedule ->
            val obj = JSONObject().apply {
                put("id", schedule.id)
                put("label", schedule.label)
                put("enabled", schedule.enabled)
                put("startHour", schedule.startHour)
                put("startMinute", schedule.startMinute)
                put("endHour", schedule.endHour)
                put("endMinute", schedule.endMinute)
                put("days", JSONArray(schedule.daysOfWeek.map { it.name }))
                put("strictReelsBlock", schedule.strictReelsBlock)
                put("overrideLimit", schedule.overrideSessionLimitMinutes)
                put("overrideCooldown", schedule.overrideCooldownMinutes)
                put("timezoneId", schedule.timezoneId)
            }
            array.put(obj)
        }
        prefs.edit().putString("focus_schedules_json", array.toString()).apply()
    }

    private fun defaultFocusSchedules(): List<FocusSchedule> = listOf(
        FocusSchedule(
            id = "deep_work",
            label = "Workday Focus",
            enabled = false,
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            ),
            strictReelsBlock = true,
            overrideSessionLimitMinutes = 10,
            overrideCooldownMinutes = 15
        ),
        FocusSchedule(
            id = "wind_down",
            label = "Overnight Wind-Down",
            enabled = false,
            startHour = 22,
            startMinute = 30,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = DayOfWeek.entries.toSet(),
            strictReelsBlock = true,
            overrideSessionLimitMinutes = 10,
            overrideCooldownMinutes = 20
        )
    )

    // --- Local Story Mute Controls ---
    fun mutedStories(): List<MutedStoryAccount> {
        val raw = prefs.getString("muted_stories_json", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { idx ->
                val obj = array.getJSONObject(idx)
                MutedStoryAccount(
                    username = obj.getString("username"),
                    mutedAtEpochMs = obj.optLong("mutedAt", 0L)
                )
            }.sortedBy { it.username.lowercase() }
        }.getOrDefault(emptyList())
    }

    fun isStoryMuted(username: String): Boolean {
        val normalized = normalizeUsername(username)
        return mutedStories().any { it.username.equals(normalized, ignoreCase = true) }
    }

    fun setStoryMuted(username: String, muted: Boolean) {
        val normalized = normalizeUsername(username)
        if (normalized.isBlank()) return
        val current = mutedStories().filterNot { it.username.equals(normalized, ignoreCase = true) }.toMutableList()
        if (muted) {
            current.add(MutedStoryAccount(normalized, System.currentTimeMillis()))
        }
        val array = JSONArray()
        current.forEach { item ->
            array.put(JSONObject().apply {
                put("username", item.username)
                put("mutedAt", item.mutedAtEpochMs)
            })
        }
        prefs.edit().putString("muted_stories_json", array.toString()).apply()
    }

    fun clearMutedStories() {
        prefs.edit().remove("muted_stories_json").apply()
    }

    private fun normalizeUsername(raw: String): String =
        raw.trim().removePrefix("@").lowercase()

    // --- Notification Filtering ---
    fun notificationFilterConfig(): NotificationFilterConfig {
        val enabledKeys = prefs.getStringSet("notif_categories", null)
        val categories = if (enabledKeys == null) {
            NotificationCategory.entries.filter { it.defaultEnabled }.toSet()
        } else {
            NotificationCategory.entries.filter { it.key in enabledKeys }.toSet()
        }
        return NotificationFilterConfig(
            masterEnabled = prefs.getBoolean("notif_master_enabled", false),
            filterInstagramApp = prefs.getBoolean("notif_filter_ig", false),
            filterThreadsApp = prefs.getBoolean("notif_filter_threads", false),
            enabledCategories = categories
        )
    }

    fun saveNotificationFilterConfig(config: NotificationFilterConfig) {
        prefs.edit()
            .putBoolean("notif_master_enabled", config.masterEnabled)
            .putBoolean("notif_filter_ig", config.filterInstagramApp)
            .putBoolean("notif_filter_threads", config.filterThreadsApp)
            .putStringSet("notif_categories", config.enabledCategories.map { it.key }.toSet())
            .apply()
    }

    fun notificationStats(): NotificationStats = NotificationStats(
        keptCount = prefs.getInt("notif_kept_count", 0),
        filteredCount = prefs.getInt("notif_filtered_count", 0)
    )

    fun recordNotificationDecision(kept: Boolean) {
        val key = if (kept) "notif_kept_count" else "notif_filtered_count"
        prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply()
    }

    fun clearNotificationStats() {
        prefs.edit().remove("notif_kept_count").remove("notif_filtered_count").apply()
    }

    // --- Local Usage Awareness (seconds per day, no server sync) ---
    fun addUsageSeconds(platformId: String, seconds: Long, date: LocalDate = LocalDate.now()) {
        if (seconds <= 0L) return
        val key = "usage_${date}_$platformId"
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + seconds).apply()
    }

    fun todayUsageSeconds(platformId: String? = null, date: LocalDate = LocalDate.now()): Long {
        val prefix = "usage_${date}_"
        return if (platformId != null) {
            prefs.getLong("$prefix$platformId", 0L)
        } else {
            prefs.all.entries
                .filter { it.key.startsWith(prefix) }
                .sumOf { (it.value as? Long) ?: 0L }
        }
    }

    fun sevenDayUsageSeconds(today: LocalDate = LocalDate.now()): Long {
        return (0L..6L).sumOf { offset ->
            todayUsageSeconds(date = today.minusDays(offset))
        }
    }

    fun clearUsageStats() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("usage_") || it.startsWith("rec_") }.forEach { editor.remove(it) }
        editor.apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
