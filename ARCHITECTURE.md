# Architecture

## High-Level Flow

```
UI / Repository ──► BehaviorTracker (suspend functions) ──► BehaviorFeatureExtractor
        │                                                   │
        │                                                   ▼
        └───────────────────────► UserBehaviorProfile ◄───── AdaptationEngine
                                                   │
                                                   ▼
                                             BehaviorStateEngine (new)
                                                   │
                                                   ▼
                                           Adaptive UI Actions
```

## Component Responsibilities

### BehaviorTracker (Interface)
- Defines suspend functions for tracking interaction events
- Standardized event types: screen opened/closed, button clicks, navigation, dwell time, workflow completion/abandonment, errors, task completion times, feature usage

### BehaviorFeatureExtractor
- Accumulates statistics from `InteractionEvent`s
- Builds `UserBehaviorProfile` with aggregated metrics
- Updated incrementally via `update(event)`

### BehaviorStateEngine **(NEW)**
- Receives behavioral metrics (can share tracker contract)
- Maintains internal state (current screen, active workflow, recent action frequencies, workflow attempts/success)
- Infers four state dimensions deterministically:
  - Proficiency level
  - Interaction friction
  - Current intent
  - Workflow familiarity
- Configurable thresholds
- Produces `BehaviorState` summary

### UserBehaviorProfile
- Central model of collected behavior metrics
- Feature usage frequency, navigation depth, error rates, workflow rates, screen sequences, etc.
- Built by `BehaviorFeatureExtractor.getProfile()`

### AdaptationEngine
- Receives `BehaviorTracker` and `BehaviorStateEngine` (or `UserBehaviorProfile`)
- Drives adaptive UI changes based on inferred state
- Currently a mock; plug in real adaptation logic

### InteractionEvent
- Individual event data class with: id, timestamp, sessionId, screen, action, target, previousScreen, duration, workflowId, success, metadata

## Data Flow

1. UI calls `BehaviorTracker` suspend functions (e.g., `trackButtonClicked`, `trackWorkflowCompletion`)
2. Implementation forwards events to `BehaviorFeatureExtractor.update(event)`
3. `BehaviorFeatureExtractor` maintains internal counters/maps
4. When `getProfile()` is called, builds `UserBehaviorProfile`
5. **New**: `BehaviorStateEngine` can subscribe to events or receive periodic metric snapshots
6. `BehaviorStateEngine.getCurrentState()` infers adaptive state
7. `AdaptationEngine` uses `BehaviorState` (or `UserBehaviorProfile`) to decide UI adjustments

## Extending

- Add new inference rules in `BehaviorStateEngine` private functions
- Configure thresholds via the threshold data classes
- Wire engine into `AdaptationEngine` for adaptive UI
- Expose state via `ViewModel` for UI consumption