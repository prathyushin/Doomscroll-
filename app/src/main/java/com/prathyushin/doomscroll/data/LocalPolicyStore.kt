package com.prathyushin.doomscroll.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Synchronous local preferences for real-time service decisions.
 * History writes are dispatched to IO so Accessibility callbacks never wait
 * for a database operation on the main thread.
 */
class LocalPolicyStore(context: Context) {
    private val appContext = context.applicationContext
    private val targets = appContext.getSharedPreferences("doomscroll_targets", Context.MODE_PRIVATE)
    private val secure = KeystoreValueStore(appContext)
    private val dao = PolicyDatabase.get(appContext).policyDao()
    private val historyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun isEnabled(packageName: String): Boolean =
        targets.getBoolean("enabled_$packageName", false)

    fun setEnabled(packageName: String, enabled: Boolean, displayName: String = packageName) {
        targets.edit()
            .putBoolean("enabled_$packageName", enabled)
            .putString("name_$packageName", displayName)
            .apply()
    }

    fun sessionLimitMinutes(): Int = secure.getInt("session_limit_minutes") ?: 20

    fun setSessionLimitMinutes(value: Int) {
        secure.putInt("session_limit_minutes", value.coerceIn(5, 120))
    }

    fun cooldownMinutes(): Int = secure.getInt("cooldown_minutes") ?: 10

    fun setCooldownMinutes(value: Int) {
        secure.putInt("cooldown_minutes", value.coerceIn(1, 60))
    }

    fun policies(): Set<String> =
        targets.all.keys
            .filter { it.startsWith("enabled_") && targets.getBoolean(it, false) }
            .map { it.removePrefix("enabled_") }
            .toSet()

    fun recordIntervention(
        packageName: String,
        reason: String,
        sessionSeconds: Long,
        scrollRatePerMinute: Double
    ) {
        historyScope.launch {
            runCatching {
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
        }
    }

    fun disclosureAccepted(): Boolean = secure.getBoolean("disclosure_accepted") == true

    fun setDisclosureAccepted(accepted: Boolean) {
        secure.putBoolean("disclosure_accepted", accepted)
    }

    fun clear() {
        targets.edit().clear().apply()
        secure.putBoolean("disclosure_accepted", false)
        secure.putInt("session_limit_minutes", 20)
        secure.putInt("cooldown_minutes", 10)
    }
}
