package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehaviorSnapshot
import com.prathyushin.doomscroll.model.PolicyDecision

class PolicyEngine(
    private val sessionLimitSeconds: Long = 20 * 60,
    private val scrollThreshold: Int = 80,
    private val reopenThreshold: Int = 4
) {
    fun evaluate(snapshot: BehaviorSnapshot): PolicyDecision {
        val sustainedSession = snapshot.sessionSeconds >= sessionLimitSeconds
        val sustainedScrolling = snapshot.scrolls >= scrollThreshold &&
            snapshot.sessionSeconds >= 5 * 60
        val repeatedReopen = snapshot.reopenCount >= reopenThreshold &&
            snapshot.sessionSeconds >= 60

        return when {
            sustainedSession -> PolicyDecision(true, "Long session")
            sustainedScrolling -> PolicyDecision(true, "Sustained scrolling")
            repeatedReopen -> PolicyDecision(true, "Repeated reopening")
            else -> PolicyDecision(false, "")
        }
    }
}
