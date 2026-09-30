package com.prathyushin.doomscroll.auth

import android.net.Uri

enum class Platform(val label: String, val scheme: String) { INSTAGRAM("Instagram", "instagram"), THREADS("Threads", "threads") }

data class OAuthConfig(
    val platform: Platform, val clientId: String, val redirectUri: String,
    val authorizeEndpoint: String, val tokenEndpoint: String, val scopes: List<String>
) {
    fun authorizationUri(state: String, codeChallenge: String): Uri =
        Uri.parse(authorizeEndpoint).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", scopes.joinToString(","))
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

    companion object {
        fun instagram(clientId: String) = OAuthConfig(
            Platform.INSTAGRAM, clientId, "doomscroll://oauth/instagram",
            "https://www.instagram.com/oauth/authorize",
            "https://api.instagram.com/oauth/access_token",
            listOf("user_profile", "user_media")
        )
        fun threads(clientId: String) = OAuthConfig(
            Platform.THREADS, clientId, "doomscroll://oauth/threads",
            "https://threads.net/oauth/authorize",
            "https://graph.threads.net/oauth/access_token",
            listOf("threads_basic", "threads_content_publish", "threads_manage_replies")
        )
    }
}
