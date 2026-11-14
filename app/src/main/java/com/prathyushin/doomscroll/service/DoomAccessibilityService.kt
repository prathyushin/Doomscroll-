package com.prathyushin.doomscroll.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.model.BehaviorSnapshot
import com.prathyushin.doomscroll.policy.PolicyEngine
import com.prathyushin.doomscroll.policy.ReopenTracker
import java.util.ArrayDeque

class DoomAccessibilityService : AccessibilityService() {
    private lateinit var store: LocalPolicyStore
    private val handler = Handler(Looper.getMainLooper())
    private val scrollEvents = ArrayDeque<Long>()
    private val reopenTracker = ReopenTracker()

    private var activePackage: String? = null
    private var sessionStartedAt = 0L
    private var scrollCount = 0
    private var highScrollStartedAt = 0L
    private var lastInterventionAt = 0L
    private var overlay: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = LocalPolicyStore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val now = SystemClock.elapsedRealtime()

        // Treat Doom Scroll and every unprotected app as an exit from the
        // protected-app group. This also makes returning from Doom Scroll count.
        if (!store.isEnabled(packageName)) {
            if (activePackage != null) {
                reopenTracker.onProtectedExit(now)
                resetActiveSession()
            }
            return
        }

        if (packageName != activePackage) {
            if (activePackage == null) {
                reopenTracker.onProtectedEntry(now)
            }
            activePackage = packageName
            sessionStartedAt = now
            scrollCount = 0
            highScrollStartedAt = 0L
            scrollEvents.clear()
            removeOverlay()
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scrollCount++
            scrollEvents.addLast(now)
        }

        trimScrollWindow(now)
        val scrollRatePerMinute = scrollEvents.size.toDouble()
        if (scrollRatePerMinute >= SCROLL_RATE_THRESHOLD_PER_MINUTE) {
            if (highScrollStartedAt == 0L) highScrollStartedAt = now
        } else {
            highScrollStartedAt = 0L
        }

        if (now - lastInterventionAt < store.cooldownMinutes() * 60_000L) return
        if (overlay != null) return

        val sessionSeconds = ((now - sessionStartedAt) / 1000L).coerceAtLeast(0L)
        val sustainedScrollSeconds =
            if (highScrollStartedAt == 0L) 0L
            else ((now - highScrollStartedAt) / 1000L).coerceAtLeast(0L)

        val decision = PolicyEngine(
            sessionLimitSeconds = store.sessionLimitMinutes() * 60L
        ).evaluate(
            BehaviorSnapshot(
                packageName = packageName,
                sessionSeconds = sessionSeconds,
                scrolls = scrollCount,
                scrollRatePerMinute = scrollRatePerMinute,
                reopenCount = reopenTracker.count(now),
                sustainedScrollSeconds = sustainedScrollSeconds
            )
        )

        if (decision.intervene) {
            lastInterventionAt = now
            store.recordIntervention(packageName, decision.reason, sessionSeconds, scrollRatePerMinute)
            showIntervention(decision.reason)
        }
    }

    private fun trimScrollWindow(now: Long) {
        val cutoff = now - SCROLL_WINDOW_MILLIS
        while (scrollEvents.isNotEmpty() && scrollEvents.first() < cutoff) {
            scrollEvents.removeFirst()
        }
    }

    private fun resetActiveSession() {
        activePackage = null
        sessionStartedAt = 0L
        scrollCount = 0
        highScrollStartedAt = 0L
        scrollEvents.clear()
        removeOverlay()
    }

    private fun showIntervention(reason: String) {
        if (overlay != null) return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        var remaining = PAUSE_SECONDS

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(0xFFFFF3E6.toInt())
        }

        panel.addView(TextView(this).apply {
            text = "Pause before you continue"
            textSize = 26f
            setTextColor(0xFF111111.toInt())
        })

        panel.addView(TextView(this).apply {
            text = "$reason. Take a moment to decide intentionally."
            textSize = 16f
            setTextColor(0xFF43302E.toInt())
            setPadding(0, 20, 0, 20)
        })

        val unlock = Button(this).apply {
            text = "Wait " + remaining + "s to continue"
            isEnabled = false
        }
        panel.addView(unlock)

        panel.addView(Button(this).apply {
            text = "Leave app"
            setOnClickListener {
                performGlobalAction(GLOBAL_ACTION_BACK)
                removeOverlay()
            }
        })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager.addView(panel, params)
        overlay = panel

        val tick = object : Runnable {
            override fun run() {
                if (overlay !== panel) return
                remaining--
                if (remaining <= 0) {
                    unlock.text = "Continue intentionally"
                    unlock.isEnabled = true
                    unlock.setOnClickListener {
                        lastInterventionAt = SystemClock.elapsedRealtime()
                        removeOverlay()
                    }
                } else {
                    unlock.text = "Wait " + remaining + "s to continue"
                    handler.postDelayed(this, 1000L)
                }
            }
        }
        handler.postDelayed(tick, 1000L)
    }

    private fun removeOverlay() {
        handler.removeCallbacksAndMessages(null)
        overlay?.let {
            runCatching {
                (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)
            }
        }
        overlay = null
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private companion object {
        const val SCROLL_WINDOW_MILLIS = 60_000L
        const val SCROLL_RATE_THRESHOLD_PER_MINUTE = 12.0
        const val PAUSE_SECONDS = 15
    }
}
