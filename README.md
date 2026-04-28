# SeedSight - Bedrock Edition

Minimap & Intelligence Addon for Minecraft Bedrock Edition using the Script API.

> **Looking for a specific Minecraft version?** This repo has branches for each supported version. Switch to the branch matching your version: `java-1.21.0`, `java-1.21.1`, `java-1.21.2`, `java-1.21.3`, `java-1.21.4`, `bedrock-1.21.80`, or `bedrock-1.21.132`. The `main` branch tracks the latest Java release (1.21.4). See [SETUP.md](SETUP.md) for detailed installation instructions.

## Features

- **HUD Overlay** — Coordinates, direction, time, day counter, entity counts via actionbar
- **Dimension-aware** — Shows [Nether] or [End] label in coordinates
- **Waypoint System** — Add, list, remove, find nearest waypoints with color coding
- **Death Markers** — Auto-tracked on player death with location display
- **Portal Calculator** — Auto-detects dimension: shows OW→Nether or Nether→OW
- **Per-Setting Toggles** — Toggle HUD, coords, direction, time, entities independently
- **Entity Detection** — Counts 25 hostile and 26 passive mob types within 128 blocks

## Commands

| Command | Description |
|---------|-------------|
| `!ss help` | Show all commands |
| `!ss toggle [setting]` | Toggle HUD/coords/time/entities/direction |
| `!ss wp add <name>` | Add waypoint at current position |
| `!ss wp list` | List all waypoints with distance |
| `!ss wp remove <name>` | Remove a waypoint |
| `!ss wp nearest` | Show nearest waypoint |
| `!ss portal` | Auto-convert coords (detects dimension) |
| `!ss calc` | Show calculator (Nether coords, XP, enchanting) |
| `!ss death` | Show last death location |
| `!ss settings` | Show current settings |

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

## License

MIT License - see [LICENSE](LICENSE)
