package com.prathyushin.doomscroll.webview

/**
 * Lightweight, privacy-first DOM & SPA navigation scripts for Instagram and Threads.
 * All scripts are idempotent and resilient to Meta markup changes.
 */
object WebViewScripts {

    fun instagramScript(
        mutedUsernames: List<String>,
        strictFocusMode: Boolean,
        singleDmReelId: String?
    ): String {
        val mutedJson = mutedUsernames.joinToString(prefix = "[", postfix = "]") {
            "\"${it.replace("\"", "\\\"")}\""
        }
        val dmReelLiteral = if (singleDmReelId != null) "\"${singleDmReelId.replace("\"", "")}\"" else "null"
        return """
            (function() {
                if (window.__doomZenIgInit) {
                    window.__doomZenMuted = $mutedJson;
                    window.__doomZenStrictFocus = $strictFocusMode;
                    window.__doomZenDmReelId = $dmReelLiteral;
                    if (window.__doomZenApplyIg) window.__doomZenApplyIg();
                    return;
                }
                window.__doomZenIgInit = true;
                window.__doomZenMuted = $mutedJson;
                window.__doomZenStrictFocus = $strictFocusMode;
                window.__doomZenDmReelId = $dmReelLiteral;
                window.__doomZenLastPath = window.location.pathname || '/';

                var lastY = window.scrollY || 0;
                var scrollTick = 0;

                window.addEventListener('scroll', function() {
                    var y = window.scrollY || 0;
                    var delta = y - lastY;
                    if (Math.abs(delta) > 24) {
                        if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                            window.DoomZenBridge.onScrollDirection(delta > 0);
                        }
                        scrollTick++;
                        if (scrollTick % 3 === 0 && window.DoomZenBridge && window.DoomZenBridge.onUserScroll) {
                            window.DoomZenBridge.onUserScroll();
                        }
                        lastY = y;
                    }
                }, { passive: true });

                function enforceIgRoute() {
                    var path = (window.location.pathname || '/').toLowerCase();
                    var prevPath = (window.__doomZenLastPath || '/').toLowerCase();

                    if (path === '/explore' || path.indexOf('/explore/') === 0) {
                        window.location.replace('https://www.instagram.com/');
                        return;
                    }
                    if (path === '/reels' || path === '/reels/' || path.indexOf('/reels/videos') === 0) {
                        window.location.replace('https://www.instagram.com/');
                        return;
                    }

                    var match = path.match(/^\/reels?\/([^\/]+)/);
                    if (match) {
                        var id = match[1];
                        if (window.__doomZenStrictFocus) {
                            window.location.replace('https://www.instagram.com/');
                            return;
                        }
                        if (prevPath.indexOf('/direct') === 0) {
                            window.__doomZenDmReelId = id;
                        }
                        if (!window.__doomZenDmReelId || window.__doomZenDmReelId !== id) {
                            window.__doomZenDmReelId = null;
                            window.location.replace('https://www.instagram.com/direct/inbox/');
                            return;
                        }
                    } else if (path.indexOf('/direct') !== 0) {
                        window.__doomZenDmReelId = null;
                    }

                    window.__doomZenLastPath = path;
                }

                function applyIgDomFirewall() {
                    enforceIgRoute();

                    // Hide Reels & Explore navigation links
                    var navLinks = document.querySelectorAll('a[href*="/reels"], a[href*="/explore"]');
                    navLinks.forEach(function(el) {
                        var href = (el.getAttribute('href') || '').toLowerCase();
                        if (href === '/reels/' || href === '/reels' || href === '/explore/' || href === '/explore') {
                            el.style.display = 'none';
                        }
                    });

                    // Best-effort suppression of Suggested / Recommended feed cards
                    var spans = document.querySelectorAll('span, div');
                    for (var i = 0; i < spans.length; i++) {
                        var txt = (spans[i].textContent || '').trim();
                        if (txt === 'Suggested for you' || txt === 'Suggested posts' || txt === 'Because you watched' || txt === 'Suggested Reels') {
                            var article = spans[i].closest('article');
                            if (article) article.style.display = 'none';
                        }
                    }

                    // Local Story mute: dim and mark muted story tiles without altering IG account settings
                    var muted = window.__doomZenMuted || [];
                    var storyButtons = document.querySelectorAll('[role="menuitem"], button[aria-label*="Story by"], div[role="button"][aria-label*="Story"]');
                    storyButtons.forEach(function(btn) {
                        var label = (btn.getAttribute('aria-label') || btn.textContent || '').toLowerCase();
                        var isMuted = false;
                        for (var m = 0; m < muted.length; m++) {
                            if (label.indexOf(muted[m].toLowerCase()) !== -1) {
                                isMuted = true;
                                break;
                            }
                        }
                        btn.style.opacity = isMuted ? '0.2' : '';
                        if (isMuted) {
                            btn.setAttribute('data-zen-muted', 'true');
                        } else {
                            btn.removeAttribute('data-zen-muted');
                        }
                    });
                }

                window.__doomZenApplyIg = applyIgDomFirewall;

                // Long-press on Story tile to mute/unmute locally
                var pressTimer = null;
                document.addEventListener('touchstart', function(e) {
                    var target = e.target && e.target.closest ? e.target.closest('[aria-label*="Story"]') : null;
                    if (!target) return;
                    pressTimer = setTimeout(function() {
                        var label = target.getAttribute('aria-label') || '';
                        var match = label.match(/Story by ([^,]+)/i);
                        var user = match ? match[1].trim() : (target.textContent || '').trim();
                        if (user && window.DoomZenBridge && window.DoomZenBridge.onStoryLongPress) {
                            window.DoomZenBridge.onStoryLongPress(user);
                        }
                    }, 550);
                }, { passive: true });
                document.addEventListener('touchend', function() { if (pressTimer) clearTimeout(pressTimer); }, { passive: true });
                document.addEventListener('touchmove', function() { if (pressTimer) clearTimeout(pressTimer); }, { passive: true });

                var origPush = history.pushState;
                history.pushState = function() {
                    origPush.apply(this, arguments);
                    setTimeout(applyIgDomFirewall, 40);
                };
                var origReplace = history.replaceState;
                history.replaceState = function() {
                    origReplace.apply(this, arguments);
                    setTimeout(applyIgDomFirewall, 40);
                };
                window.addEventListener('popstate', function() {
                    setTimeout(applyIgDomFirewall, 40);
                });

                var observer = new MutationObserver(function() {
                    applyIgDomFirewall();
                });
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                }
                applyIgDomFirewall();
            })();
        """.trimIndent()
    }

    fun threadsScript(): String = """
        (function() {
            if (window.__doomZenThreadsInit) return;
            window.__doomZenThreadsInit = true;

            var lastY = window.scrollY || 0;
            var scrollTick = 0;

            window.addEventListener('scroll', function() {
                var y = window.scrollY || 0;
                var delta = y - lastY;
                if (Math.abs(delta) > 24) {
                    if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                        window.DoomZenBridge.onScrollDirection(delta > 0);
                    }
                    scrollTick++;
                    if (scrollTick % 3 === 0 && window.DoomZenBridge && window.DoomZenBridge.onUserScroll) {
                        window.DoomZenBridge.onUserScroll();
                    }
                    lastY = y;
                }
            }, { passive: true });

            function preferFollowingTab() {
                var tabs = document.querySelectorAll('a, button, div[role="tab"]');
                for (var i = 0; i < tabs.length; i++) {
                    var label = (tabs[i].textContent || '').trim();
                    if (label === 'Following' && tabs[i].getAttribute('aria-selected') !== 'true') {
                        tabs[i].click();
                        break;
                    }
                }
            }

            setTimeout(preferFollowingTab, 900);
        })();
    """.trimIndent()
}
