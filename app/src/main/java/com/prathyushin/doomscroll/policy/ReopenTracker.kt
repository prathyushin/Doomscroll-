package com.prathyushin.doomscroll.policy

import java.util.ArrayDeque

/**
 * Counts returns to any protected app after the user has left the protected
 * app group. Timestamps are kept only in memory and expire after the window.
 */
class ReopenTracker(
    private val windowMillis: Long = 10 * 60 * 1000L
) {
    private val reopenTimes = ArrayDeque<Long>()
    private var lastProtectedExitAt: Long? = null

    fun onProtectedExit(nowMillis: Long) {
        lastProtectedExitAt = nowMillis
    }

    fun onProtectedEntry(nowMillis: Long): Int {
        trim(nowMillis)
        val exitAt = lastProtectedExitAt
        if (exitAt != null && nowMillis >= exitAt && nowMillis - exitAt <= windowMillis) {
            reopenTimes.addLast(nowMillis)
        }
        lastProtectedExitAt = null
        return reopenTimes.size
    }

    fun count(nowMillis: Long): Int {
        trim(nowMillis)
        return reopenTimes.size
    }

    private fun trim(nowMillis: Long) {
        val cutoff = nowMillis - windowMillis
        while (reopenTimes.isNotEmpty() && reopenTimes.first() < cutoff) {
            reopenTimes.removeFirst()
        }
    }
}
