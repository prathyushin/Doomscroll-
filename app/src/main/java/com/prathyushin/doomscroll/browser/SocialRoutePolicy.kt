package com.prathyushin.doomscroll.browser

object SocialRoutePolicy {
    fun isBlockedInstagramPath(path: String): Boolean {
        val normalized = normalize(path)
        if (normalized == "/explore" || normalized.startsWith("/explore/") ||
            normalized == "/reels" || normalized.startsWith("/reels/")) return true

        val segments = normalized.split('/').filter { it.isNotBlank() }
        return segments.size == 2 && segments[1] == "reels"
        // Singular /reel/{shortcode}/ is handled separately and is DM-only.
    }

    fun isSingleReelPath(path: String): Boolean =
        Regex("^/reel/[^/]+/?$", RegexOption.IGNORE_CASE).matches(normalize(path))

    fun isDmSourcePath(path: String): Boolean =
        normalize(path).lowercase().startsWith("/direct/")

    fun shouldBlockInstagramNavigation(destinationPath: String, sourcePath: String): Boolean =
        isBlockedInstagramPath(destinationPath) ||
            (isSingleReelPath(destinationPath) && !isDmSourcePath(sourcePath))

    fun isAllowedHost(host: String, provider: SocialProvider): Boolean {
        val normalized = host.lowercase().trimEnd('.')
        val platformHost = when (provider) {
            SocialProvider.INSTAGRAM ->
                normalized == "instagram.com" || normalized.endsWith(".instagram.com")
            SocialProvider.THREADS ->
                normalized == "threads.com" || normalized.endsWith(".threads.com") ||
                    normalized == "threads.net" || normalized.endsWith(".threads.net")
        }
        val metaAuthHost = normalized == "facebook.com" || normalized.endsWith(".facebook.com") ||
            normalized == "meta.com" || normalized.endsWith(".meta.com")
        return platformHost || metaAuthHost
    }

    private fun normalize(path: String): String {
        val value = path.substringBefore('?').substringBefore('#').ifBlank { "/" }
        return (if (value.startsWith("/")) value else "/$value").lowercase()
    }
}
