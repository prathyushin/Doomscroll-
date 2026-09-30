package com.prathyushin.doomscroll

import com.prathyushin.doomscroll.core.model.ContentItem
import com.prathyushin.doomscroll.core.model.ContentSource
import com.prathyushin.doomscroll.core.policy.DoomPolicyEngine
import com.prathyushin.doomscroll.core.policy.PolicyDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoomPolicyEngineTest {
    private val engine = DoomPolicyEngine()

    @Test fun recommendationsAreFiltered() {
        val item = ContentItem("r", "Suggested", "x", source = ContentSource.RECOMMENDED)
        assertEquals(PolicyDecision.FILTER, engine.evaluate(item).decision)
    }

    @Test fun followingContentIsAllowed() {
        val item = ContentItem("f", "Friend", "x")
        assertEquals(PolicyDecision.ALLOW, engine.evaluate(item).decision)
    }

    @Test fun shortVideoNeedsIntentAndStopsAfterOne() {
        assertTrue(engine.nextShortVideoAllowed(true, false))
        assertFalse(engine.nextShortVideoAllowed(false, false))
        assertFalse(engine.nextShortVideoAllowed(true, true))
    }

    @Test fun followedPersonUploadsDoNotNotifyByDefault() {
        assertFalse(engine.shouldNotifyForFollowedPersonUpload())
    }
}
