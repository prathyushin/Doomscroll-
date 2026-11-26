package com.prathyushin.doomscroll.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.prathyushin.doomscroll.model.SocialPlatform
import com.prathyushin.doomscroll.model.WebConnectionState
import com.prathyushin.doomscroll.policy.InstagramFirewallPolicy
import com.prathyushin.doomscroll.policy.RouteAction
import com.prathyushin.doomscroll.policy.ThreadsPolicy
import com.prathyushin.doomscroll.policy.ThreadsRouteAction

/**
 * Holds persistent WebView instances for Instagram and Threads across tab switches
 * without unnecessary reloads, flickering, or blank screens.
 */
class PlatformWebViewPool(
    private val context: Context,
    private val onConnectionChanged: (SocialPlatform, WebConnectionState, String?) -> Unit,
    private val onScrollEvent: (SocialPlatform) -> Unit,
    private val onScrollDirection: (Boolean) -> Unit,
    private val onStoryLongPress: (String) -> Unit,
    private val onFileChooserRequested: (ValueCallback<Array<Uri>>?, WebChromeClient.FileChooserParams?) -> Boolean,
    private val onMediaPermissionRequested: (PermissionRequest) -> Unit,
    private val onCustomViewChanged: (View?, WebChromeClient.CustomViewCallback?) -> Unit,
    private val isStrictFocusActive: () -> Boolean,
    private val getMutedUsernames: () -> List<String>
) {
    private val webViews = mutableMapOf<SocialPlatform, WebView>()
    private val igFirewall = InstagramFirewallPolicy()
    private val threadsPolicy = ThreadsPolicy()
    private var currentDmReelId: String? = null
    private var lastInstagramUrl: String? = null

    fun getOrCreate(platform: SocialPlatform): WebView {
        return webViews.getOrPut(platform) {
            createWebView(platform)
        }
    }

    fun reinjectScripts(platform: SocialPlatform) {
        val wv = webViews[platform] ?: return
        val script = when (platform) {
            SocialPlatform.INSTAGRAM -> WebViewScripts.instagramScript(
                mutedUsernames = getMutedUsernames(),
                strictFocusMode = isStrictFocusActive(),
                singleDmReelId = currentDmReelId
            )
            SocialPlatform.THREADS -> WebViewScripts.threadsScript()
        }
        wv.evaluateJavascript(script, null)
    }

    fun canGoBack(platform: SocialPlatform): Boolean = webViews[platform]?.canGoBack() == true

    fun goBack(platform: SocialPlatform): Boolean {
        val wv = webViews[platform] ?: return false
        return if (wv.canGoBack()) {
            wv.goBack()
            true
        } else {
            false
        }
    }

    fun reload(platform: SocialPlatform) {
        val wv = webViews[platform] ?: return
        onConnectionChanged(platform, WebConnectionState.LOADING, null)
        if (wv.url.isNullOrBlank()) {
            wv.loadUrl(platform.homeUrl)
        } else {
            wv.reload()
        }
    }

    fun flushCookies() {
        runCatching { CookieManager.getInstance().flush() }
    }

    fun onPause() {
        webViews.values.forEach { it.onPause() }
        flushCookies()
    }

    fun onResume() {
        webViews.values.forEach { it.onResume() }
    }

    fun destroyAll() {
        flushCookies()
        webViews.values.forEach { wv ->
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.stopLoading()
            wv.destroy()
        }
        webViews.clear()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(platform: SocialPlatform): WebView {
        val wv = WebView(context).apply {
            setBackgroundColor(Color.parseColor("#101114"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv, true)

        with(wv.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            setSupportMultipleWindows(false)
            // Strip "; wv" token so Meta/Threads OAuth and login flows do not reject the embedded browser
            userAgentString = userAgentString.replace("; wv", "")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = true
            }
        }

        wv.addJavascriptInterface(
            ZenJsBridge(
                onScroll = { onScrollEvent(platform) },
                onDirection = onScrollDirection,
                onStoryPress = onStoryLongPress
            ),
            "DoomZenBridge"
        )

        wv.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val targetUrl = request?.url?.toString() ?: return false
                val referer = view?.url ?: lastInstagramUrl

                return when (platform) {
                    SocialPlatform.INSTAGRAM -> {
                        val decision = igFirewall.evaluateUrl(
                            targetUrl = targetUrl,
                            refererUrl = referer,
                            strictFocusMode = isStrictFocusActive()
                        )
                        when (decision.action) {
                            RouteAction.ALLOW -> {
                                if (!targetUrl.contains("/direct")) currentDmReelId = null
                                lastInstagramUrl = targetUrl
                                false
                            }
                            RouteAction.ALLOW_SINGLE_DM_REEL -> {
                                currentDmReelId = extractReelId(targetUrl)
                                lastInstagramUrl = targetUrl
                                false
                            }
                            RouteAction.BLOCK_REDIRECT_HOME -> {
                                currentDmReelId = null
                                view?.loadUrl(decision.redirectUrl)
                                true
                            }
                            RouteAction.OPEN_EXTERNAL_BROWSER -> {
                                openExternal(targetUrl)
                                true
                            }
                        }
                    }

                    SocialPlatform.THREADS -> {
                        val decision = threadsPolicy.evaluateUrl(targetUrl)
                        when (decision.action) {
                            ThreadsRouteAction.ALLOW,
                            ThreadsRouteAction.ALLOW_AUTH_REDIRECT -> false
                            ThreadsRouteAction.BLOCK_LOOP_REDIRECT -> {
                                threadsPolicy.resetLoopGuard()
                                true
                            }
                            ThreadsRouteAction.OPEN_EXTERNAL_BROWSER -> {
                                openExternal(targetUrl)
                                true
                            }
                        }
                    }
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                onConnectionChanged(platform, WebConnectionState.LOADING, null)
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                if (platform == SocialPlatform.INSTAGRAM && url != null) {
                    val decision = igFirewall.evaluateUrl(
                        targetUrl = url,
                        refererUrl = lastInstagramUrl,
                        strictFocusMode = isStrictFocusActive()
                    )
                    when (decision.action) {
                        RouteAction.ALLOW_SINGLE_DM_REEL -> {
                            currentDmReelId = extractReelId(url)
                            lastInstagramUrl = url
                        }
                        RouteAction.BLOCK_REDIRECT_HOME -> {
                            currentDmReelId = null
                            view?.loadUrl(decision.redirectUrl)
                        }
                        RouteAction.ALLOW -> {
                            if (!url.contains("/direct")) currentDmReelId = null
                            lastInstagramUrl = url
                        }
                        RouteAction.OPEN_EXTERNAL_BROWSER -> Unit
                    }
                }
                reinjectScripts(platform)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                onConnectionChanged(platform, WebConnectionState.READY, null)
                if (platform == SocialPlatform.INSTAGRAM && url != null) {
                    lastInstagramUrl = url
                }
                threadsPolicy.resetLoopGuard()
                reinjectScripts(platform)
                flushCookies()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) {
                    val desc = error?.description?.toString() ?: "Unable to reach ${platform.displayName}"
                    onConnectionChanged(platform, WebConnectionState.ERROR, desc)
                }
            }

            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                // Recover cleanly from renderer crash without crashing the host app
                webViews.remove(platform)
                runCatching {
                    (view?.parent as? ViewGroup)?.removeView(view)
                    view?.destroy()
                }
                onConnectionChanged(platform, WebConnectionState.ERROR, "Web view recovered from a crash. Tap retry.")
                return true
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                return onFileChooserRequested(filePathCallback, fileChooserParams)
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) return
                val host = request.origin?.host?.lowercase().orEmpty()
                val isTrustedOrigin = host == "instagram.com" || host.endsWith(".instagram.com") ||
                    host == "threads.net" || host.endsWith(".threads.net") ||
                    host == "threads.com" || host.endsWith(".threads.com")
                if (isTrustedOrigin) {
                    onMediaPermissionRequested(request)
                } else {
                    request.deny()
                }
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                onCustomViewChanged(view, callback)
            }

            override fun onHideCustomView() {
                onCustomViewChanged(null, null)
            }
        }

        onConnectionChanged(platform, WebConnectionState.LOADING, null)
        wv.loadUrl(platform.homeUrl)
        return wv
    }

    private fun extractReelId(url: String): String? {
        val segments = Uri.parse(url).pathSegments ?: return null
        return if (segments.size >= 2 && (segments[0] == "reel" || segments[0] == "reels")) {
            segments[1]
        } else null
    }

    private fun openExternal(url: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private class ZenJsBridge(
        private val onScroll: () -> Unit,
        private val onDirection: (Boolean) -> Unit,
        private val onStoryPress: (String) -> Unit
    ) {
        @JavascriptInterface
        fun onUserScroll() = onScroll()

        @JavascriptInterface
        fun onScrollDirection(scrollingDown: Boolean) = onDirection(scrollingDown)

        @JavascriptInterface
        fun onStoryLongPress(username: String) = onStoryPress(username)
    }
}
