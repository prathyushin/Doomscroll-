package com.prathyushin.doomscroll.feed

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrollBurstDetectorTest {
    @Test
    fun triggersOnlyAfterConfiguredNumberWithinWindow() {
        val detector = ScrollBurstDetector(threshold = 3, windowMillis = 5_000L)
        assertFalse(detector.record(100L))
        assertFalse(detector.record(900L))
        assertTrue(detector.record(1_500L))
    }

    @Test
    fun discardsEventsOutsideRollingWindow() {
        val detector = ScrollBurstDetector(threshold = 3, windowMillis = 5_000L)
        assertFalse(detector.record(100L))
        assertFalse(detector.record(200L))
        assertFalse(detector.record(5_101L))
        assertFalse(detector.record(5_200L))
        assertTrue(detector.record(5_300L))
    }

    @Test
    fun resetClearsPreviousEvents() {
        val detector = ScrollBurstDetector(threshold = 2)
        assertFalse(detector.record(100L))
        detector.reset()
        assertFalse(detector.record(200L))
        assertTrue(detector.record(300L))
    }
}
