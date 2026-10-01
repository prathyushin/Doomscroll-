package com.prathyushin.doomscroll.data

import android.content.Context

class LocalPolicyStore(context: Context) {
    private val appContext = context.applicationContext
    private val secure = KeystoreValueStore(appContext)
    private val dao = PolicyDatabase.get(appContext).policyDao()

    fun isEnabled(packageName: String): Boolean = dao.isEnabled(packageName) == true

    fun setEnabled(packageName: String, enabled: Boolean, displayName: String = packageName) {
        dao.upsertTarget(AppTargetEntity(packageName, displayName, enabled))
    }

    fun sessionLimitMinutes(): Int = secure.getInt("session_limit_minutes") ?: 20

    fun setSessionLimitMinutes(value: Int) {
        secure.putInt("session_limit_minutes", value.coerceIn(5, 120))
    }

    fun cooldownMinutes(): Int = secure.getInt("cooldown_minutes") ?: 10

    fun setCooldownMinutes(value: Int) {
        secure.putInt("cooldown_minutes", value.coerceIn(1, 60))
    }

    fun policies(): Set<String> = dao.enabledTargets().map { it.packageName }.toSet()

    fun recordIntervention(packageName: String, reason: String, sessionSeconds: Long, scrollRatePerMinute: Double) {
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

    fun disclosureAccepted(): Boolean = secure.getBoolean("disclosure_accepted") == true

    fun setDisclosureAccepted(accepted: Boolean) {
        secure.putBoolean("disclosure_accepted", accepted)
    }

    fun clear() {
        // Room targets/history remain local records; only encrypted scalar settings are cleared here.
        secure.putBoolean("disclosure_accepted", false)
        secure.putInt("session_limit_minutes", 20)
        secure.putInt("cooldown_minutes", 10)
    }
}
