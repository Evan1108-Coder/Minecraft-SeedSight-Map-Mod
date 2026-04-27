# WorldWhisperer - Bedrock Edition

Minimap & Intelligence Addon for Minecraft Bedrock 1.21.80+

## Status: Early Development

The Bedrock edition uses the Script API (@minecraft/server) for game interaction.
Due to Bedrock's limited HUD rendering capabilities, this version uses:

- **Actionbar text** for stats display (coordinates, time, entity counts)
- **Chat commands** (!ww) for waypoints, calculator, and settings
- **Forms UI** for interactive settings (planned)

A full graphical minimap will require a resource pack with the JSON UI system.

## Commands

| Command | Description |
|---------|-------------|
| `!ww help` | Show all commands |
| `!ww toggle` | Toggle HUD on/off |
| `!ww wp add <name>` | Add waypoint at current position |
| `!ww wp list` | List all waypoints |
| `!ww wp remove <name>` | Remove a waypoint |
| `!ww calc` | Open calculator |
| `!ww nether` | Convert coords to Nether |
| `!ww overworld` | Convert coords to Overworld |
| `!ww settings` | Show settings |

## Installation

1. Download the `.mcaddon` file from Releases
2. Double-click to import into Minecraft Bedrock
3. Apply the behavior pack to your world

## Building

```bash
npm install
npm run build
```

## License

MIT License
