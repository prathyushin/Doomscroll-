package com.prathyushin.doomscroll.browser

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import org.json.JSONArray

enum class SocialProvider(val startUrl: String) {
    INSTAGRAM("https://www.instagram.com/"),
    THREADS("https://www.threads.com/")
}

/**
 * Two persistent platform WebViews share Android's CookieManager while keeping
 * independent navigation histories. Page content receives no native JS bridge.
 */
class SocialBrowserHostView(context: Context) : FrameLayout(context) {
    private val cookieManager = CookieManager.getInstance()
    private val instagramView = createWebView(SocialProvider.INSTAGRAM)
    private val threadsView = createWebView(SocialProvider.THREADS)
    private var activeProvider = SocialProvider.INSTAGRAM
    private var mutedUsernames: Set<String> = emptySet()
    private var allowCurrentReelFromDm = false
    private var destroyed = false
    private var onCanGoBackChanged: ((Boolean) -> Unit)? = null
    private var onStoryLongPress: ((String) -> Unit)? = null
    private var onMediaViewerChanged: ((Boolean) -> Unit)? = null
    private var onFullscreenChanged: ((Boolean) -> Unit)? = null
    private var fullscreenView: View? = null
    private var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
    private val popupViews = mutableSetOf<WebView>()

    init {
        setBackgroundColor(Color.rgb(255, 243, 230))
        cookieManager.setAcceptCookie(true)
        addView(instagramView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(threadsView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        threadsView.visibility = View.GONE
        instagramView.loadUrl(SocialProvider.INSTAGRAM.startUrl)
    }

    fun setOnCanGoBackChanged(listener: (Boolean) -> Unit) {
        onCanGoBackChanged = listener
        notifyBackState()
    }

    fun setOnStoryLongPress(listener: (String) -> Unit) {
        onStoryLongPress = listener
    }

    fun setOnMediaViewerChanged(listener: (Boolean) -> Unit) {
        onMediaViewerChanged = listener
    }

    fun setOnFullscreenChanged(listener: (Boolean) -> Unit) {
        onFullscreenChanged = listener
    }

    fun exitFullscreen() {
        hideFullscreenView()
    }

    fun showProvider(provider: SocialProvider) {
        if (destroyed) return
        activeProvider = provider
        instagramView.visibility = if (provider == SocialProvider.INSTAGRAM) View.VISIBLE else View.GONE
        threadsView.visibility = if (provider == SocialProvider.THREADS) View.VISIBLE else View.GONE
        val target = currentView()
        if (target.url.isNullOrBlank()) target.loadUrl(provider.startUrl)
        target.onResume()
        notifyBackState()
    }

    fun setMutedUsernames(usernames: Set<String>) {
        mutedUsernames = usernames.map { it.trim().removePrefix("@").lowercase() }
            .filter { it.matches(Regex("[a-z0-9._]{1,30}")) }
            .toSet()
        applyInstagramControls()
    }

    fun canGoBack(): Boolean = currentView().canGoBack()

    fun goBack() {
        val view = currentView()
        if (view.canGoBack()) view.goBack()
        notifyBackState()
    }

    fun pauseAndFlush() {
        if (destroyed) return
        instagramView.onPause()
        threadsView.onPause()
        popupViews.forEach { it.onPause() }
        cookieManager.flush()
    }

    fun resumeViews() {
        if (destroyed) return
        currentView().onResume()
    }

    fun destroy() {
        if (destroyed) return
        destroyed = true
        cookieManager.flush()
        (popupViews + setOf(instagramView, threadsView)).toList().forEach { view ->
            view.stopLoading()
            (view.parent as? FrameLayout)?.removeView(view)
            view.destroy()
        }
        popupViews.clear()
        removeAllViews()
        hideFullscreenView()
        onCanGoBackChanged = null
        onStoryLongPress = null
        onMediaViewerChanged = null
        onFullscreenChanged = null
    }

    private fun currentView(): WebView =
        if (activeProvider == SocialProvider.INSTAGRAM) instagramView else threadsView

    private fun notifyBackState() {
        onCanGoBackChanged?.invoke(!destroyed && currentView().canGoBack())
    }

    private fun createWebView(provider: SocialProvider): WebView {
        return WebView(context).apply {
            setBackgroundColor(Color.rgb(255, 243, 230))
            configureWebView(this)
            webChromeClient = object : WebChromeClient() {
                override fun onJsPrompt(
                    view: WebView,
                    url: String,
                    message: String,
                    defaultValue: String?,
                    result: android.webkit.JsPromptResult
                ): Boolean {
                    val prefix = "doomscroll-story:"
                    if (message.startsWith(prefix)) {
                        val username = message.removePrefix(prefix).trim().lowercase()
                        if (username.matches(Regex("[a-z0-9._]{1,30}"))) {
                            onStoryLongPress?.invoke(username)
                            result.confirm("")
                            return true
                        }
                    }
                    if (message.startsWith("doomscroll-media:")) {
                        onMediaViewerChanged?.invoke(message.endsWith(":true"))
                        result.confirm("")
                        return true
                    }
                    return super.onJsPrompt(view, url, message, defaultValue, result)
                }

                override fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback) {
                    if (fullscreenView != null) {
                        callback.onCustomViewHidden()
                        return
                    }
                    fullscreenView = view
                    fullscreenCallback = callback
                    view.setBackgroundColor(Color.BLACK)
                    addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                    view.bringToFront()
                    onFullscreenChanged?.invoke(true)
                }

                override fun onHideCustomView() {
                    hideFullscreenView()
                }

                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: android.os.Message
                ): Boolean {
                    if (!isUserGesture || destroyed) return false
                    val popup = WebView(this@SocialBrowserHostView.context)
                    configureWebView(popup)
                    popup.webViewClient = createClient(provider)
                    popup.webChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView) {
                            popupViews.remove(window)
                            removeView(window)
                            window.stopLoading()
                            window.destroy()
                        }
                    }
                    popupViews.add(popup)
                    addView(popup, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                    val transport = resultMsg.obj as? WebView.WebViewTransport ?: run {
                        popupViews.remove(popup)
                        removeView(popup)
                        popup.destroy()
                        return false
                    }
                    transport.webView = popup
                    resultMsg.sendToTarget()
                    return true
                }
            }
            webViewClient = createClient(provider)
        }
    }

    private fun configureWebView(view: WebView) {
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.settings.databaseEnabled = true
        view.settings.loadsImagesAutomatically = true
        view.settings.javaScriptCanOpenWindowsAutomatically = true
        view.settings.setSupportMultipleWindows(true)
        view.settings.allowFileAccess = false
        view.settings.allowContentAccess = false
        view.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        view.settings.safeBrowsingEnabled = true
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(view, true)
    }

    private fun createClient(provider: SocialProvider): WebViewClient =
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                handleNavigation(view, request.url, provider)

            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                handleNavigation(view, Uri.parse(url), provider)

            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (view === currentView()) notifyBackState()
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                when (provider) {
                    SocialProvider.INSTAGRAM -> {
                        applyInstagramControls(view)
                        installMediaViewerObserver(view)
                    }
                    SocialProvider.THREADS -> applyThreadsControls(view)
                }
                if (view === currentView()) notifyBackState()
            }

            override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                if (view === currentView()) notifyBackState()
            }
        }

    private fun handleNavigation(view: WebView, uri: Uri, provider: SocialProvider): Boolean {
        val host = uri.host?.lowercase().orEmpty()

        // Meta sometimes wraps a normal HTTPS destination in an intent:// URL.
        // Keep first-party Threads/Instagram redirects inside their existing WebView.
        if (uri.scheme.equals("intent", ignoreCase = true) &&
            SocialRoutePolicy.isAllowedHost(host, provider)) {
            val httpsUri = Uri.Builder()
                .scheme("https")
                .encodedAuthority(uri.encodedAuthority)
                .encodedPath(uri.encodedPath)
                .encodedQuery(uri.encodedQuery)
                .build()
            view.post { view.loadUrl(httpsUri.toString()) }
            return true
        }

        if (!SocialRoutePolicy.isAllowedHost(host, provider)) {
            if (uri.scheme == "https" || uri.scheme == "http") openExternal(uri)
            return true
        }

        if (provider == SocialProvider.INSTAGRAM) {
            val path = uri.path.orEmpty()
            if (SocialRoutePolicy.isBlockedInstagramPath(path)) {
                view.post { view.loadUrl(SocialProvider.INSTAGRAM.startUrl) }
                return true
            }
            if (SocialRoutePolicy.isSingleReelPath(path)) {
                val sourcePath = Uri.parse(view.url.orEmpty()).path.orEmpty()
                if (!SocialRoutePolicy.isDmSourcePath(sourcePath)) {
                    allowCurrentReelFromDm = false
                    view.post { view.loadUrl(SocialProvider.INSTAGRAM.startUrl) }
                    return true
                }
                // A full-page DM-to-Reel navigation gets a one-time grant that
                // the injected page script uses to enable the single-Reel lock.
                allowCurrentReelFromDm = true
            } else {
                allowCurrentReelFromDm = false
            }
        }
        return false
    }

    private fun hideFullscreenView() {
        val view = fullscreenView ?: return
        fullscreenView = null
        (view.parent as? FrameLayout)?.removeView(view)
        fullscreenCallback?.onCustomViewHidden()
        fullscreenCallback = null
        onFullscreenChanged?.invoke(false)
    }

    private fun installMediaViewerObserver(view: WebView) {
        val script = """
            (function() {
              function notify() {
                const path = location.pathname.toLowerCase();
                const active = path.startsWith('/stories/') ||
                  path.startsWith('/reel/') ||
                  path.startsWith('/reels/');
                window.prompt('doomscroll-media:' + (active ? 'true' : 'false'));
              }
              if (!window.__doomScrollMediaObserver) {
                ['pushState','replaceState'].forEach(function(method) {
                  const original = history[method];
                  history[method] = function() {
                    const result = original.apply(this, arguments);
                    setTimeout(notify, 0);
                    return result;
                  };
                });
                window.addEventListener('popstate', notify);
                window.__doomScrollMediaObserver = true;
              }
              notify();
            })();
        """.trimIndent()
        view.evaluateJavascript(script, null)
    }

    private fun openExternal(uri: Uri) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: android.content.ActivityNotFoundException) {
            // Keep unsupported external destinations safely contained in the WebView.
        } catch (_: SecurityException) {
            // A platform policy may disallow launching an external activity.
        }
    }

    private fun applyInstagramControls(target: WebView = instagramView) {
        if (destroyed || target.url.isNullOrBlank()) return
        val mutedJson = JSONArray(mutedUsernames.toList()).toString()
        val grantDmReelLock = allowCurrentReelFromDm &&
            SocialRoutePolicy.isSingleReelPath(Uri.parse(target.url.orEmpty()).path.orEmpty())
        if (grantDmReelLock) allowCurrentReelFromDm = false
        val script = """
            (function() {
              if ($grantDmReelLock) window.__doomScrollDmReelActive = true;
              window.__doomScrollMutedUsers = $mutedJson;
              const styleId = 'doom-scroll-controls';
              let style = document.getElementById(styleId);
              if (!style) {
                style = document.createElement('style');
                style.id = styleId;
                document.documentElement.appendChild(style);
              }
              style.textContent = [
                'a[href^="/explore"], a[href^="/reels"],',
                '[aria-label="Explore"], [aria-label="Reels"],',
                'nav a[href*="/explore"], nav a[href*="/reels"],',
                '.doom-scroll-muted-story { display:none !important; }'
              ].join('');

              function hideTrapNavigation() {
                document.querySelectorAll('a[href],button,[role="tab"]').forEach(function(node) {
                  const label = ((node.getAttribute('aria-label') || '') + ' ' +
                    (node.innerText || '')).trim().toLowerCase();
                  const href = (node.getAttribute('href') || '').toLowerCase();
                  if (href.startsWith('/explore') || href.startsWith('/reels') ||
                      label === 'explore' || label === 'reels') {
                    node.style.setProperty('display', 'none', 'important');
                  }
                });
              }

              function getStoryTray() {
                const candidates = Array.from(document.querySelectorAll('[aria-label*="stor" i]'));
                for (const candidate of candidates) {
                  let node = candidate;
                  for (let depth = 0; node && depth < 4; depth++, node = node.parentElement) {
                    const rect = node.getBoundingClientRect();
                    const links = node.querySelectorAll('a[href]');
                    if (rect.width > 180 && rect.height > 35 && rect.height < 260 && links.length > 1) {
                      return node;
                    }
                  }
                }
                return null;
              }

              function updateMutedStories() {
                const tray = getStoryTray();
                if (!tray) return;
                const muted = new Set(window.__doomScrollMutedUsers || []);
                tray.querySelectorAll('a[href]').forEach(function(link) {
                  try {
                    const path = new URL(link.href, location.origin).pathname;
                    const parts = path.split('/').filter(Boolean);
                    const username = (parts[0] || '').toLowerCase();
                    const tile = link.closest('li,[role="listitem"]') || link;
                    if (muted.has(username)) tile.classList.add('doom-scroll-muted-story');
                    else tile.classList.remove('doom-scroll-muted-story');
                  } catch (_) {}
                });
              }

              function installStoryLongPress() {
                const tray = getStoryTray();
                if (!tray || tray.__doomScrollLongPressInstalled) return;
                tray.__doomScrollLongPressInstalled = true;
                let pressTimer = null;
                let pressedLink = null;
                function cancelPress() {
                  if (pressTimer) clearTimeout(pressTimer);
                  pressTimer = null;
                  pressedLink = null;
                }
                tray.addEventListener('touchstart', function(event) {
                  const link = event.target.closest('a[href]');
                  if (!link) return;
                  cancelPress();
                  pressedLink = link;
                  pressTimer = setTimeout(function() {
                    try {
                      const path = new URL(link.href, location.origin).pathname;
                      const username = path.split('/').filter(Boolean)[0] || '';
                      if (/^[a-z0-9._]{1,30}$/i.test(username)) {
                        window.prompt('doomscroll-story:' + username.toLowerCase());
                      }
                    } catch (_) {}
                    cancelPress();
                  }, 650);
                }, {passive:true});
                ['touchend','touchcancel','touchmove'].forEach(function(type) {
                  tray.addEventListener(type, cancelPress, {passive:true});
                });
                tray.addEventListener('contextmenu', function(event) {
                  const link = event.target.closest('a[href]');
                  if (!link) return;
                  try {
                    const username = new URL(link.href, location.origin).pathname.split('/').filter(Boolean)[0] || '';
                    if (/^[a-z0-9._]{1,30}$/i.test(username)) {
                      event.preventDefault();
                      window.prompt('doomscroll-story:' + username.toLowerCase());
                    }
                  } catch (_) {}
                });
              }

              function scheduleStoryUpdate() {
                if (window.__doomScrollStoryTimer) clearTimeout(window.__doomScrollStoryTimer);
                window.__doomScrollStoryTimer = setTimeout(attachStoryTrayObserver, 140);
              }

              function attachStoryTrayObserver() {
                const tray = getStoryTray();
                if (tray !== window.__doomScrollStoryTray) {
                  if (window.__doomScrollStoryObserver) window.__doomScrollStoryObserver.disconnect();
                  window.__doomScrollStoryTray = tray;
                  window.__doomScrollStoryObserver = null;
                  if (tray) {
                    window.__doomScrollStoryObserver = new MutationObserver(scheduleStoryUpdate);
                    window.__doomScrollStoryObserver.observe(tray, { childList: true, subtree: true });
                  }
                }
                updateMutedStories();
                installStoryLongPress();
              }

              hideTrapNavigation();
              attachStoryTrayObserver();
              const storyDiscoveryRoot = document.querySelector('header,[role="banner"]');
              if (storyDiscoveryRoot && !window.__doomScrollStoryDiscoveryObserver) {
                window.__doomScrollStoryDiscoveryObserver = new MutationObserver(scheduleStoryUpdate);
                window.__doomScrollStoryDiscoveryObserver.observe(storyDiscoveryRoot, {
                  childList: true, subtree: true
                });
              }
              if (!window.__doomScrollRouteObserverInstalled) {
                let lastPath = location.pathname;
                function isSingleReel(path) { return /^\/reel\/[^/]+\/?$/i.test(path); }
                function enforceRoutePolicy(sourcePath) {
                  const path = location.pathname.toLowerCase();
                  if (path === '/explore' || path.startsWith('/explore/') ||
                      path === '/reels' || path.startsWith('/reels/') ||
                      /^\/[^/]+\/reels\/?$/i.test(path)) {
                    location.replace('https://www.instagram.com/');
                    return;
                  }
                  if (isSingleReel(path)) {
                    const fromDm = (sourcePath || '').toLowerCase().startsWith('/direct/');
                    if (!fromDm && !window.__doomScrollDmReelActive) {
                      location.replace('https://www.instagram.com/');
                      return;
                    }
                    window.__doomScrollDmReelActive = true;
                  } else {
                    window.__doomScrollDmReelActive = false;
                  }
                  lastPath = path;
                }
                ['pushState','replaceState'].forEach(function(method) {
                  const original = history[method];
                  history[method] = function() {
                    const previousPath = location.pathname;
                    const result = original.apply(this, arguments);
                    setTimeout(function() { enforceRoutePolicy(previousPath); }, 0);
                    return result;
                  };
                });
                window.addEventListener('popstate', function() {
                  const previousPath = lastPath;
                  setTimeout(function() { enforceRoutePolicy(previousPath); }, 0);
                });
                window.__doomScrollRouteObserverInstalled = true;
              }

              if (!window.__doomScrollReelListenersInstalled) {
                function reelLockActive() {
                  return window.__doomScrollDmReelActive === true &&
                    /^\/reel\/[^/]+\/?$/i.test(location.pathname);
                }
                let startX = 0, startY = 0;
                document.addEventListener('touchstart', function(e) {
                  if (!reelLockActive() || !e.touches || !e.touches.length) return;
                  startX = e.touches[0].clientX;
                  startY = e.touches[0].clientY;
                }, {capture:true, passive:true});
                document.addEventListener('touchmove', function(e) {
                  if (!reelLockActive() || !e.touches || !e.touches.length) return;
                  const dx = e.touches[0].clientX - startX;
                  const dy = e.touches[0].clientY - startY;
                  if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 8) e.preventDefault();
                }, {capture:true, passive:false});
                document.addEventListener('wheel', function(e) {
                  if (reelLockActive() && Math.abs(e.deltaY) > Math.abs(e.deltaX)) e.preventDefault();
                }, {capture:true, passive:false});
                document.addEventListener('keydown', function(e) {
                  if (reelLockActive() && ['ArrowDown','PageDown',' '].includes(e.key)) e.preventDefault();
                }, true);
                window.__doomScrollReelListenersInstalled = true;
              }
            })();
        """.trimIndent()
        target.evaluateJavascript(script, null)
    }

    private fun applyThreadsControls(target: WebView = threadsView) {
        if (destroyed || target.url.isNullOrBlank()) return
        val script = """
            (function() {
              const styleId = 'doom-scroll-threads-controls';
              let style = document.getElementById(styleId);
              if (!style) {
                style = document.createElement('style');
                style.id = styleId;
                document.documentElement.appendChild(style);
              }
              style.textContent = '.doom-scroll-threads-for-you { display:none !important; }';
              function hideForYouTab() {
                document.querySelectorAll('a[href],button,[role="tab"]').forEach(function(node) {
                  const label = ((node.getAttribute('aria-label') || '') + ' ' +
                    (node.innerText || '')).trim().replace(/\s+/g, ' ').toLowerCase();
                  if (label === 'for you') node.classList.add('doom-scroll-threads-for-you');
                  else if (node.classList.contains('doom-scroll-threads-for-you')) {
                    node.classList.remove('doom-scroll-threads-for-you');
                  }
                });
              }
              hideForYouTab();
              if (!window.__doomScrollThreadsObserver) {
                let timer = null;
                window.__doomScrollThreadsObserver = new MutationObserver(function() {
                  if (timer) clearTimeout(timer);
                  timer = setTimeout(hideForYouTab, 160);
                });
                window.__doomScrollThreadsObserver.observe(document.documentElement, {
                  childList: true, subtree: true
                });
              }
            })();
        """.trimIndent()
        target.evaluateJavascript(script, null)
    }
}
