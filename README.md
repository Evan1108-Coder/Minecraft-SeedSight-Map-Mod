# WorldWhisperer - Minecraft Minimap & Intelligence Mod

A client-side Fabric mod for Minecraft Java Edition 1.21.x that provides a feature-rich minimap HUD with seed-based world generation prediction, game statistics, performance monitoring, waypoints, and more.

## Features

### Minimap
- Real-time minimap HUD in the top-right corner of your screen
- Chunk-based terrain scanning with accurate block colors
- Seed-based biome and structure prediction beyond render distance
- Zoom in/out with configurable levels
- North-locked or rotation-following modes
- Grid overlay and coordinate display
- Entity markers (players, hostile mobs, passive mobs)
- Structure markers with labels (Villages, Temples, Monuments, etc.)
- Slime chunk overlay

### Split-Panel UI
- Minecraft-native themed interface (bitmap font, stone buttons, MC borders)
- Left tab bar with 6 tabs + right content area
- Smart tab switching:
  - **Partial tabs** (MiniMap, Stats, Perf Stats) replace only one section
  - **Full tabs** (Waypoints, Calculator, Settings) replace the entire content area
  - Remembers your split layout when switching between partial and full views

### Game Statistics
- Player coordinates (X, Y, Z)
- Current biome name
- In-game time (12-hour format) and day count
- Light level, facing direction
- Hostile/passive entity counts within 128 blocks
- Slime chunk indicator
- Sound indicators (nearby mob sounds, footsteps, block breaking, ambient)

### Performance Stats
- FPS and TPS monitoring
- Memory usage (used/max)
- Loaded chunk count
- Render distance
- Multiplayer ping
- Entity count

### Waypoint System
- Quick-add waypoints at current position (B key)
- Death markers (auto-created on death)
- Color-coded waypoints with distance display
- JSON-based persistence
- Visible on minimap with labels

### Calculator
- Nether portal coordinate converter (Overworld <-> Nether)
- Spawn chunk radius calculator
- Enchanting power calculator (bookshelves needed)

### Settings
- Toggle all features on/off
- Configurable HUD position, scale, opacity
- Map zoom range
- North lock toggle
- Sound indicator toggle
- Seed override for multiplayer servers

### Seed-Based Prediction
- Predicts biome types for any coordinate using the world seed
- Predicts structure locations using Minecraft's spacing/separation/salt algorithm
- Supports 18 structure types: Village, Desert Temple, Jungle Temple, Swamp Hut, Igloo, Pillager Outpost, Ocean Monument, Woodland Mansion, Stronghold, Ocean Ruin, Shipwreck, Buried Treasure, Mineshaft, Ruined Portal, Ancient City, Trail Ruin, Trial Chamber, Nether Fortress
- Slime chunk calculation
- Version-aware: structures only predicted for versions where they exist (e.g., Trial Chambers only on 1.21.2+)

## Keybindings

| Key | Action |
|-----|--------|
| `H` | Toggle HUD visibility |
| `M` | Toggle expanded/compact view |
| `=` | Zoom in |
| `-` | Zoom out |
| `B` | Add waypoint at current position |
| `N` | Cycle through tabs |

All keybindings are configurable in Minecraft's Controls settings under the "WorldWhisperer" category.

## Supported Versions

### Java Edition (Fabric)

| Branch | Minecraft | Fabric API | Yarn Mappings |
|--------|-----------|------------|---------------|
| `java-1.21.0` | 1.21 (Tricky Trials) | 0.100.3+1.21 | 1.21+build.2 |
| `java-1.21.1` | 1.21.1 | 0.102.1+1.21.1 | 1.21.1+build.3 |
| `java-1.21.2` | 1.21.2 | 0.105.0+1.21.2 | 1.21.2+build.1 |
| `java-1.21.3` | 1.21.3 | 0.114.1+1.21.3 | 1.21.3+build.2 |
| `java-1.21.4` | 1.21.4 (Garden Awakens) | 0.118.0+1.21.4 | 1.21.4+build.8 |

### Bedrock Edition

| Branch | Minecraft | Status |
|--------|-----------|--------|
| `bedrock-1.21.80` | 1.21.80 | Planned |

## Installation

### Requirements
- Minecraft Java Edition 1.21.x
- [Fabric Loader](https://fabricmc.net/use/) 0.16.0 or later
- [Fabric API](https://modrinth.com/mod/fabric-api)

### Steps
1. Install Fabric Loader for your Minecraft version
2. Download the Fabric API mod and place it in your `mods` folder
3. Download the WorldWhisperer JAR for your Minecraft version from [Releases](https://github.com/Evan1108-Coder/Minecraft-WorldWhisperer-Map-Mod/releases)
4. Place the WorldWhisperer JAR in your `mods` folder
5. Launch Minecraft with the Fabric profile

### Multiplayer Note
On multiplayer servers, the world seed is not sent to clients. To use seed-based prediction features:
- Use the `/seed` command if you have operator permissions
- Or manually enter the seed in WorldWhisperer Settings (press `N` to cycle to Settings tab)

## Building from Source

### Requirements
- Java 21 (JDK)
- Git

### Build
```bash
git clone https://github.com/Evan1108-Coder/Minecraft-WorldWhisperer-Map-Mod.git
cd Minecraft-WorldWhisperer-Map-Mod
./gradlew build
```

The built mod JAR will be in `build/libs/worldwhisperer-1.0.0.jar`.

## Configuration

Config file is stored at `.minecraft/config/worldwhisperer.json`. All settings can be changed in-game via the Settings tab.

## License

MIT License - see [LICENSE](LICENSE) for details.

## Credits

Created by [Evan1108-Coder](https://github.com/Evan1108-Coder)
