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
        assertFalse(settings.hideReelsEntryPoints)
    }

    @Test
    fun cleanAndFocusModesEnableOptionalFeedRestrictions() {
        listOf(FeedProfile.CLEAN_FEED, FeedProfile.FOCUS_MODE).forEach { profile ->
            assertTrue(profile.defaults.hideRecommendations)
            assertTrue(profile.defaults.hideSuggestedAccounts)
            assertTrue(profile.defaults.hideReelsEntryPoints)
        }
    }

    @Test
    fun photoModeOnlyHidesReelsEntryPoints() {
        val settings = FeedProfile.PHOTO_MODE.defaults
        assertTrue(settings.hideReelsEntryPoints)
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

    @Test
    fun learningTopicsAreNormalizedAndInvalidEntriesRemoved() {
        val settings = LearningFocusSettings(
            enabled = true,
            topics = setOf("  Kotlin  ", "Android Development", "x", "bad/topic")
        ).normalized()
        assertEquals(setOf("kotlin", "android development"), settings.topics)
    }

    @Test
    fun focusSessionLimitIsBoundedAndCheckpointsDefaultOff() {
        assertEquals(240, FocusSettings(sessionLimitMinutes = 900).normalized().sessionLimitMinutes)
        assertEquals(0, FocusSettings(sessionLimitMinutes = -10).normalized().sessionLimitMinutes)
        assertFalse(FocusSettings().scrollCheckpoints)
    }

    @Test
    fun scrollPauseDefaultsOffAndBoundsItsControls() {
        val defaults = FocusSettings()
        assertFalse(defaults.scrollPauseEnabled)
        assertEquals(12, defaults.scrollBurstThreshold)
        assertEquals(20, defaults.pauseCooldownMinutes)

        val bounded = FocusSettings(
            scrollBurstThreshold = 100,
            pauseCooldownMinutes = 1
        ).normalized()
        assertEquals(30, bounded.scrollBurstThreshold)
        assertEquals(5, bounded.pauseCooldownMinutes)
    }

    @Test
    fun privacyAnalyticsDefaultsOff() {
        assertFalse(PrivacyPreferences().analyticsEnabled)
    }
}
