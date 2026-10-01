package com.prathyushin.doomscroll.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
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

class DoomAccessibilityService : AccessibilityService() {
    private lateinit var store: LocalPolicyStore
    private var activePackage: String? = null
    private var sessionStartedAt = 0L
    private var scrollCount = 0
    private var reopenCount = 0
    private var lastInterventionAt = 0L
    private var lastPackageChangeAt = 0L
    private var overlay: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = LocalPolicyStore(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName == packageNameOfService()) return
        if (!store.isEnabled(packageName)) {
            resetIfNeeded(packageName)
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (packageName != activePackage) {
            if (activePackage != null && now - lastPackageChangeAt < 30_000) reopenCount++
            activePackage = packageName
            sessionStartedAt = now
            scrollCount = 0
            lastPackageChangeAt = now
            removeOverlay()
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            scrollCount++
        }

        if (now - lastInterventionAt < store.cooldownMinutes() * 60_000L) return

        val sessionSeconds = ((now - sessionStartedAt) / 1000L).coerceAtLeast(0)
        val engine = PolicyEngine(
            sessionLimitSeconds = store.sessionLimitMinutes() * 60L
        )
        val decision = engine.evaluate(
            BehaviorSnapshot(packageName, sessionSeconds, scrollCount, reopenCount)
        )
        if (decision.intervene) {
            lastInterventionAt = now
            showIntervention(decision.reason)
        }
    }

    private fun resetIfNeeded(packageName: String) {
        if (packageName != activePackage) {
            activePackage = packageName
            sessionStartedAt = SystemClock.elapsedRealtime()
            scrollCount = 0
            reopenCount = 0
            removeOverlay()
        }
    }

    private fun showIntervention(reason: String) {
        if (overlay != null) return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

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
            setPadding(0, 20, 0, 28)
        })
        val unlock = Button(this).apply {
            text = "Continue intentionally"
            setOnClickListener {
                removeOverlay()
                lastInterventionAt = SystemClock.elapsedRealtime()
            }
        }
        panel.addView(unlock)
        val close = Button(this).apply {
            text = "Leave app"
            setOnClickListener {
                performGlobalAction(GLOBAL_ACTION_BACK)
                removeOverlay()
            }
        }
        panel.addView(close)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager.addView(panel, params)
        overlay = panel
    }

    private fun removeOverlay() {
        overlay?.let {
            runCatching { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
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
