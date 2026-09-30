package com.prathyushin.doomscroll.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

data class PkcePair(val verifier: String, val challenge: String)

object Pkce {
    fun create(): PkcePair {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val verifier = Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        val challenge = Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return PkcePair(verifier, challenge)
    }
}
