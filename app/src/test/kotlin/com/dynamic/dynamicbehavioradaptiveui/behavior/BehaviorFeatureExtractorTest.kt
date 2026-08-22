package com.dynamic.dynamicbehavioradaptiveui.behavior

import com.dynamic.dynamicbehavioradaptiveui.models.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.UserBehaviorProfile
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BehaviorFeatureExtractorTest {

    private lateinit var extractor: BehaviorFeatureExtractor

    @Before
    fun setUp() {
        extractor = BehaviorFeatureExtractor()
    }

    private fun makeEvent(
        screen: String = "home",
        previousScreen: String = "",
        action: String = "button_click",
        target: String = "feature_a",
        duration: Long = 1000L,
        success: Boolean = true,
        workflowId: String = "workflow_1",
        metadata: String = ""
    ): InteractionEvent = InteractionEvent(
        eventId = "event_1",
        timestamp = System.currentTimeMillis(),
        sessionId = "session_1",
        screen = screen,
        previousScreen = previousScreen,
        action = action,
        target = target,
        duration = duration,
        workflowId = workflowId,
        success = success,
        metadata = metadata
    )

    @Test
    fun testInitialProfileIsZero() {
        val profile = extractor.getProfile()
        assertEquals(0.0, profile.averageDwellTime, 0.001)
        assertEquals(0, profile.navigationDepth)
        assertEquals(0, profile.backtrackingFrequency)
        assertEquals(0.0, profile.workflowCompletionRate, 0.001)
        assertEquals(0.0, profile.workflowAbandonmentRate, 0.001)
        assertEquals(0.0, profile.averageTaskCompletionTime, 0.001)
        assertEquals(0.0, profile.interactionErrorRate, 0.001)
        assertEquals(emptyMap(), profile.attemptsBeforeSuccessfulCompletion)
        assertEquals(emptyList(), profile.frequentlyVisitedScreenSequences)
    }

    @Test
    fun testFeatureUsageFrequencyIncremental() {
        extractor.update(makeEvent(target = "feature_a"))
        extractor.update(makeEvent(target = "feature_b"))
        extractor.update(makeEvent(target = "feature_a"))

        val profile = extractor.getProfile()
        assertEquals(2, profile.featureUsageFrequency["feature_a"])
        assertEquals(1, profile.featureUsageFrequency["feature_b"])
        assertEquals(listOf("feature_a", "feature_b"), profile.mostFrequentlyUsedFeatures)
    }

    @Test
    fun testAverageDwellTimeIncremental() {
        extractor.update(makeEvent(duration = 500L))
        extractor.update(makeEvent(duration = 1500L))

        val profile = extractor.getProfile()
        assertEquals(1000.0, profile.averageDwellTime, 0.001)
    }

    @Test
    fun testNavigationDepthTracking() {
        extractor.update(makeEvent(screen = "home", previousScreen = "", action = "navigate"))
        extractor.update(makeEvent(screen = "settings", previousScreen = "home", action = "navigate"))
        extractor.update(makeEvent(screen = "profile", previousScreen = "settings", action = "navigate"))

        val profile = extractor.getProfile()
        assertEquals(3, profile.navigationDepth)
    }

    @Test
    fun testBacktrackingFrequency() {
        extractor.update(makeEvent(screen = "home", action = "navigate"))
        extractor.update(makeEvent(screen = "settings", previousScreen = "home", action = "back_navigation"))

        val profile = extractor.getProfile()
        assertEquals(1, profile.backtrackingFrequency)
    }

    @Test
    fun testWorkflowCompletionAndAbandonment() {
        val completedEvent = makeEvent(workflowId = "wf_1", success = true)
        val abandonedEvent = makeEvent(workflowId = "wf_2", success = false)

        extractor.update(completedEvent)
        extractor.update(abandonedEvent)

        val profile = extractor.getProfile()
        assertTrue(profile.workflowCompletionRate > 0.0)
        assertTrue(profile.workflowAbandonmentRate > 0.0)
    }

    @Test
    fun testAverageTaskCompletionTime() {
        extractor.update(makeEvent(duration = 3000L, success = true))
        extractor.update(makeEvent(duration = 5000L, success = true))
        extractor.update(makeEvent(duration = 2000L, success = false))

        val profile = extractor.getProfile()
        assertEquals(4000.0, profile.averageTaskCompletionTime, 0.001)
    }

    @Test
    fun testInteractionErrorRate() {
        extractor.update(makeEvent(success = false))
        extractor.update(makeEvent(success = true))
        extractor.update(makeEvent(success = false))

        val profile = extractor.getProfile()
        assertEquals(2.0 / 3.0, profile.interactionErrorRate, 0.001)
    }

    @Test
    fun testAttemptsBeforeSuccessfulCompletion() {
        val event1 = makeEvent(workflowId = "wf_1", success = false)
        val event2 = makeEvent(workflowId = "wf_1", success = true)

        extractor.update(event1)
        extractor.update(event2)

        val profile = extractor.getProfile()
        val attempts = profile.attemptsBeforeSuccessfulCompletion
        assertEquals(2, attempts["wf_1"])
    }

    @Test
    fun testScreenSequencesTracking() {
        extractor.update(makeEvent(screen = "home", previousScreen = "", action = "navigate"))
        extractor.update(makeEvent(screen = "settings", previousScreen = "home", action = "navigate"))
        extractor.update(makeEvent(screen = "home", previousScreen = "settings", action = "navigate"))

        val profile = extractor.getProfile()
        assertTrue(profile.frequentlyVisitedScreenSequences.isNotEmpty())
    }

    @Test
    fun testRepeatedActions() {
        extractor.update(makeEvent(action = "button_click"))
        extractor.update(makeEvent(action = "button_click"))
        extractor.update(makeEvent(action = "feature_select"))

        val profile = extractor.getProfile()
        assertTrue(profile.repeatedActions["button_click"]!!.value > 1)
        assertTrue(profile.repeatedActions["feature_select"] == null || profile.repeatedActions["feature_select"] < 2)
    }

    @Test
    fun testRepeatedNavigationPaths() {
        extractor.update(makeEvent(screen = "home", previousScreen = "", action = "navigate"))
        extractor.update(makeEvent(screen = "settings", previousScreen = "home", action = "navigate"))
        extractor.update(makeEvent(screen = "home", previousScreen = "settings", action = "navigate"))

        val profile = extractor.getProfile()
        assertTrue(profile.repeatedNavigationPaths > 0)
    }
}