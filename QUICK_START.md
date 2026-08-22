# Quick Start

## Getting the BehaviorStateEngine Up and Running

### 1. Add the Engine

```kotlin
// In your Application class or DI container
val behaviorStateEngine = BehaviorStateEngine()
```

### 2. Update State with Behavioral Metrics

```kotlin
// Collect metrics from your BehaviorFeatureExtractor or tracker
val metrics = BehaviorStateEngine.Metrics(
    backtrackingFrequency = extractor.backtrackFrequency,
    averageDwellTime = extractor.averageDwellTime.toLong(),
    interactionErrorRate = extractor.interactionErrorRate,
    workflowCompletionRate = extractor.workflowCompletionRate,
    workflowAbandonmentRate = extractor.workflowAbandonmentRate,
    mostFrequentlyUsedFeatures = extractor.mostFrequentlyUsedFeatures,
    repeatedActions = extractor.repeatedActions.size,
    frequentlyVisitedScreenSequences = extractor.frequentlyVisitedScreenSequences.size
)

// Update engine per session
behaviorStateEngine.updateState("user_session_123", metrics)
```

### 3. Get Current Inferred State

```kotlin
val state = behaviorStateEngine.getCurrentState("user_session_123")

// Example usage in UI/ViewModel
@Composable
fun AdaptiveScreen() {
    val state by remember { derivedStateOf {
        behaviorStateEngine.getCurrentState(currentSessionId)
    } }

    when {
        state.proficiencyLevel == ProficiencyLevel.BEGINNER -> {
            // Show beginner help
        }
        state.interactionFriction == InteractionFriction.HIGH -> {
            // Show reduced complexity
        }
        state.workflowFamiliarity == WorkflowFamiliarity.NEW -> {
            // Show onboarding prompt
        }
    }
}
```

### 4. Configure Thresholds (Optional)

```kotlin
val engine = BehaviorStateEngine(
    proficiencyThresholds = BehaviorStateEngine.ProficiencyThresholds(
        errorRateMaxForBeginner = 0.2,
        minInteractionsForProficiency = 20
    ),
    frictionThresholds = BehaviorStateEngine.FrictionThresholds(
        backtrackingMinForHigh = 5,
        dwellTimeMinForHighMs = 10000L
    ),
    familiarityThresholds = BehaviorStateEngine.FamiliarityThresholds(
        minFeatureRepeatsForFamiliar = 5
    )
)
```

### 5. Run Tests

```bash
./gradlew test
```

### 6. Integrate with AdaptationEngine

Wire the `BehaviorStateEngine` into your `AdaptationEngine` to drive adaptive UI changes based on the inferred state.