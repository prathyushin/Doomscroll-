package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.NotificationCategory
import com.prathyushin.doomscroll.model.NotificationFilterConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFilterEngineTest {

    private val engine = NotificationFilterEngine()

    @Test
    fun keepsDirectMessagesAndSuppressesLikesByDefault() {
        val config = NotificationFilterConfig(
            masterEnabled = true,
            filterInstagramApp = true,
            filterThreadsApp = true
        )

        val dm = engine.evaluate(
            NotificationFilterEngine.INSTAGRAM_PACKAGE,
            "alex",
            "sent you a message",
            config
        )
        assertTrue(dm.shouldKeep)
        assertEquals(NotificationCategory.DIRECT_MESSAGES, dm.category)

        val like = engine.evaluate(
            NotificationFilterEngine.INSTAGRAM_PACKAGE,
            "alex",
            "liked your photo",
            config
        )
        assertFalse(like.shouldKeep)
        assertEquals(NotificationCategory.LIKES, like.category)
    }

    @Test
    fun keepsDmSharedReelsWhenEnabled() {
        val config = NotificationFilterConfig(
            masterEnabled = true,
            filterInstagramApp = true
        )
        val sharedReel = engine.evaluate(
            NotificationFilterEngine.INSTAGRAM_PACKAGE,
            "sam",
            "sent a reel by creator",
            config
        )
        assertTrue(sharedReel.shouldKeep)
        assertEquals(NotificationCategory.DM_SHARED_REELS, sharedReel.category)
    }

    @Test
    fun alwaysAllowsWhenMasterDisabled() {
        val config = NotificationFilterConfig(masterEnabled = false, filterInstagramApp = true)
        val like = engine.evaluate(
            NotificationFilterEngine.INSTAGRAM_PACKAGE,
            "alex",
            "liked your photo",
            config
        )
        assertTrue(like.shouldKeep)
    }
}
