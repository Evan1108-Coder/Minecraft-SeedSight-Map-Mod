# Contributing to WorldWhisperer

Thank you for your interest in contributing to WorldWhisperer!

## Getting Started

### Prerequisites
- Java 21 (JDK)
- Git
- An IDE with Java support (IntelliJ IDEA recommended)

### Setup
1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR-USERNAME/Minecraft-WorldWhisperer-Map-Mod.git`
3. Open the project in your IDE (Gradle import)
4. Run `./gradlew build` to verify the setup

### Branch Structure
- `main` - Primary development branch (targets MC 1.21.4)
- `java-1.21.0` through `java-1.21.4` - Version-specific branches
- `bedrock-1.21.80`, `bedrock-1.21.132` - Bedrock Script API branches

## Development Guidelines

### Code Style
- Use Java 21 features where appropriate (records, switch expressions, pattern matching)
- Keep methods focused and under 50 lines when possible
- Use `MathHelper.floor()` for coordinate conversions
- Avoid `Math.pow(x, 2)` — use `x * x` instead

### Architecture
- **HUD Rendering**: `MinimapRenderer`, `PanelRenderer`, `HudRenderer`
- **Game State**: `GameStats`, `PerfStats`, `SoundIndicator`
- **World Generation**: `StructureFinder`, `SeedPredictor`, `SlimeChunkFinder`
- **Data**: `WaypointManager`, `WorldWhispererConfig`
- **Utilities**: `ColorUtil`, `RenderUtil`, `McCalculator`

### Thread Safety
- Sound events come from the sound system thread — use `synchronized` or immutable snapshots
- Mark shared mutable state as `volatile` when accessed from render thread
- Use `List.copyOf()` for lists shared between tick and render threads

### Adding Structure Predictions
Structures using the spacing/separation/salt algorithm can be added to `StructureFinder.STRUCTURES`:
```java
new StructureType("Name", "LBL", spacing, separation, salt, color, minMinor)
```

### Adding Sound Indicators
Add entries to `SoundIndicator.formatSoundName()`:
```java
if (soundId.contains("entity.mob_name")) return "Display Name";
```

### Multi-Version Support
- Use `ModVersion.MC_MINOR` for version-gated features
- Cherry-pick commits from `main` to version branches
- Test on at least 1.21.0 and 1.21.4

## Pull Request Process
1. Create a feature branch from `main`
2. Make your changes with descriptive commits
3. Ensure `./gradlew build` passes
4. Submit a PR to `main`
5. Maintainers will cherry-pick to version branches after merge

## License
By contributing, you agree that your contributions will be licensed under the MIT License.
