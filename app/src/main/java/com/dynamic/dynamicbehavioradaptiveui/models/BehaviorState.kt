package com.dynamic.dynamicbehavioradaptiveui.models

data class BehaviorState(
    val proficiencyLevel: ProficiencyLevel = ProficiencyLevel.INTERMEDIATE,
    val interactionFriction: InteractionFriction = InteractionFriction.MEDIUM,
    val currentIntent: String = "",
    val workflowFamiliarity: WorkflowFamiliarity = WorkflowFamiliarity.NEW
)

enum class ProficiencyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class InteractionFriction {
    LOW,
    MEDIUM,
    HIGH
}

enum class WorkflowFamiliarity {
    NEW,
    FAMILIAR,
    FREQUENT
}