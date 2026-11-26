package com.prathyushin.doomscroll.webview

/**
 * Lightweight, privacy-first DOM & SPA navigation scripts for Instagram and Threads.
 * - Instagram: preserves Search/Explore bar & results, hides mindless Explore grid & Reels tab,
 *   allows single intentional Reel from DMs or Search, blocks next-Reel scrolling.
 * - Threads: keeps navigation inside WebView, suppresses "Open in app" nags, prefers Following tab.
 * - Calm dock signaling: only hides dock after sustained downward scroll (>120px) and reveals at top,
 *   never popping up repeatedly on small scroll bounces.
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
                var downAccum = 0;
                var scrollTick = 0;
                var dockHiddenByScroll = false;

                window.addEventListener('scroll', function() {
                    var y = window.scrollY || 0;
                    var delta = y - lastY;
                    lastY = y;

                    if (y <= 32 && dockHiddenByScroll) {
                        dockHiddenByScroll = false;
                        downAccum = 0;
                        if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                            window.DoomZenBridge.onScrollDirection(false);
                        }
                    } else if (delta > 0) {
                        downAccum += delta;
                        if (downAccum > 140 && !dockHiddenByScroll) {
                            dockHiddenByScroll = true;
                            if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                                window.DoomZenBridge.onScrollDirection(true);
                            }
                        }
                    } else if (delta < -20) {
                        downAccum = 0;
                    }

                    if (Math.abs(delta) > 36) {
                        scrollTick++;
                        if (scrollTick % 3 === 0 && window.DoomZenBridge && window.DoomZenBridge.onUserScroll) {
                            window.DoomZenBridge.onUserScroll();
                        }
                    }
                }, { passive: true });

                function enforceIgRoute() {
                    var path = (window.location.pathname || '/').toLowerCase();
                    var prevPath = (window.__doomZenLastPath || '/').toLowerCase();

                    // Block endless Reels feed tab, but keep /explore/ open for intentional Search
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
                        if (prevPath.indexOf('/direct') === 0 || prevPath.indexOf('/explore') === 0 || (prevPath !== '/' && prevPath.indexOf('/reel') !== 0)) {
                            window.__doomZenDmReelId = id;
                        }
                        if (!window.__doomZenDmReelId || window.__doomZenDmReelId !== id) {
                            window.__doomZenDmReelId = null;
                            window.location.replace('https://www.instagram.com/');
                            return;
                        }
                    } else if (path.indexOf('/direct') !== 0 && path.indexOf('/explore') !== 0) {
                        window.__doomZenDmReelId = null;
                    }

                    window.__doomZenLastPath = path;
                }

                function applyIgDomFirewall() {
                    enforceIgRoute();

                    var path = (window.location.pathname || '/').toLowerCase();

                    // Hide Reels navigation tab, but KEEP Search/Explore nav link visible!
                    var reelsLinks = document.querySelectorAll('a[href="/reels/"], a[href="/reels"]');
                    reelsLinks.forEach(function(el) {
                        el.style.display = 'none';
                    });

                    // On /explore/ root (when no search query is typed), hide only the mindless recommendation grid while keeping the Search bar & results usable
                    if (path === '/explore' || path === '/explore/') {
                        var searchInput = document.querySelector('input[type="search"], input[aria-label*="Search"], input[placeholder*="Search"]');
                        var hasQuery = searchInput && (searchInput.value || '').trim().length > 0;
                        var mainGrid = document.querySelector('main div[style*="flex-direction: column"] > div > div > div > a[href*="/p/"], main div a[href*="/reel/"]');
                        if (mainGrid && mainGrid.closest('main')) {
                            var gridContainer = mainGrid.closest('div[style*="position: relative"]') || mainGrid.parentElement;
                            if (gridContainer) {
                                gridContainer.style.display = hasQuery ? '' : 'none';
                            }
                        }
                    }

                    // Best-effort suppression of Suggested / Recommended home feed cards
                    var spans = document.querySelectorAll('span, div');
                    for (var i = 0; i < spans.length; i++) {
                        var txt = (spans[i].textContent || '').trim();
                        if (txt === 'Suggested for you' || txt === 'Suggested posts' || txt === 'Because you watched' || txt === 'Suggested Reels') {
                            var article = spans[i].closest('article');
                            if (article) article.style.display = 'none';
                        }
                    }

                    // Local Story mute: dim muted story tiles without modifying IG account settings
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
            var downAccum = 0;
            var scrollTick = 0;
            var dockHiddenByScroll = false;

            window.addEventListener('scroll', function() {
                var y = window.scrollY || 0;
                var delta = y - lastY;
                lastY = y;

                if (y <= 32 && dockHiddenByScroll) {
                    dockHiddenByScroll = false;
                    downAccum = 0;
                    if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                        window.DoomZenBridge.onScrollDirection(false);
                    }
                } else if (delta > 0) {
                    downAccum += delta;
                    if (downAccum > 140 && !dockHiddenByScroll) {
                        dockHiddenByScroll = true;
                        if (window.DoomZenBridge && window.DoomZenBridge.onScrollDirection) {
                            window.DoomZenBridge.onScrollDirection(true);
                        }
                    }
                } else if (delta < -20) {
                    downAccum = 0;
                }

                if (Math.abs(delta) > 36) {
                    scrollTick++;
                    if (scrollTick % 3 === 0 && window.DoomZenBridge && window.DoomZenBridge.onUserScroll) {
                        window.DoomZenBridge.onUserScroll();
                    }
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

            setTimeout(preferFollowingTab, 1200);
        })();
    """.trimIndent()
}
