package com.prathyushin.doomscroll

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.ui.DoomScrollApp

class MainActivity : ComponentActivity() {
    private var browserHost: SocialBrowserHostView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        setContent {
            DoomScrollApp(onBrowserHostReady = { browserHost = it })
        }
    }

    override fun onPause() {
        browserHost?.pauseAndFlush()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserHost?.resumeViews()
    }

    override fun onDestroy() {
        browserHost?.destroy()
        browserHost = null
        super.onDestroy()
    }
}
