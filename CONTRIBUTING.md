# Contributing

## Development Setup

1. Ensure Java 17+ and Android SDK are installed
2. Open project in Android Studio
3. Sync Gradle files
4. Run `./gradlew test` to verify existing tests pass

## Code Conventions

- **Kotlin naming**: PascalCase for classes/interfaces, camelCase for properties, `data class` for DTOs
- **Package structure**: Follow existing domain-based organization
- **Null safety**: Use `?.` and `!!` appropriately; prefer safe calls
- **Extension functions**: Place in dedicated files, not as top-level in packages
- **Comments**: Minimal; code should be self-documenting where possible

## Adding New Features

1. Identify the relevant package/domain
2. Follow existing patterns (see `BehaviorFeatureExtractor`, `UserBehaviorProfile`)
3. Update `build.gradle.kts` if new dependencies are needed
4. Run tests to verify integration

## Testing

- Place tests next to the classes they test (e.g., `BehaviorStateEngineTest.kt`)
- Use `kotlinc` test framework conventions
- Test both normal and edge cases for inference rules
- Mock dependencies where appropriate

## Pull Request Process

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add some amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Questions?

Open an issue or discuss in the team channel.