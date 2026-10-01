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
import java.util.ArrayDeque

class DoomAccessibilityService : AccessibilityService() {
    private lateinit var store: LocalPolicyStore
    private val handler = Handler(Looper.getMainLooper())
    private val scrollEvents = ArrayDeque<Long>()

    private var activePackage: String? = null
    private var sessionStartedAt = 0L
    private var scrollCount = 0
    private var reopenCount = 0
    private var lastInterventionAt = 0L
    private var lastProtectedExitAt = 0L
    private var overlay: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = LocalPolicyStore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName == packageNameOfService()) return

        if (!store.isEnabled(packageName)) {
            if (packageName == activePackage) {
                lastProtectedExitAt = SystemClock.elapsedRealtime()
                resetSession()
            }
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (packageName != activePackage) {
            if (activePackage != null && now - lastProtectedExitAt <= 30_000L) {
                reopenCount++
            }
            activePackage = packageName
            sessionStartedAt = now
            scrollCount = 0
            scrollEvents.clear()
            removeOverlay()
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scrollCount++
            scrollEvents.addLast(now)
            trimScrollWindow(now)
        }

        if (now - lastInterventionAt < store.cooldownMinutes() * 60_000L) return
        if (overlay != null) return

        val sessionSeconds = ((now - sessionStartedAt) / 1000L).coerceAtLeast(0)
        val scrollRatePerMinute = currentScrollRatePerMinute(now)
        val decision = PolicyEngine(
            sessionLimitSeconds = store.sessionLimitMinutes() * 60L
        ).evaluate(
            BehaviorSnapshot(
                packageName = packageName,
                sessionSeconds = sessionSeconds,
                scrolls = scrollCount,
                scrollRatePerMinute = scrollRatePerMinute,
                reopenCount = reopenCount
            )
        )

        if (decision.intervene) {
            lastInterventionAt = now
            store.recordIntervention(packageName, decision.reason, sessionSeconds, scrollRatePerMinute)
            showIntervention(decision.reason)
        }
    }

    private fun trimScrollWindow(now: Long) {
        val cutoff = now - 5_000L
        while (scrollEvents.isNotEmpty() && scrollEvents.first() < cutoff) {
            scrollEvents.removeFirst()
        }
    }

    private fun currentScrollRatePerMinute(now: Long): Double {
        trimScrollWindow(now)
        return scrollEvents.size * 12.0
    }

    private fun resetSession() {
        activePackage = null
        sessionStartedAt = 0L
        scrollCount = 0
        reopenCount = 0
        scrollEvents.clear()
        removeOverlay()
    }

    private fun showIntervention(reason: String) {
        if (overlay != null) return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        var remaining = 15

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

    private fun packageNameOfService() = packageName

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
