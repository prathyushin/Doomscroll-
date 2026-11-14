package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehaviorSnapshot
import org.junit.Assert.assertEquals
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
        assertEquals("Long session", result.reason)
    }

    @Test fun highScrollRateAloneDoesNotIntervene() {
        val snapshot = BehaviorSnapshot(
            packageName = "demo",
            sessionSeconds = 301,
            scrolls = 30,
            scrollRatePerMinute = 20.0,
            reopenCount = 0,
            sustainedScrollSeconds = 20
        )
        assertFalse(PolicyEngine().evaluate(snapshot).intervene)
    }

    @Test fun sustainedRapidScrollingIntervenesOnlyAfterDurationThreshold() {
        val engine = PolicyEngine(requiredSustainedScrollSeconds = 60)
        val shortBurst = BehaviorSnapshot("demo", 600, 60, 15.0, 0, 59)
        val sustained = shortBurst.copy(sustainedScrollSeconds = 60)
        assertFalse(engine.evaluate(shortBurst).intervene)
        assertTrue(engine.evaluate(sustained).intervene)
    }

    @Test fun repeatedReopenRequiresFourReturnsAndSessionContext() {
        val engine = PolicyEngine(reopenThreshold = 4)
        assertFalse(engine.evaluate(BehaviorSnapshot("demo", 30, 0, 0.0, 4)).intervene)
        assertFalse(engine.evaluate(BehaviorSnapshot("demo", 61, 0, 0.0, 3)).intervene)
        assertTrue(engine.evaluate(BehaviorSnapshot("demo", 61, 0, 0.0, 4)).intervene)
    }

    @Test fun firstLaunchIsNotCountedAsAReopen() {
        val tracker = ReopenTracker()
        assertEquals(0, tracker.onProtectedEntry(1_000L))
        assertEquals(0, tracker.count(2_000L))
    }

    @Test fun repeatedReturnsAreCountedAcrossSessions() {
        val tracker = ReopenTracker()
        var now = 1_000L
        repeat(4) {
            tracker.onProtectedExit(now)
            now += 5_000L
            tracker.onProtectedEntry(now)
            now += 5_000L
        }
        assertEquals(4, tracker.count(now))
    }

    @Test fun oldReturnsExpireFromTheRollingWindow() {
        val tracker = ReopenTracker(windowMillis = 60_000L)
        tracker.onProtectedExit(1_000L)
        tracker.onProtectedEntry(2_000L)
        assertEquals(1, tracker.count(3_000L))
        assertEquals(0, tracker.count(63_000L))
    }
}
