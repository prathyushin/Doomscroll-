package com.prathyushin.doomscroll.policy

import com.prathyushin.doomscroll.model.BehavioralConfig
import com.prathyushin.doomscroll.model.FocusEvaluation
import com.prathyushin.doomscroll.model.InterventionState
import com.prathyushin.doomscroll.model.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BehavioralInterventionEngineTest {

    private val defaultConfig = BehavioralConfig(
        sessionLimitMinutes = 10,
        scrollThreshold = 50,
        reopenThreshold = 3,
        cooldownMinutes = 5
    )

    private val noFocus = FocusEvaluation(isFocusActive = false)

    @Test
    fun transitionsFromActiveToGentlePauseAtSessionLimit() {
        val engine = BehavioralInterventionEngine()
        engine.startOrResumeSession("INSTAGRAM", elapsedMs = 1_000L, wallMs = 100_000L)
        assertEquals(InterventionState.ACTIVE, engine.state().state)

        engine.tickSeconds(600L, defaultConfig, noFocus, elapsedMs = 601_000L, wallMs = 700_000L)
        assertEquals(InterventionState.GENTLE_PAUSE, engine.state().state)
        assertTrue(engine.state().pauseReason.contains("10 min"))
    }

    @Test
    fun continueIntentionallyStartsCooldownAndRecoversToActive() {
        val engine = BehavioralInterventionEngine()
        engine.startOrResumeSession("THREADS", elapsedMs = 1_000L, wallMs = 100_000L)
        engine.tickSeconds(600L, defaultConfig, noFocus, elapsedMs = 601_000L, wallMs = 700_000L)

        engine.acknowledgeAndContinue(defaultConfig, noFocus, elapsedMs = 602_000L, wallMs = 701_000L)
        assertEquals(InterventionState.COOLDOWN, engine.state().state)
        assertEquals(300L, engine.state().cooldownRemainingSeconds)

        engine.tickSeconds(300L, defaultConfig, noFocus, elapsedMs = 902_000L, wallMs = 1_001_000L)
        assertEquals(InterventionState.ACTIVE, engine.state().state)
    }

    @Test
    fun focusOverrideEnforcesShorterSessionLimit() {
        val engine = BehavioralInterventionEngine()
        engine.startOrResumeSession("INSTAGRAM", elapsedMs = 1_000L, wallMs = 100_000L)

        val activeFocus = FocusEvaluation(
            isFocusActive = true,
            strictReelsBlock = true,
            effectiveSessionLimitMinutes = 5
        )
        engine.tickSeconds(300L, defaultConfig, activeFocus, elapsedMs = 301_000L, wallMs = 400_000L)
        assertEquals(InterventionState.GENTLE_PAUSE, engine.state().state)
        assertTrue(engine.state().pauseReason.contains("Focus"))
    }

    @Test
    fun recoverStateResetsStaleSessionAfterLongAbsence() {
        val engine = BehavioralInterventionEngine()
        val stale = SessionState(
            platformId = "INSTAGRAM",
            state = InterventionState.ACTIVE,
            sessionSeconds = 500L,
            lastUpdatedWallMs = 1_000_000L
        )
        val recovered = engine.recoverState(stale, currentElapsedMs = 50_000L, currentWallMs = 1_000_000L + 20 * 60_000L)
        assertEquals(InterventionState.IDLE, recovered.state)
        assertEquals(0L, recovered.sessionSeconds)
    }
}
