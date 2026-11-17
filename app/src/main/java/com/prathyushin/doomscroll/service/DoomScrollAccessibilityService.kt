package com.prathyushin.doomscroll.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.prathyushin.doomscroll.feed.DoomScrollPreferencesStore
import java.util.ArrayDeque

/**
 * Optional, local-only scroll speed bump.
 *
 * It reads only the event type and package name. It never retrieves view text,
 * node trees, passwords, messages, URLs or gestures. Events from other apps
 * are ignored and are not stored.
 */
class DoomScrollAccessibilityService : AccessibilityService() {
    private val recentScrollEvents = ArrayDeque<Long>()
    private var appIsForeground = false
    private var lastInterventionAt = 0L
    private var overlay: View? = null
    private val preferences by lazy { DoomScrollPreferencesStore(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val sourcePackage = event.packageName?.toString() ?: return
        val now = SystemClock.elapsedRealtime()

        if (sourcePackage != packageName) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && appIsForeground) {
                appIsForeground = false
                recentScrollEvents.clear()
                removeOverlay()
            }
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            appIsForeground = true
            recentScrollEvents.clear()
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED || !appIsForeground) return

        val focus = runCatching { preferences.load().focus.normalized() }.getOrNull() ?: return
        if (!focus.scrollPauseEnabled) {
            recentScrollEvents.clear()
            return
        }

        while (recentScrollEvents.isNotEmpty() && now - recentScrollEvents.peekFirst() > WINDOW_MS) {
            recentScrollEvents.removeFirst()
        }
        recentScrollEvents.addLast(now)

        val cooldownMs = focus.pauseCooldownMinutes * 60_000L
        if (overlay != null || (lastInterventionAt != 0L && now - lastInterventionAt < cooldownMs)) return

        if (recentScrollEvents.size >= focus.scrollBurstThreshold) {
            lastInterventionAt = now
            recentScrollEvents.clear()
            showPauseOverlay()
        }
    }

    private fun showPauseOverlay() {
        if (overlay != null) return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.argb(176, 8, 9, 11))
            isClickable = true
            isFocusable = true
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(26), dp(28), dp(26), dp(24))
            background = rounded(0xFFFFF3E6.toInt(), dp(28).toFloat())
            isClickable = true
        }

        card.addView(TextView(this).apply {
            text = "A moment outside the scroll"
            textSize = 25f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setTextColor(0xFF111111.toInt())
            gravity = Gravity.CENTER
        })

        card.addView(TextView(this).apply {
            text = "You have been scrolling quickly. Take a breath and decide whether you still want to be here."
            textSize = 15f
            setLineSpacing(dp(4).toFloat(), 1f)
            setTextColor(0xFF43302E.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(22))
        })

        card.addView(actionButton("I'm done for now", true) {
            removeOverlay()
            appIsForeground = false
            performGlobalAction(GLOBAL_ACTION_HOME)
        })

        card.addView(actionButton("Continue intentionally", false) {
            removeOverlay()
            recentScrollEvents.clear()
        }.apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(10)
            layoutParams = params
        })

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ).apply {
            leftMargin = dp(22)
            rightMargin = dp(22)
        }
        root.addView(card, cardParams)

        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        runCatching {
            windowManager.addView(root, windowParams)
            overlay = root
        }
    }

    private fun actionButton(label: String, primary: Boolean, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 15f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            setTextColor(if (primary) Color.WHITE else 0xFF43302E.toInt())
            setPadding(dp(18), dp(15), dp(18), dp(15))
            background = rounded(
                if (primary) 0xFF8B1735.toInt() else 0xFFE8D8CC.toInt(),
                dp(18).toFloat()
            )
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun removeOverlay() {
        val current = overlay ?: return
        overlay = null
        runCatching { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(current) }
    }

    override fun onInterrupt() {
        removeOverlay()
        recentScrollEvents.clear()
    }

    override fun onDestroy() {
        removeOverlay()
        recentScrollEvents.clear()
        super.onDestroy()
    }

    private companion object {
        const val WINDOW_MS = 5_000L
    }
}
