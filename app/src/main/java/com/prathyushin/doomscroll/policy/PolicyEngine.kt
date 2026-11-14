package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehaviorSnapshot
import com.prathyushin.doomscroll.model.PolicyDecision

/**
 * Deterministic, local-only policy evaluation.
 *
 * Scroll rate is a rolling 60-second event rate. A high rate alone is not
 * enough: it must persist for [requiredSustainedScrollSeconds] after the
 * minimum session context has elapsed.
 */
class PolicyEngine(
    private val sessionLimitSeconds: Long = 20 * 60,
    private val scrollRateThresholdPerMinute: Double = 12.0,
    private val minimumSessionSecondsForScroll: Long = 5 * 60,
    private val requiredSustainedScrollSeconds: Long = 60,
    private val reopenThreshold: Int = 4,
    private val minimumSessionSecondsForReopen: Long = 60
) {
    fun evaluate(snapshot: BehaviorSnapshot): PolicyDecision {
        val sustainedSession = snapshot.sessionSeconds >= sessionLimitSeconds
        val sustainedScrolling =
            snapshot.sessionSeconds >= minimumSessionSecondsForScroll &&
                snapshot.scrollRatePerMinute >= scrollRateThresholdPerMinute &&
                snapshot.sustainedScrollSeconds >= requiredSustainedScrollSeconds
        val repeatedReopen =
            snapshot.sessionSeconds >= minimumSessionSecondsForReopen &&
                snapshot.reopenCount >= reopenThreshold

        return when {
            sustainedSession -> PolicyDecision(true, "Long session")
            sustainedScrolling -> PolicyDecision(true, "Sustained rapid scrolling")
            repeatedReopen -> PolicyDecision(true, "Repeated reopening")
            else -> PolicyDecision(false, "")
        }
    }
}
