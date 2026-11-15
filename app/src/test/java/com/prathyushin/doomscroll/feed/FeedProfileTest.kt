package com.prathyushin.doomscroll.feed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedProfileTest {
    @Test
    fun normalModeLeavesFeedControlsDisabled() {
        val settings = FeedProfile.NORMAL.defaults

        assertFalse(settings.hideRecommendations)
        assertFalse(settings.hideSuggestedAccounts)
        assertFalse(settings.hideReels)
    }

    @Test
    fun cleanAndFocusModesEnableAllOptionalFeedRestrictions() {
        listOf(FeedProfile.CLEAN_FEED, FeedProfile.FOCUS_MODE).forEach { profile ->
            assertTrue(profile.defaults.hideRecommendations)
            assertTrue(profile.defaults.hideSuggestedAccounts)
            assertTrue(profile.defaults.hideReels)
        }
    }

    @Test
    fun photoModeHidesReelsWithoutHidingRecommendationsByDefault() {
        val settings = FeedProfile.PHOTO_MODE.defaults

        assertTrue(settings.hideReels)
        assertFalse(settings.hideRecommendations)
        assertFalse(settings.hideSuggestedAccounts)
    }

    @Test
    fun hiddenAccountsAreNormalizedAndInvalidValuesDiscarded() {
        val settings = FeedFirewallSettings(
            hiddenAccounts = setOf(" @Example.User ", "valid_name", "invalid name", "")
        ).normalized()

        assertEquals(setOf("example.user", "valid_name"), settings.hiddenAccounts)
    }
}
