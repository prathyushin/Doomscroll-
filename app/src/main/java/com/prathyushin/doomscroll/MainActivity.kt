package com.prathyushin.doomscroll

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prathyushin.doomscroll.auth.OAuthConfig
import com.prathyushin.doomscroll.auth.OAuthManager
import com.prathyushin.doomscroll.auth.Platform
import com.prathyushin.doomscroll.ui.DoomScrollApp

class MainActivity : ComponentActivity() {
    private lateinit var oauth: OAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oauth = OAuthManager(this)
        setContent { DoomScrollApp(oauth) }
        intent?.data?.let(::handleOAuth)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let(::handleOAuth)
    }

    private fun handleOAuth(uri: android.net.Uri) {
        val configs = buildMap {
            if (BuildConfig.INSTAGRAM_CLIENT_ID.isNotBlank()) {
                put(Platform.INSTAGRAM, OAuthConfig.instagram(BuildConfig.INSTAGRAM_CLIENT_ID))
            }
            if (BuildConfig.THREADS_CLIENT_ID.isNotBlank()) {
                put(Platform.THREADS, OAuthConfig.threads(BuildConfig.THREADS_CLIENT_ID))
            }
        }
        oauth.handleCallback(uri, configs) { platform, success ->
            runOnUiThread { }
        }
    }
}
