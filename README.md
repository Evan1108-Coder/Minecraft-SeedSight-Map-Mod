# WorldWhisperer - Bedrock Edition

Minimap & Intelligence Addon for Minecraft Bedrock Edition using the Script API.

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
| `!ww help` | Show all commands |
| `!ww toggle [setting]` | Toggle HUD/coords/time/entities/direction |
| `!ww wp add <name>` | Add waypoint at current position |
| `!ww wp list` | List all waypoints with distance |
| `!ww wp remove <name>` | Remove a waypoint |
| `!ww wp nearest` | Show nearest waypoint |
| `!ww portal` | Auto-convert coords (detects dimension) |
| `!ww calc` | Show calculator (Nether coords, XP, enchanting) |
| `!ww death` | Show last death location |
| `!ww settings` | Show current settings |

## Installation

1. Download the `.mcaddon` file from Releases
2. Double-click to import into Minecraft Bedrock
3. Apply the behavior pack to your world

## Building

```bash
npm install
npm run build
```

## Supported Versions

| Branch | Minecraft | Script API |
|--------|-----------|------------|
| `bedrock-1.21.80` | 1.21.80 | @minecraft/server 1.17.0-beta |
| `bedrock-1.21.132` | 1.21.132 | @minecraft/server 1.18.0-beta |

## License

MIT License
