package com.dynamic.dynamicbehavioradaptiveui.adaptation

import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorFeatureExtractor
import com.dynamic.dynamicbehavioradaptiveui.behavior.BehaviorStateEngine
import com.dynamic.dynamicbehavioradaptiveui.behavior.InteractionEvent
import com.dynamic.dynamicbehavioradaptiveui.models.*
import com.dynamic.dynamicbehavioradaptiveui.llm.AdaptationRecommendation
import com.dynamic.dynamicbehavioradaptiveui.llm.LLMResponse
import kotlin.random.Random

object DemoMode {

    sealed class DemoScenario {
        val name: String
        val description: String
        val eventCount: Int
        val targetProficiency: ProficiencyLevel
        val targetFriction: InteractionFriction
        val targetIntent: String
        val targetFamiliarity: WorkflowFamiliarity
    }

    object NewUser extends DemoScenario(
        name = "NEW USER",
        description = "Default interface and conservative behavior",
        eventCount = 8,
        targetProficiency = ProficiencyLevel.BEGINNER,
        targetFriction = InteractionFriction.LOW,
        targetIntent = "general_interaction",
        targetFamiliarity = WorkflowFamiliarity.NEW
    )

    object RepeatedUser extends DemoScenario(
        name = "REPEATED USER",
        description = "System learns pattern and surfaces contextual shortcut",
        eventCount = 25,
        targetProficiency = ProficiencyLevel.INTERMEDIATE,
        targetFriction = InteractionFriction.MEDIUM,
        targetIntent = "repeated_workflow",
        targetFamiliarity = WorkflowFamiliarity.FAMILIAR
    )

    object FrictionUser extends DemoScenario(
        name = "FRICTION DETECTION",
        description = "Contextual guidance simplifies workflow after errors/backtracking",
        eventCount = 35,
        targetProficiency = ProficiencyLevel.INTERMEDIATE,
        targetFriction = InteractionFriction.HIGH,
        targetIntent = "in_progress_workflow",
        targetFamiliarity = WorkflowFamiliarity.FAMILIAR
    )

    data class OverlayState(
        val eventsCollected: Int = 0,
        val proficiency: ProficiencyLevel = ProficiencyLevel.INTERMEDIATE,
        val intent: String = "",
        val friction: InteractionFriction = InteractionFriction.MEDIUM,
        val llmRecommendation: String? = null,
        val confidence: Double = 0.0,
        val adaptationApplied: String? = null,
        val measuredOutcome: String? = null,
        val adaptationPhase: AdaptationPhase.AdaptationPhase = AdaptationPhase.AdaptationPhase.PHASE_1_STABLE_DEFAULT,
        val confidenceSummary: String = ""
    )

    private var currentScenario: DemoScenario? = null
    private var overlayState = OverlayState()
    private var isDemoMode = false
    private var sessionId: String = "demo-session-001"
    private val eventsList = mutableListOf<InteractionEvent>()

    fun startDemo(scenario: DemoScenario) {
        isDemoMode = true
        currentScenario = scenario
        overlayState = OverlayState()
        eventsList.clear()

        when (scenario) {
            is NewUser -> startNewUserDemo()
            is RepeatedUser -> startRepeatedUserDemo()
            is FrictionUser -> startFrictionDemo()
        }
    }

    fun stopDemo() {
        isDemoMode = false
        currentScenario = null
        overlayState = OverlayState()
        eventsList.clear()
    }

    fun isActive(): Boolean = isDemoMode

    private fun startNewUserDemo() {
        // 8 events demonstrating new user behavior
        injectEvent(screen = "Home", action = "view_tasks", target = "tasks_list")
        injectEvent(screen = "Home", action = "view_calendar", target = "calendar_list")
        injectEvent(screen = "Tasks", action = "select_task", target = "task_1", duration = 2000L, success = true)
        injectEvent(screen = "Tasks", action = "complete_task", target = "task_1", duration = 3000L, success = false)
        injectEvent(screen = "Home", action = "view_notes", target = "notes_list")
        injectEvent(screen = "Home", action = "view_files", target = "files_list")
        injectEvent(screen = "Settings", action = "toggle_dark_mode", target = "theme_toggle")
        injectEvent(screen = "Home", action = "search", target = "search_bar")
    }

    private fun startRepeatedUserDemo() {
        // 25 events demonstrating repeated workflow pattern
        repeat(5) {
            injectEvent(screen = "Home", action = "navigate_to_tasks", target = "tasks_list")
            injectEvent(screen = "Tasks", action = "select_task", target = "task_1", duration = 1500L, success = true)
            injectEvent(screen = "Tasks", action = "complete_task", target = "task_1", duration = 2000L, success = true)
            injectEvent(screen = "Tasks", action = "navigate_back", target = "home", duration = 500L)
            injectEvent(screen = "Home", action = "navigate_to_calendar", target = "calendar_list")
            injectEvent(screen = "Calendar", action = "view_event", target = "event_1", duration = 1800L, success = true)
            injectEvent(screen = "Calendar", action = "create_event", target = "new_event", duration = 3000L, success = true)
        }
        injectEvent(screen = "Home", action = "view_notes", target = "notes_list")
        injectEvent(screen = "Home", action = "view_files", target = "files_list")
    }

    private fun startFrictionDemo() {
        // 35 events demonstrating high friction pattern
        repeat(10) {
            injectEvent(screen = "Tasks", action = "select_task", target = "task_1", duration = 5000L, success = false)
            injectEvent(screen = "Tasks", action = "go_back", target = "home", duration = 800L)
            injectEvent(screen = "Tasks", action = "retry_task", target = "task_1", duration = 6000L, success = false)
        }
        repeat(5) {
            injectEvent(screen = "Calendar", action = "view_event", target = "event_1", duration = 4000L, success = false)
            injectEvent(screen = "Calendar", action = "navigate_back", target = "home", duration = 600L)
        }
        repeat(8) {
            injectEvent(screen = "Home", action = "navigate_to_tasks", target = "tasks_list")
            injectEvent(screen = "Tasks", action = "select_task", target = "task_2", duration = 3000L, success = true)
        }
        injectEvent(screen = "Home", action = "view_notes", target = "notes_list")
        injectEvent(screen = "Home", action = "view_files", target = "files_list")
    }

    fun injectEvent(
        screen: String,
        action: String,
        target: String,
        duration: Long = 0L,
        success: Boolean = true,
        metadata: String = ""
    ) {
        if (!isDemoMode) return

        val event = InteractionEvent(
            eventId = "demo-event-${eventsList.size}",
            timestamp = System.currentTimeMillis(),
            sessionId = sessionId,
            screen = screen,
            action = action,
            target = target,
            previousScreen = "",
            duration = duration,
            workflowId = UUID.randomUUID().toString(),
            success = success,
            metadata = metadata
        )

        eventsList.add(event)

        updateOverlayState()
    }

    private fun updateOverlayState() {
        val state = when (currentScenario) {
            is DemoMode.NewUser -> BehaviorState(
                proficiencyLevel = ProficiencyLevel.BEGINNER,
                interactionFriction = InteractionFriction.LOW,
                currentIntent = "general_interaction",
                workflowFamiliarity = WorkflowFamiliarity.NEW
            )
            is DemoMode.RepeatedUser -> BehaviorState(
                proficiencyLevel = ProficiencyLevel.INTERMEDIATE,
                interactionFriction = InteractionFriction.MEDIUM,
                currentIntent = "repeated_workflow",
                workflowFamiliarity = WorkflowFamiliarity.FAMILIAR
            )
            is DemoMode.FrictionUser -> BehaviorState(
                proficiencyLevel = ProficiencyLevel.INTERMEDIATE,
                interactionFriction = InteractionFriction.HIGH,
                currentIntent = "in_progress_workflow",
                workflowFamiliarity = WorkflowFamiliarity.FAMILIAR
            )
            else -> BehaviorState()
        }

        val phase = when (currentScenario) {
            is DemoMode.NewUser -> AdaptationPhase(
                phase = ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS,
                confidence = 0.6,
                accumulatedInteractions = eventsList.size
            )
            is DemoMode.RepeatedUser -> AdaptationPhase(
                phase = ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE,
                confidence = 0.85,
                accumulatedInteractions = eventsList.size
            )
            is DemoMode.FrictionUser -> AdaptationPhase(
                phase = ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE,
                confidence = 0.85,
                accumulatedInteractions = eventsList.size
            )
            else -> AdaptationPhase(
                phase = ColdStartPhase.PHASE_1_STABLE_DEFAULT,
                confidence = 0.0,
                accumulatedInteractions = 0
            )
        }

        val lastEvent = eventsList.lastOrNull()

        overlayState = OverlayState(
            eventsCollected = eventsList.size,
            proficiency = state.proficiencyLevel,
            intent = state.currentIntent,
            friction = state.interactionFriction,
            adaptationPhase = phase,
            confidenceSummary = generateReasoningSummary(state, phase)
        )

        // Simulate LLM recommendation based on state
        llmRecommendation = when (state.currentIntent) {
            "repeated_workflow" -> "Based on repeated workflow detected, recommending navigation enhancement with quick-access shortcuts"
            "in_progress_workflow" -> "Based on in-progress workflow with high friction, recommending widget reordering and guidance card"
            "exploring_features" -> "Based on feature exploration, recommending feature visibility highlights"
            else -> "Based on general interaction, applying conservative shortcuts for beginner proficiency"
        }

        confidence = when (state.interactionFriction) {
            InteractionFriction.HIGH -> 0.85
            InteractionFriction.MEDIUM -> 0.65
            InteractionFriction.LOW -> 0.45
        }.coerceIn(0.0, 1.0)

        adaptationApplied = when (state.proficiencyLevel) {
            ProficiencyLevel.BEGINNER -> "Showing contextual tooltips and simplified layout"
            ProficiencyLevel.INTERMEDIATE -> "Showing feature recommendations and navigation enhancements"
            ProficiencyLevel.ADVANCED -> "Showing widget reordering and personalized shortcuts"
        }

        measuredOutcome = when (state.interactionFriction) {
            InteractionFriction.HIGH -> "Reduced backtracking by 40% with contextual guidance"
            InteractionFriction.MEDIUM -> "Improved task completion time by 25%"
            InteractionFriction.LOW -> "Maintaining efficient workflow with minimal interventions"
        }
    }

    private fun generateReasoningSummary(state: BehaviorState, phase: AdaptationPhase.AdaptationPhase): String {
        return when {
            phase.phase == AdaptationPhase.ColdStartPhase.PHASE_1_STABLE_DEFAULT -> {
                "Default interface active. No behavioral patterns detected yet."
            }
            state.proficiencyLevel == ProficiencyLevel.BEGINNER && phase.phase >= AdaptationPhase.ColdStartPhase.PHASE_2_CONSERVATIVE_SHORTCUTS -> {
                "Conservative shortcuts mode. User identified as beginner with low friction."
            }
            state.currentIntent == "repeated_workflow" && phase.phase >= AdaptationPhase.ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE -> {
                "Repeated workflow detected. Preparing navigation enhancement with contextual shortcuts."
            }
            state.interactionFriction == InteractionFriction.HIGH && phase.phase >= AdaptationPhase.ColdStartPhase.PHASE_3_PERSONALIZED_ADAPTIVE -> {
                "High friction detected. Contextual guidance will simplify workflow."
            }
            else -> "Adapting UI based on accumulated behavior patterns."
        }
    }

    // Getters for overlay display
    fun getOverlayState(): OverlayState = overlayState
    fun getCurrentScenario(): DemoScenario? = currentScenario
    fun getEventsList(): List<InteractionEvent> = eventsList
    fun isDemoMode(): Boolean = isDemoMode

    // Mock LLM recommendation for demo
    fun getMockLLMRecommendation(state: BehaviorState): (String, Double) {
        return when {
            state.proficiencyLevel == ProficiencyLevel.BEGINNER && state.interactionFriction == InteractionFriction.LOW -> {
                "Show contextual tooltips for beginner users", 0.4
            }
            state.currentIntent == "repeated_workflow" -> {
                "Navigation enhancement with quick-access shortcuts", 0.75
            }
            state.interactionFriction == InteractionFriction.HIGH -> {
                "Contextual guidance card and widget reordering", 0.85
            }
            state.proficiencyLevel == ProficiencyLevel.ADVANCED -> {
                "Widget reordering for advanced user", 0.7
            }
            else -> "Conservative shortcuts applied", 0.5
        }
    }
}