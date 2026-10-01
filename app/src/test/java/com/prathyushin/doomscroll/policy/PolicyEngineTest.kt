package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehaviorSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PolicyEngineTest {
    @Test fun shortSessionDoesNotIntervene() {
        val result = PolicyEngine().evaluate(BehaviorSnapshot("demo", 120, 40, 4.0, 0))
        assertFalse(result.intervene)
    }

    @Test fun sustainedSessionIntervenes() {
        val result = PolicyEngine(sessionLimitSeconds = 600)
            .evaluate(BehaviorSnapshot("demo", 601, 1, 0.0, 0))
        assertTrue(result.intervene)
    }

    @Test fun sustainedScrollingRequiresSessionContext() {
        val engine = PolicyEngine(scrollRateThresholdPerMinute = 12.0)
        assertFalse(engine.evaluate(BehaviorSnapshot("demo", 30, 11, 20.0, 0)).intervene)
        assertTrue(engine.evaluate(BehaviorSnapshot("demo", 301, 60, 13.0, 0)).intervene)
    }

    @Test fun repeatedReopenRequiresContext() {
        val engine = PolicyEngine(reopenThreshold = 3)
        assertFalse(engine.evaluate(BehaviorSnapshot("demo", 30, 0, 0.0, 3)).intervene)
        assertTrue(engine.evaluate(BehaviorSnapshot("demo", 61, 0, 0.0, 3)).intervene)
    }
}
