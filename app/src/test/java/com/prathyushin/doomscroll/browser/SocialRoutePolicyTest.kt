package com.prathyushin.doomscroll.browser

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialRoutePolicyTest {
    @Test fun blocksExploreAndReelsFeeds() {
        assertTrue(SocialRoutePolicy.isBlockedInstagramPath("/explore/"))
        assertTrue(SocialRoutePolicy.isBlockedInstagramPath("/explore/tags/"))
        assertTrue(SocialRoutePolicy.isBlockedInstagramPath("/reels/"))
        assertTrue(SocialRoutePolicy.isBlockedInstagramPath("/reels/audio/"))
    }

    @Test fun allowsPurposefulInstagramDestinations() {
        assertFalse(SocialRoutePolicy.isBlockedInstagramPath("/"))
        assertFalse(SocialRoutePolicy.isBlockedInstagramPath("/direct/inbox/"))
        assertFalse(SocialRoutePolicy.isBlockedInstagramPath("/stories/friend/123/"))
        assertFalse(SocialRoutePolicy.isBlockedInstagramPath("/reel/abc123/"))
        assertFalse(SocialRoutePolicy.isBlockedInstagramPath("/username/"))
    }

    @Test fun allowsPlatformAndMetaAuthenticationHostsOnly() {
        assertTrue(SocialRoutePolicy.isAllowedHost("www.instagram.com", SocialProvider.INSTAGRAM))
        assertTrue(SocialRoutePolicy.isAllowedHost("threads.com", SocialProvider.THREADS))
        assertTrue(SocialRoutePolicy.isAllowedHost("www.threads.net", SocialProvider.THREADS))
        assertTrue(SocialRoutePolicy.isAllowedHost("m.facebook.com", SocialProvider.INSTAGRAM))
        assertFalse(SocialRoutePolicy.isAllowedHost("example.com", SocialProvider.INSTAGRAM))
        assertFalse(SocialRoutePolicy.isAllowedHost("instagram.example.com.evil", SocialProvider.INSTAGRAM))
    }
}
