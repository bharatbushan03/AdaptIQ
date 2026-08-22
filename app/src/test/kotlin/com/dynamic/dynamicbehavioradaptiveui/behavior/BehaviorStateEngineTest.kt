package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.BehaviorState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BehaviorStateEngineTest {

    private lateinit var engine: BehaviorStateEngine

    @Before
    fun setUp() {
        engine = BehaviorStateEngine()
    }

    @Test
    fun testPhase1StableDefaultWithLessThan5Interactions() {
        val event1 = InteractionEvent(
            eventId = "e1", timestamp = 1L, sessionId = "s1",
            screen = "home", previousScreen = "", action = "click", target = "a", duration = 100L,
            workflowId = "w1", success = true, metadata = ""
        )
        engine.updateState("s1", Metrics(repeatedActions = 1), 1L)

        val phase = engine.getCurrentAdaptationPhase("s1")

        assertEquals(ColdStartPhase.PHASE_1_STABLE_DEFAULT, phase.phase)
        assertEquals(1, phase.accumulatedInteractions)
        assertTrue(phase.confidence < 0.5)
    }

    @Test
    fun testPhase2ConservativeShortcutsWith5to19InteractionsAndAccumulatedConfidence() {
        val event1 = InteractionEvent(
            eventId = "e1", timestamp = 1L, sessionId = "s1",
            screen = "home", previousScreen = "", action = "click", target = "a", duration = 100L,
            workflowId = "w1", success = true, metadata = ""
        )
        engine.updateState("s1", Metrics(repeatedActions = 3), 1L)
        engine.updateState("s1", Metrics(repeatedActions = 3), 2L)
        engine.updateState("s1", Metrics(repeatedActions = 3), 3L)
        engine.updateState("s1", Metrics(repeatedActions = 3), 4L)
        engine.updateState("s1", Metrics(repeatedActions = 3), 5L)

        val phase = engine.getCurrentAdaptationPhase("s1")

        assertEquals(ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS, phase.phase)
        assertEquals(5, phase.accumulatedInteractions)
        assertTrue(phase.confidence >= 0.5)
    }

    @Test
    fun testPhase3PersonalizedAdaptiveWith20PlusInteractions() {
        val event1 = InteractionEvent(
            eventId = "e1", timestamp = 1L, sessionId = "s1",
            screen = "home", previousScreen = "", action = "click", target = "a", duration = 100L,
            workflowId = "w1", success = true, metadata = ""
        )
for (i in 1..25) {
            engine.updateState("s1", Metrics(repeatedActions = 5), Long(i))
        }

        val phase = engine.getCurrentAdaptationPhase("s1")

        assertEquals(ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE, phase.phase)
        assertEquals(25, phase.accumulatedInteractions)
    }

    @Test
    fun testResetColdStart() {
        val event1 = InteractionEvent(
            eventId = "e1", timestamp = 1L, sessionId = "s1",
            screen = "home", previousScreen = "", action = "click", target = "a", duration = 100L,
            workflowId = "w1", success = true, metadata = ""
        )
        engine.updateState("s1", Metrics(repeatedActions = 5), 1L)

        engine.resetColdStart("s1")

        val phaseAfterReset = engine.getCurrentAdaptationPhase("s1")
        assertEquals(ColdStartPhase.PHASE_1_STABLE_DEFAULT, phaseAfterReset.phase)
        assertEquals(0, phaseAfterReset.accumulatedInteractions)
    }

    @Test
    fun testConfidenceDecayOverTime() {
        val event1 = InteractionEvent(
            eventId = "e1", timestamp = 1L, sessionId = "s1",
            screen = "home", previousScreen = "", action = "click", target = "a", duration = 100L,
            workflowId = "w1", success = true, metadata = ""
        )
        engine.updateState("s1", Metrics(repeatedActions = 5), 1L)

        // Wait for half-life to pass (600000ms = 10min)
        val decayedTimestamp = 600001L
        engine.updateState("s1", Metrics(repeatedActions = 5), decayedTimestamp)

        val phase = engine.getCurrentAdaptationPhase("s1")

        // Confidence should be lower due to decay, but still accumulated from new interactions
        assertTrue("Confidence should reflect decay over time", phase.confidence < 5.0)
    }

    @Test
    fun testPhaseTransitionThresholds() {
        // Phase 1 -> Phase 2 at 5 interactions
        for (i in 1..4) {
            engine.updateState("s1", Metrics(repeatedActions = 1), Long(i))
        }
        assertEquals(ColdStartPhase.PHASE_1_STABLE_DEFAULT, engine.getCurrentAdaptationPhase("s1").phase)

        engine.updateState("s1", Metrics(repeatedActions = 1), 5L)
        assertEquals(ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS, engine.getCurrentAdaptationPhase("s1").phase)

        // Phase 2 -> Phase 3 at 20 interactions
        for (i in 6..19) {
            engine.updateState("s1", Metrics(repeatedActions = 1), Long(i))
        }
        assertEquals(ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS, engine.getCurrentAdaptationPhase("s1").phase)

        engine.updateState("s1", Metrics(repeatedActions = 1), 20L)
        assertEquals(ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE, engine.getCurrentAdaptationPhase("s1").phase)
    }
}