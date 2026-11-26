package com.prathyushin.doomscroll.policy

import java.net.URI

enum class RouteAction {
    ALLOW,
    ALLOW_SINGLE_DM_REEL,
    BLOCK_REDIRECT_HOME,
    OPEN_EXTERNAL_BROWSER
}

data class FirewallDecision(
    val action: RouteAction,
    val reason: String,
    val redirectUrl: String = "https://www.instagram.com/"
)

/**
 * Instagram Feed Firewall route policy.
 * Preserves posts, Stories, profiles, DMs, comments, likes, and intentional Search/Explore,
 * while restricting the Reels feed tab and endless next-Reel consumption.
 * Allows a single Reel intentionally opened from DMs or Search results.
 */
class InstagramFirewallPolicy {

    private var activeIntentionalReelId: String? = null

    fun resetDmReelAllowance() {
        activeIntentionalReelId = null
    }

    fun evaluateUrl(
        targetUrl: String,
        refererUrl: String? = null,
        strictFocusMode: Boolean = false
    ): FirewallDecision {
        val uri = runCatching { URI(targetUrl) }.getOrNull()
            ?: return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "Malformed URL")

        val scheme = uri.scheme?.lowercase().orEmpty()
        if (scheme == "intent" || scheme == "instagram") {
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Keep navigation inside Doom Scroll")
        }

        val host = uri.host?.lowercase()
            ?: return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "Missing host")

        if (isAuthOrCdnHost(host)) {
            return FirewallDecision(RouteAction.ALLOW, "Auth or CDN host")
        }

        if (!isInstagramHost(host)) {
            return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "External destination")
        }

        val path = (uri.path ?: "/").lowercase()

        // Allow /explore/ for intentional search (mindless discovery grid is hidden via DOM CSS/JS)
        if (path == "/explore" || path.startsWith("/explore/")) {
            activeIntentionalReelId = null
            return FirewallDecision(RouteAction.ALLOW, "Intentional Search & Explore")
        }

        // Block endless Reels feed tab
        if (path == "/reels" || path == "/reels/" || path.startsWith("/reels/videos")) {
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Reels feed is restricted")
        }

        // Single Reel (/reel/<id>/ or /reels/<id>/)
        val reelId = extractReelId(path)
        if (reelId != null) {
            if (strictFocusMode) {
                return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Reels are blocked during Focus Schedule")
            }

            if (isIntentionalReelSource(refererUrl)) {
                activeIntentionalReelId = reelId
                return FirewallDecision(RouteAction.ALLOW_SINGLE_DM_REEL, "Single intentional Reel opened")
            }

            if (activeIntentionalReelId == reelId) {
                return FirewallDecision(RouteAction.ALLOW_SINGLE_DM_REEL, "Active single intentional Reel")
            }

            // Navigating to a different Reel -> block endless next-Reel consumption
            activeIntentionalReelId = null
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Next-Reel scrolling is restricted")
        }

        if (!path.startsWith("/direct") && !path.startsWith("/explore")) {
            activeIntentionalReelId = null
        }

        return FirewallDecision(RouteAction.ALLOW, "Intentional Instagram route")
    }

    private fun extractReelId(path: String): String? {
        val segments = path.trim('/').split('/').filter { it.isNotEmpty() }
        if (segments.size >= 2 && (segments[0] == "reel" || segments[0] == "reels")) {
            return segments[1]
        }
        return null
    }

    private fun isIntentionalReelSource(refererUrl: String?): Boolean {
        if (refererUrl.isNullOrBlank()) return false
        val uri = runCatching { URI(refererUrl) }.getOrNull() ?: return false
        val path = (uri.path ?: "").lowercase()
        return path.startsWith("/direct") ||
            path.startsWith("/explore") ||
            path.startsWith("/p/") ||
            (path != "/" && !path.startsWith("/reel"))
    }

    private fun isInstagramHost(host: String): Boolean =
        host == "instagram.com" || host.endsWith(".instagram.com")

    private fun isAuthOrCdnHost(host: String): Boolean =
        host.endsWith("facebook.com") ||
            host.endsWith("fbcdn.net") ||
            host.endsWith("cdninstagram.com") ||
            host.endsWith("meta.com") ||
            host == "l.instagram.com"
}
