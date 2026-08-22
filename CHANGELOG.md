# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-08-22

### Added
- `BehaviorState` model with four inferred states: proficiency, friction, intent, familiarity
- `BehaviorStateEngine` with deterministic rule-based inference
- Configurable thresholds via `ProficiencyThresholds`, `FrictionThresholds`, `FamiliarityThresholds`
- `Metrics` data class for behavioral inputs
- `ProficiencyLevel`, `InteractionFriction`, `WorkflowFamiliarity` enums

### Changed
- Reorganized `models/` package to include new state model
- Updated `BehaviorStateEngine.kt` structure to follow existing code conventions

### Fixed
- None