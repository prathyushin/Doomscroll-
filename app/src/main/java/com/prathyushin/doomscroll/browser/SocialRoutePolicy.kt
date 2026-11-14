package com.prathyushin.doomscroll.browser

object SocialRoutePolicy {
    fun isBlockedInstagramPath(path: String): Boolean {
        val normalized = if (path.startsWith("/")) path.lowercase() else "/${path.lowercase()}"
        return normalized == "/explore" || normalized.startsWith("/explore/") ||
            normalized == "/reels" || normalized.startsWith("/reels/")
        // Singular /reel/{shortcode}/ is intentionally allowed.
    }

    fun isAllowedHost(host: String, provider: SocialProvider): Boolean {
        val normalized = host.lowercase()
        val platformHost = when (provider) {
            SocialProvider.INSTAGRAM ->
                normalized == "instagram.com" || normalized.endsWith(".instagram.com")
            SocialProvider.THREADS ->
                normalized == "threads.com" || normalized.endsWith(".threads.com") ||
                    normalized == "threads.net" || normalized.endsWith(".threads.net")
        }
        val metaAuthHost = normalized == "facebook.com" || normalized.endsWith(".facebook.com")
        return platformHost || metaAuthHost
    }
}
