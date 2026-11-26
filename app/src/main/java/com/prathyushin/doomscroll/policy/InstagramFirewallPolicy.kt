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
 * Preserves posts, Stories, profiles, DMs, comments, likes, and explicit search,
 * while restricting Reels, Explore, and endless next-Reel consumption.
 */
class InstagramFirewallPolicy {

    private var activeDmReelId: String? = null

    fun resetDmReelAllowance() {
        activeDmReelId = null
    }

    fun evaluateUrl(
        targetUrl: String,
        refererUrl: String? = null,
        strictFocusMode: Boolean = false
    ): FirewallDecision {
        val uri = runCatching { URI(targetUrl) }.getOrNull()
            ?: return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "Malformed URL")

        val host = uri.host?.lowercase() ?: return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "Missing host")

        // Allow Meta auth / SSO / CDN hosts inside WebView
        if (isAuthOrCdnHost(host)) {
            return FirewallDecision(RouteAction.ALLOW, "Auth or CDN host")
        }

        if (!isInstagramHost(host)) {
            return FirewallDecision(RouteAction.OPEN_EXTERNAL_BROWSER, "External destination")
        }

        val path = (uri.path ?: "/").lowercase()

        // Block Explore
        if (path == "/explore" || path.startsWith("/explore/")) {
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Explore is restricted in Zen mode")
        }

        // Block Reels feed tab
        if (path == "/reels" || path == "/reels/" || path.startsWith("/reels/videos")) {
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Reels feed is restricted")
        }

        // Single Reel (/reel/<id>/ or /reels/<id>/)
        val reelId = extractReelId(path)
        if (reelId != null) {
            if (strictFocusMode) {
                return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Reels are strictly blocked during Focus Schedule")
            }

            val fromDm = isFromDirectMessages(refererUrl)
            if (fromDm) {
                activeDmReelId = reelId
                return FirewallDecision(RouteAction.ALLOW_SINGLE_DM_REEL, "Single Reel opened from DMs")
            }

            if (activeDmReelId == reelId) {
                return FirewallDecision(RouteAction.ALLOW_SINGLE_DM_REEL, "Active DM-origin Reel")
            }

            // Navigating to a different Reel without coming from DMs -> block endless next-Reel consumption
            activeDmReelId = null
            return FirewallDecision(RouteAction.BLOCK_REDIRECT_HOME, "Next-Reel browsing is restricted")
        }

        // Any non-Reel navigation clears active DM Reel allowance unless staying in /direct/
        if (!path.startsWith("/direct")) {
            activeDmReelId = null
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

    private fun isFromDirectMessages(refererUrl: String?): Boolean {
        if (refererUrl.isNullOrBlank()) return false
        val uri = runCatching { URI(refererUrl) }.getOrNull() ?: return false
        val path = (uri.path ?: "").lowercase()
        return path.startsWith("/direct")
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
