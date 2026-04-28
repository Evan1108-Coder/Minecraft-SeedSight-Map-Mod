# SeedSight - Bedrock Edition v2.0

Minimap & Intelligence Addon for Minecraft Bedrock Edition using the Script API.

> **Looking for a specific Minecraft version?** This repo has branches for each supported version. Switch to the branch matching your version: `java-1.21.0`, `java-1.21.1`, `java-1.21.2`, `java-1.21.3`, `java-1.21.4`, `bedrock-1.21.80`, or `bedrock-1.21.132`. The `main` branch tracks the latest Java release (1.21.4). See [SETUP.md](SETUP.md) for detailed installation instructions.

## Features

### HUD & Display
- **Action Bar HUD** — Coordinates, direction arrows (8-way compass), health with color coding, time, entity counts
- **Dimension-aware** — Shows [Nether] or [End] label, all features adapt per dimension
- **Configurable** — Toggle each HUD element independently, compact/full modes
- **Adjustable scan radius** — Set entity detection range from 16 to 256 blocks

### Navigation
- **Waypoint System** — Add, list, remove, find nearest with distance + compass direction
- **Home Waypoint** — Quick-save your base location
- **Death History** — Tracks last 5 deaths with dimension labels and directions
- **Distance Calculator** — Measure distance to any coordinates
- **Dimension-filtered waypoints** — Shows waypoints for your current dimension

### World Analysis (Seed-based)
- **Structure Finder** — Predicts 12 Overworld structures (Village, Temple, Mansion, Monument, Trial Chamber, etc.), Nether structures (Fortress, Bastion), and End Cities
- **Stronghold Locator** — Ring-based prediction for all 128 strongholds, shows nearest 5
- **Slime Chunk Finder** — Identifies slime chunks near you with block coordinates and directions

### Utilities
- **Portal Calculator** — Nether/Overworld coordinate conversion with auto-detection
- **XP Calculator** — Calculate total XP needed for any level
- **Circle Calculator** — Area, circumference, diameter for building
- **Ore Y-Level Reference** — Best mining heights for all ores (Diamond, Ancient Debris, Iron, etc.)
- **Enchanting Reference** — Bookshelf requirements, spawn chunk info

### Statistics
- **Session Stats** — Play time, distance walked, mobs killed, deaths, K/D ratio
- **Entity Scanner** — Detailed breakdown of all entities by type (30+ hostile, 30+ passive)
- **Movement Tracking** — Distance traveled in blocks and kilometers

## Commands

| Command | Description |
|---------|-------------|
| **HUD & Display** | |
| `!ss help` | Show commands (page 1) |
| `!ss help2` | Show commands (page 2) |
| `!ss toggle <opt>` | Toggle: hud/coords/time/entities/dir/health/weather |
| `!ss hud [compact\|full]` | Switch HUD display mode |
| `!ss radius <blocks>` | Set entity scan radius (16-256) |
| `!ss settings` | View all current settings |
| **Navigation** | |
| `!ss wp add <name>` | Save waypoint at current position |
| `!ss wp list` | List waypoints with distance + direction |
| `!ss wp remove <name>` | Remove waypoint (or nearest within 32m) |
| `!ss wp nearest` | Show nearest waypoint with direction |
| `!ss wp clear` | Remove all waypoints |
| `!ss home` | Save/update Home waypoint |
| `!ss death` | Show death history (last 5) |
| `!ss dist <x> <z>` | Distance to coordinates |
| **World Analysis** | |
| `!ss seed <number>` | Set world seed for predictions |
| `!ss structures` | Find nearby structures |
| `!ss stronghold` | Find nearest strongholds |
| `!ss slime` | Check slime chunks nearby |
| `!ss scan` | Detailed entity breakdown |
| **Utilities** | |
| `!ss portal` | Nether/Overworld coordinate conversion |
| `!ss calc` | Calculator + ore Y-level reference |
| `!ss calc circle <r>` | Circle area calculator |
| `!ss xp <level>` | XP needed for level |
| `!ss stats` | Session statistics |

## Installation

1. Download the `.mcpack` file for your Minecraft version from [Releases](https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod/releases)
2. Double-click to import into Minecraft Bedrock
3. Apply the behavior pack to your world
4. Enable **Beta APIs** in your world's Experiments settings

## Building from Source

```bash
npm install
npm run build
```

The compiled JavaScript will be output to `behavior_pack/scripts/main.js`.

## Supported Versions

| Branch | Minecraft | Script API |
|--------|-----------|------------|
| `bedrock-1.21.80` | 1.21.80 | @minecraft/server 1.17.0-beta |
| `bedrock-1.21.132` | 1.21.132 | @minecraft/server 1.18.0-beta |

## Bedrock vs Java

The Bedrock edition uses the Script API which has different capabilities than Java's Fabric mod:

| Feature | Java | Bedrock |
|---------|------|---------|
| Visual minimap | Yes (HUD overlay) | No (API limitation) |
| Action bar HUD | Yes | Yes |
| Waypoints | Yes | Yes |
| Structure finder | Yes | Yes (seed-based) |
| Slime chunks | Yes | Yes (seed-based) |
| Stronghold finder | Yes | Yes |
| Entity scanner | Yes | Yes |
| Portal calculator | Yes | Yes |
| Death markers | Yes | Yes (last 5) |
| Session stats | Yes | Yes |
| XP calculator | Yes | Yes |
| Ore Y-levels | Yes | Yes |
| Keybindings | Yes (11 keys) | No (chat commands) |
| Biome colors | Yes (50+ biomes) | No (API limitation) |
| Sound indicators | Yes | No (API limitation) |
| FPS/TPS display | Yes | No (API limitation) |

## License

MIT License - see [LICENSE](LICENSE)
