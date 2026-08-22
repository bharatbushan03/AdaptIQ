# BehaviorStateEngine

Deterministic behavior state inference engine for Android applications.

## Overview

The `BehaviorStateEngine` infers the user's current interaction state from behavioral features:
- Proficiency level (BEGINNER/INTERMEDIATE/ADVANCED)
- Interaction friction (LOW/MEDIUM/HIGH)
- Current intent (based on recent interaction sequence)
- Workflow familiarity (NEW/FAMILIAR/FREQUENT)

## Features

- Deterministic rule-based inference (no LLM required)
- Configurable thresholds for all inference rules
- Session-based state tracking
- Integrates with existing `BehaviorTracker` and `BehaviorFeatureExtractor`

## Installation

```kotlin
// Add the engine to your Application or DI container
val engine = BehaviorStateEngine()
engine.updateState(sessionId, metrics)
val state = engine.getCurrentState(sessionId)
```

## Configuration

All thresholds are configurable via nested data classes:

```kotlin
engine = BehaviorStateEngine(
    proficiencyThresholds = ProficiencyThresholds(
        errorRateMaxForBeginner = 0.25,
        minInteractionsForProficiency = 15
    ),
    frictionThresholds = FrictionThresholds(
        backtrackingMinForHigh = 8,
        dwellTimeMinForHighMs = 5000L
    ),
    familiarityThresholds = FamiliarityThresholds(
        minFeatureRepeatsForFamiliar = 5
    )
)
```

## Example Usage

```kotlin
// Update with behavioral metrics
engine.updateState(
    "session_123",
    BehaviorStateEngine.Metrics(
        backtrackingFrequency = 5,
        averageDwellTime = 5000L,
        interactionErrorRate = 0.3,
        workflowCompletionRate = 0.8,
        workflowAbandonmentRate = 0.1,
        mostFrequentlyUsedFeatures = ["save", "export"],
        repeatedActions = 8,
        frequentlyVisitedScreenSequences = 4
    )
)

// Get current inferred state
val state = engine.getCurrentState("session_123")
// BehaviorState(proficiencyLevel=INTERMEDIATE, interactionFriction=HIGH, currentIntent="in_progress_workflow", workflowFamiliarity=FAMILIAR)
```

## Project Structure

```
app/src/main/java/com/dynamic/dynamicbehavioradaptiveui/
├── models/           # Data classes (BehaviorState, UserBehaviorProfile, etc.)
├── behavior/         # BehaviorStateEngine, BehaviorFeatureExtractor
├── adaptation/      # AdaptationEngine
├── analytics/       # InteractionLogger, AnalyticsTracker
├── llm/             # LocalLLM
└── ui/              # Compose UI screens
```

## Testing

Run existing tests:

```bash
./gradlew test
```