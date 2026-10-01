package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehaviorSnapshot
import com.prathyushin.doomscroll.model.PolicyDecision

class PolicyEngine(
    private val sessionLimitSeconds: Long = 20 * 60,
    private val scrollRateThresholdPerMinute: Double = 12.0,
    private val sustainedScrollSeconds: Long = 5 * 60,
    private val reopenThreshold: Int = 4
) {
    fun evaluate(snapshot: BehaviorSnapshot): PolicyDecision {
        val sustainedSession = snapshot.sessionSeconds >= sessionLimitSeconds
        val sustainedScrolling =
            snapshot.scrollRatePerMinute >= scrollRateThresholdPerMinute &&
                snapshot.sessionSeconds >= sustainedScrollSeconds
        val repeatedReopen =
            snapshot.reopenCount >= reopenThreshold && snapshot.sessionSeconds >= 60

        return when {
            sustainedSession -> PolicyDecision(true, "Long session")
            sustainedScrolling -> PolicyDecision(true, "Rapid sustained scrolling")
            repeatedReopen -> PolicyDecision(true, "Repeated reopening")
            else -> PolicyDecision(false, "")
        }
    }
}
