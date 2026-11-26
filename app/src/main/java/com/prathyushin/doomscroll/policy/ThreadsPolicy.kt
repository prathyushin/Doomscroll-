package com.prathyushin.doomscroll.policy

import java.net.URI

enum class ThreadsRouteAction {
    ALLOW,
    ALLOW_AUTH_REDIRECT,
    BLOCK_APP_HANDOFF,
    BLOCK_LOOP_REDIRECT,
    OPEN_EXTERNAL_BROWSER
}

data class ThreadsDecision(
    val action: ThreadsRouteAction,
    val reason: String
)

/**
 * Threads route, app-handoff suppression, and redirect-loop protection policy.
 */
class ThreadsPolicy {
    private var lastRedirectUrl: String? = null
    private var consecutiveSameRedirects: Int = 0

    fun evaluateUrl(targetUrl: String): ThreadsDecision {
        // Intercept Meta's "Open in Threads app" deep links so WebView stays intact
        val lower = targetUrl.lowercase()
        if (lower.startsWith("intent://") || lower.startsWith("barcelona://") || lower.startsWith("threads://")) {
            return ThreadsDecision(ThreadsRouteAction.BLOCK_APP_HANDOFF, "Stay inside Doom Scroll WebView")
        }

        val uri = runCatching { URI(targetUrl) }.getOrNull()
            ?: return ThreadsDecision(ThreadsRouteAction.OPEN_EXTERNAL_BROWSER, "Malformed URL")

        val host = uri.host?.lowercase()
            ?: return ThreadsDecision(ThreadsRouteAction.OPEN_EXTERNAL_BROWSER, "Missing host")

        if (targetUrl == lastRedirectUrl) {
            consecutiveSameRedirects++
            if (consecutiveSameRedirects > 5) {
                return ThreadsDecision(ThreadsRouteAction.BLOCK_LOOP_REDIRECT, "Prevented redirect loop")
            }
        } else {
            lastRedirectUrl = targetUrl
            consecutiveSameRedirects = 0
        }

        if (isThreadsHost(host)) {
            return ThreadsDecision(ThreadsRouteAction.ALLOW, "Threads destination")
        }

        if (isMetaAuthHost(host)) {
            return ThreadsDecision(ThreadsRouteAction.ALLOW_AUTH_REDIRECT, "Meta SSO / Instagram login")
        }

        return ThreadsDecision(ThreadsRouteAction.OPEN_EXTERNAL_BROWSER, "External link")
    }

    fun resetLoopGuard() {
        lastRedirectUrl = null
        consecutiveSameRedirects = 0
    }

    private fun isThreadsHost(host: String): Boolean =
        host == "threads.com" || host.endsWith(".threads.com") ||
            host == "threads.net" || host.endsWith(".threads.net")

    private fun isMetaAuthHost(host: String): Boolean =
        host == "instagram.com" || host.endsWith(".instagram.com") ||
            host.endsWith("facebook.com") || host.endsWith("meta.com") ||
            host.endsWith("fbcdn.net") || host.endsWith("cdninstagram.com")
}
