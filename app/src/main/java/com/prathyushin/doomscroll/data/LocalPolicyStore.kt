package com.prathyushin.doomscroll.data

import android.content.Context
import com.prathyushin.doomscroll.model.AppPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalPolicyStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("doomscroll_policy", Context.MODE_PRIVATE)
    private val db = PolicyDatabase.get(appContext)
    private val dao = db.policyDao()

    fun isEnabled(packageName: String): Boolean =
        dao.isEnabled(packageName) == true

    fun setEnabled(packageName: String, enabled: Boolean, displayName: String = packageName) {
        dao.upsertTarget(AppTargetEntity(packageName, displayName, enabled))
    }

    fun sessionLimitMinutes(): Int = prefs.getInt("session_limit_minutes", 20)

    fun setSessionLimitMinutes(value: Int) {
        prefs.edit().putInt("session_limit_minutes", value.coerceIn(5, 120)).apply()
    }

    fun cooldownMinutes(): Int = prefs.getInt("cooldown_minutes", 10)

    fun setCooldownMinutes(value: Int) {
        prefs.edit().putInt("cooldown_minutes", value.coerceIn(1, 60)).apply()
    }

    fun policies(): Set<String> = dao.enabledTargets().map { it.packageName }.toSet()

    fun recordIntervention(
        packageName: String,
        reason: String,
        sessionSeconds: Long,
        scrollRatePerMinute: Double
    ) {
        dao.recordIntervention(
            InterventionHistoryEntity(
                packageName = packageName,
                timestampMs = System.currentTimeMillis(),
                reason = reason,
                sessionSeconds = sessionSeconds,
                scrollRatePerMinute = scrollRatePerMinute
            )
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
