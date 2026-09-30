package com.prathyushin.doomscroll.core.policy

import com.prathyushin.doomscroll.core.model.ContentItem
import com.prathyushin.doomscroll.core.model.ContentSource
import com.prathyushin.doomscroll.core.model.ContentType

enum class PolicyDecision { ALLOW, FILTER, STOP }

data class PolicyResult(
    val decision: PolicyDecision,
    val reason: String
)

class DoomPolicyEngine {

    fun evaluate(item: ContentItem): PolicyResult {
        if (item.isHiddenByUser) {
            return PolicyResult(PolicyDecision.FILTER, "Hidden by user")
        }

        return when (item.source) {
            ContentSource.RECOMMENDED ->
                PolicyResult(PolicyDecision.FILTER, "Recommendations are disabled")

            ContentSource.SPONSORED ->
                PolicyResult(PolicyDecision.FILTER, "Sponsored content is not part of the Doom Scroll feed")

            ContentSource.FOLLOWING,
            ContentSource.SEARCH ->
                PolicyResult(PolicyDecision.ALLOW, "Intentional content")
        }
    }

    fun nextShortVideoAllowed(
        requestedExplicitly: Boolean,
        previousShortVideoWasWatched: Boolean
    ): Boolean {
        if (!requestedExplicitly) return false
        if (previousShortVideoWasWatched) return false
        return true
    }

    fun shouldNotifyForFollowedPersonUpload(): Boolean = false
}
