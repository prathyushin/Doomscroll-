package com.prathyushin.doomscroll.data

import android.content.Context
import com.prathyushin.doomscroll.model.AppPolicy

class LocalPolicyStore(context: Context) {
    private val prefs = context.getSharedPreferences("doomscroll_policy", Context.MODE_PRIVATE)

    fun isEnabled(packageName: String): Boolean =
        prefs.getBoolean("enabled_$packageName", false)

    fun setEnabled(packageName: String, enabled: Boolean) {
        prefs.edit().putBoolean("enabled_$packageName", enabled).apply()
    }

    fun sessionLimitMinutes(): Int = prefs.getInt("session_limit_minutes", 20)

    fun setSessionLimitMinutes(value: Int) {
        prefs.edit().putInt("session_limit_minutes", value.coerceIn(5, 120)).apply()
    }

    fun cooldownMinutes(): Int = prefs.getInt("cooldown_minutes", 10)

    fun setCooldownMinutes(value: Int) {
        prefs.edit().putInt("cooldown_minutes", value.coerceIn(1, 60)).apply()
    }

    fun policies(): Set<String> =
        prefs.all.keys.filter { it.startsWith("enabled_") && prefs.getBoolean(it, false) }
            .map { it.removePrefix("enabled_") }.toSet()

    fun clear() { prefs.edit().clear().apply() }
}
