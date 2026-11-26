package com.prathyushin.doomscroll.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class InstagramFirewallPolicyTest {

    @Test
    fun blocksExploreAndReelsTab() {
        val policy = InstagramFirewallPolicy()
        assertEquals(
            RouteAction.BLOCK_REDIRECT_HOME,
            policy.evaluateUrl("https://www.instagram.com/explore/").action
        )
        assertEquals(
            RouteAction.BLOCK_REDIRECT_HOME,
            policy.evaluateUrl("https://www.instagram.com/reels/").action
        )
    }

    @Test
    fun allowsSingleDmReelAndBlocksNextReel() {
        val policy = InstagramFirewallPolicy()
        val dmReel = policy.evaluateUrl(
            targetUrl = "https://www.instagram.com/reel/ABC123/",
            refererUrl = "https://www.instagram.com/direct/t/999/"
        )
        assertEquals(RouteAction.ALLOW_SINGLE_DM_REEL, dmReel.action)

        // Endless next-reel navigation should be blocked
        val nextReel = policy.evaluateUrl(
            targetUrl = "https://www.instagram.com/reel/XYZ789/",
            refererUrl = "https://www.instagram.com/reel/ABC123/"
        )
        assertEquals(RouteAction.BLOCK_REDIRECT_HOME, nextReel.action)
    }

    @Test
    fun strictFocusBlocksEvenDmReels() {
        val policy = InstagramFirewallPolicy()
        val dmReel = policy.evaluateUrl(
            targetUrl = "https://www.instagram.com/reel/ABC123/",
            refererUrl = "https://www.instagram.com/direct/t/999/",
            strictFocusMode = true
        )
        assertEquals(RouteAction.BLOCK_REDIRECT_HOME, dmReel.action)
    }

    @Test
    fun routesExternalLinksToBrowser() {
        val policy = InstagramFirewallPolicy()
        assertEquals(
            RouteAction.OPEN_EXTERNAL_BROWSER,
            policy.evaluateUrl("https://example.com/article").action
        )
    }
}
