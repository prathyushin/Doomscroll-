package com.prathyushin.doomscroll.feed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoomScrollSettingsTest {
    @Test
    fun feedFirewallNormalizesAndRejectsInvalidAccountNames() {
        val result = FeedFirewallSettings(
            hideRecommendations = true,
            hiddenAccounts = setOf("@Good.User", " spaces ", "UPPER_CASE", "valid_2")
        ).normalized()

        assertTrue(result.hideRecommendations)
        assertEquals(setOf("good.user", "valid_2"), result.hiddenAccounts)
    }

    @Test
    fun focusSettingsClampUserConfiguredValuesToSafeBounds() {
        val result = FocusSettings(
            sessionLimitMinutes = 999,
            scrollBurstThreshold = 1,
            pauseCooldownMinutes = 999
        ).normalized()

        assertEquals(240, result.sessionLimitMinutes)
        assertEquals(6, result.scrollBurstThreshold)
        assertEquals(60, result.pauseCooldownMinutes)
    }

    @Test
    fun learningTopicsAreNormalizedAndInvalidTopicsAreDropped() {
        val result = LearningFocusSettings(
            enabled = true,
            topics = setOf(" Kotlin ", "AI + ML", "x", "topic/with/slash")
        ).normalized()

        assertTrue(result.enabled)
        assertEquals(setOf("kotlin", "ai + ml"), result.topics)
    }

    @Test
    fun normalProfileDoesNotEnableRestrictions() {
        assertFalse(FeedProfile.NORMAL.defaults.hideRecommendations)
        assertFalse(FeedProfile.NORMAL.defaults.hideSuggestedAccounts)
        assertFalse(FeedProfile.NORMAL.defaults.hideReelsEntryPoints)
    }

    @Test
    fun cleanFeedProfileEnablesLocalRecommendationAndReelsControls() {
        val defaults = FeedProfile.CLEAN_FEED.defaults
        assertTrue(defaults.hideRecommendations)
        assertTrue(defaults.hideSuggestedAccounts)
        assertTrue(defaults.hideReelsEntryPoints)
    }
}
