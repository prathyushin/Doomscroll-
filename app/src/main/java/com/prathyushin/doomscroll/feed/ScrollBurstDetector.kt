package com.prathyushin.doomscroll.feed

import java.util.ArrayDeque

/**
 * Counts only timestamps supplied by the caller. It stores no event payload,
 * package name or content and is intended for short-lived in-memory use.
 */
class ScrollBurstDetector(
    private val threshold: Int = 12,
    private val windowMillis: Long = 5_000L
) {
    private val timestamps = ArrayDeque<Long>()

    init {
        require(threshold > 0)
        require(windowMillis > 0L)
    }

    fun record(timestampMillis: Long): Boolean {
        while (timestamps.isNotEmpty() && timestampMillis - timestamps.peekFirst() >= windowMillis) {
            timestamps.removeFirst()
        }
        timestamps.addLast(timestampMillis)
        if (timestamps.size < threshold) return false
        timestamps.clear()
        return true
    }

    fun reset() {
        timestamps.clear()
    }
}
