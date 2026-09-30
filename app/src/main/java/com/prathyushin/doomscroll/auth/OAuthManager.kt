package com.prathyushin.doomscroll.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import kotlin.concurrent.thread

class OAuthManager(private val context: Context) {
    private val store = SecureTokenStore(context)
    private val prefs = context.getSharedPreferences("doomscroll_oauth", Context.MODE_PRIVATE)

    fun begin(config: OAuthConfig) {
        val pkce = Pkce.create()
        val state = UUID.randomUUID().toString()
        prefs.edit()
            .putString("state_${config.platform.name}", state)
            .putString("verifier_${config.platform.name}", pkce.verifier)
            .apply()
        context.startActivity(Intent(Intent.ACTION_VIEW, config.authorizationUri(state, pkce.challenge)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun handleCallback(uri: Uri, configs: Map<Platform, OAuthConfig>, onComplete: (Platform, Boolean) -> Unit) {
        val platform = when (uri.host) {
            "instagram" -> Platform.INSTAGRAM
            "threads" -> Platform.THREADS
            else -> return
        }
        val config = configs[platform] ?: return
        val expectedState = prefs.getString("state_${platform.name}", null)
        if (uri.getQueryParameter("state") != expectedState) return
        val code = uri.getQueryParameter("code") ?: return
        val verifier = prefs.getString("verifier_${platform.name}", null) ?: return

        thread(name = "oauth-token-exchange") {
            val ok = exchangeCode(config, code, verifier)
            prefs.edit().remove("state_${platform.name}").remove("verifier_${platform.name}").apply()
            onComplete(platform, ok)
        }
    }

    fun isConnected(platform: Platform) = store.get(platform) != null
    fun disconnect(platform: Platform) = store.clear(platform)

    private fun exchangeCode(config: OAuthConfig, code: String, verifier: String): Boolean = runCatching {
        val body = listOf(
            "client_id" to config.clientId,
            "redirect_uri" to config.redirectUri,
            "grant_type" to "authorization_code",
            "code" to code,
            "code_verifier" to verifier
        ).joinToString("&") { (k, v) -> "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}" }

        val connection = URL(config.tokenEndpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.outputStream.use { it.write(body.toByteArray()) }
        val response = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
            .bufferedReader().use { it.readText() }
        val token = Regex("""["']access_token["']\s*:\s*["']([^"']+)["']""").find(response)?.groupValues?.get(1)
            ?: return false
        store.save(config.platform, token)
        true
    }.getOrDefault(false)
}
