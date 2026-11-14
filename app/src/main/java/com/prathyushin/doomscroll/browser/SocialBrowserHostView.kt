package com.prathyushin.doomscroll.browser

import android.content.Context
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
    THREADS("https://www.threads.net/")
}

/**
 * Two persistent WebViews share Android's CookieManager but keep independent
 * navigation histories. No JavaScript bridge is exposed to page content.
 */
class SocialBrowserHostView(context: Context) : FrameLayout(context) {
    private val cookieManager = CookieManager.getInstance()
    private val instagramView = createWebView(SocialProvider.INSTAGRAM)
    private val threadsView = createWebView(SocialProvider.THREADS)
    private var activeProvider = SocialProvider.INSTAGRAM
    private var mutedUsernames: Set<String> = emptySet()
    private var destroyed = false

    init {
        setBackgroundColor(Color.rgb(255, 243, 230))
        cookieManager.setAcceptCookie(true)
        addView(instagramView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(threadsView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        threadsView.visibility = View.GONE
        instagramView.loadUrl(SocialProvider.INSTAGRAM.startUrl)
    }

    fun showProvider(provider: SocialProvider) {
        if (destroyed) return
        activeProvider = provider
        instagramView.visibility = if (provider == SocialProvider.INSTAGRAM) View.VISIBLE else View.GONE
        threadsView.visibility = if (provider == SocialProvider.THREADS) View.VISIBLE else View.GONE
        val target = if (provider == SocialProvider.INSTAGRAM) instagramView else threadsView
        if (target.url.isNullOrBlank()) target.loadUrl(provider.startUrl)
        target.onResume()
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
    }

    fun pauseAndFlush() {
        if (destroyed) return
        instagramView.onPause()
        threadsView.onPause()
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
        instagramView.stopLoading()
        threadsView.stopLoading()
        removeAllViews()
        instagramView.destroy()
        threadsView.destroy()
    }

    private fun currentView(): WebView =
        if (activeProvider == SocialProvider.INSTAGRAM) instagramView else threadsView

    private fun createWebView(provider: SocialProvider): WebView {
        return WebView(context).apply {
            setBackgroundColor(Color.rgb(255, 243, 230))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.safeBrowsingEnabled = true
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    return handleNavigation(view, request.url, provider)
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return handleNavigation(view, Uri.parse(url), provider)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    if (provider == SocialProvider.INSTAGRAM) applyInstagramControls(view)
                }
            }
        }
    }

    private fun handleNavigation(view: WebView, uri: Uri, provider: SocialProvider): Boolean {
        val host = uri.host?.lowercase().orEmpty()
        if (!isAllowedHost(host, provider)) return true

        if (provider == SocialProvider.INSTAGRAM && isBlockedInstagramRoute(uri.path.orEmpty())) {
            view.post { view.loadUrl(SocialProvider.INSTAGRAM.startUrl) }
            return true
        }
        return false
    }

    private fun isAllowedHost(host: String, provider: SocialProvider): Boolean {
        val platformHost = when (provider) {
            SocialProvider.INSTAGRAM -> host == "instagram.com" || host.endsWith(".instagram.com")
            SocialProvider.THREADS -> host == "threads.net" || host.endsWith(".threads.net")
        }
        val metaAuthHost = host == "facebook.com" || host.endsWith(".facebook.com") ||
            host == "accountscenter.instagram.com"
        return platformHost || metaAuthHost
    }

    private fun isBlockedInstagramRoute(path: String): Boolean {
        val normalized = if (path.startsWith("/")) path.lowercase() else "/${path.lowercase()}"
        return normalized == "/explore" || normalized.startsWith("/explore/") ||
            normalized == "/reels" || normalized.startsWith("/reels/")
        // Singular /reel/{shortcode}/ remains available as a one-item destination.
    }

    private fun applyInstagramControls(target: WebView = instagramView) {
        if (destroyed || target.url.isNullOrBlank()) return
        val mutedJson = JSONArray(mutedUsernames.toList()).toString()
        val script = """
            (function() {
              const muted = new Set($mutedJson);
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
                'nav a[href*="/explore"], nav a[href*="/reels"] { display:none !important; }'
              ].join('');

              function hideTrapNavigation() {
                document.querySelectorAll('a,button,[role="tab"]').forEach(function(node) {
                  const label = ((node.getAttribute('aria-label') || '') + ' ' +
                    (node.innerText || '')).trim().toLowerCase();
                  const href = (node.getAttribute('href') || '').toLowerCase();
                  if (href.startsWith('/explore') || href.startsWith('/reels') ||
                      label === 'explore' || label === 'reels') {
                    node.style.setProperty('display', 'none', 'important');
                  }
                });
              }

              function hideMutedStories() {
                if (!muted.size) return;
                const rails = Array.from(document.querySelectorAll('[aria-label*="stor" i],[role="list"],div'))
                  .filter(function(el) {
                    const r = el.getBoundingClientRect();
                    return r.width > 180 && r.height > 35 && r.height < 260 &&
                      el.scrollWidth > el.clientWidth + 12;
                  });
                rails.forEach(function(rail) {
                  rail.querySelectorAll('a[href]').forEach(function(link) {
                    try {
                      const parts = new URL(link.href, location.origin).pathname.split('/').filter(Boolean);
                      const username = (parts[0] || '').toLowerCase();
                      if (muted.has(username)) {
                        const tile = link.closest('li,[role="listitem"]') || link;
                        tile.style.setProperty('display', 'none', 'important');
                      }
                    } catch (_) {}
                  });
                });
              }

              hideTrapNavigation();
              hideMutedStories();
              if (!window.__doomScrollObserver) {
                window.__doomScrollObserver = new MutationObserver(function() {
                  hideTrapNavigation();
                  hideMutedStories();
                });
                window.__doomScrollObserver.observe(document.documentElement, {
                  childList: true, subtree: true, attributes: true
                });
              }

              // A singular /reel/ URL is permitted as a one-item destination.
              // Lock only vertical gesture navigation on that route; taps remain usable.
              if (/^\/reel\/[^/]+/.test(location.pathname) && !window.__doomScrollReelLock) {
                let startX = 0, startY = 0;
                document.addEventListener('touchstart', function(e) {
                  if (e.touches && e.touches.length) {
                    startX = e.touches[0].clientX;
                    startY = e.touches[0].clientY;
                  }
                }, {capture:true, passive:true});
                document.addEventListener('touchmove', function(e) {
                  if (!e.touches || !e.touches.length) return;
                  const dx = e.touches[0].clientX - startX;
                  const dy = e.touches[0].clientY - startY;
                  if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 8) e.preventDefault();
                }, {capture:true, passive:false});
                document.addEventListener('wheel', function(e) {
                  if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) e.preventDefault();
                }, {capture:true, passive:false});
                document.addEventListener('keydown', function(e) {
                  if (['ArrowDown','PageDown',' '].includes(e.key)) e.preventDefault();
                }, true);
                window.__doomScrollReelLock = true;
              }
            })();
        """.trimIndent()
        target.evaluateJavascript(script, null)
    }
}
