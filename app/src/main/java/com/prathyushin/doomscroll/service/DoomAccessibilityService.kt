package com.prathyushin.doomscroll.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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
import com.prathyushin.doomscroll.policy.FocusScheduleEngine
import com.prathyushin.doomscroll.policy.PolicyEngine

class DoomAccessibilityService : AccessibilityService() {
    private lateinit var store: LocalPolicyStore
    private val focusEngine = FocusScheduleEngine()
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
        if (packageName == this.packageName) return
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

        val focus = focusEngine.evaluate(store.focusSchedules())
        val effectiveCooldownMin = focus.effectiveCooldownMinutes ?: store.cooldownMinutes()
        if (now - lastInterventionAt < effectiveCooldownMin * 60_000L) return

        val effectiveLimitMin = focus.effectiveSessionLimitMinutes ?: store.sessionLimitMinutes()
        val sessionSeconds = ((now - sessionStartedAt) / 1000L).coerceAtLeast(0)
        val engine = PolicyEngine(
            sessionLimitSeconds = effectiveLimitMin * 60L,
            scrollThreshold = store.scrollThreshold(),
            reopenThreshold = store.reopenThreshold()
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

        val panelBg = GradientDrawable().apply {
            setColor(0xF01B1A1F.toInt())
            cornerRadius = 48f
            setStroke(2, 0x44B8F2D0)
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 56, 56, 56)
            background = panelBg
        }

        panel.addView(TextView(this).apply {
            text = "ZEN PAUSE"
            textSize = 12f
            letterSpacing = 0.14f
            setTextColor(0xFFB8F2D0.toInt())
        })
        panel.addView(TextView(this).apply {
            text = "Pause before you continue"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 12, 0, 8)
        })
        panel.addView(TextView(this).apply {
            text = "$reason. Take a quiet breath and decide if you want to keep scrolling."
            textSize = 15f
            setTextColor(0xFFAAA6B0.toInt())
            setPadding(0, 0, 0, 32)
        })

        val primaryBtnBg = GradientDrawable().apply {
            setColor(0xFFB8F2D0.toInt())
            cornerRadius = 32f
        }
        val unlock = Button(this).apply {
            text = "Continue intentionally"
            background = primaryBtnBg
            setTextColor(0xFF101114.toInt())
            isAllCaps = false
            setOnClickListener {
                removeOverlay()
                lastInterventionAt = SystemClock.elapsedRealtime()
            }
        }
        panel.addView(unlock, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 20 })

        val secondaryBtnBg = GradientDrawable().apply {
            setColor(0xFF25242B.toInt())
            cornerRadius = 32f
        }
        val close = Button(this).apply {
            text = "Leave app"
            background = secondaryBtnBg
            setTextColor(0xFFFFFFFF.toInt())
            isAllCaps = false
            setOnClickListener {
                performGlobalAction(GLOBAL_ACTION_BACK)
                removeOverlay()
            }
        }
        panel.addView(close, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            horizontalMargin = 0.06f
        }

        runCatching {
            windowManager.addView(panel, params)
            overlay = panel
        }
    }

    private fun removeOverlay() {
        overlay?.let {
            runCatching { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
        }
        overlay = null
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
