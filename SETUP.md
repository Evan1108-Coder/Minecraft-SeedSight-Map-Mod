# SeedSight Bedrock Edition - Setup Guide

## Prerequisites

- **Minecraft Bedrock Edition** 1.21.80+ (Windows 10/11, Xbox, Mobile, or PlayStation)
- **Node.js 18+** and **npm** (only if building from source)

## Method 1: Install from Release

1. Go to [Releases](https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod/releases)
2. Download the `.mcpack` file matching your Minecraft version:
   - `seedsight-bedrock-1.21.80.mcpack` for Minecraft 1.21.80
   - `seedsight-bedrock-1.21.132.mcpack` for Minecraft 1.21.132
3. Double-click the `.mcpack` file — Minecraft will open and import it automatically
4. Go to **Settings > Storage** to verify the pack appears under "Behavior Packs"

## Method 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod.git
cd Minecraft-SeedSight-Map-Mod

# Switch to your Bedrock version branch
git checkout bedrock-1.21.80   # or bedrock-1.21.132

# Install dependencies and build
npm install
npm run build
```

Then copy the `behavior_pack` folder into your world's behavior packs directory:
- **Windows**: `%LOCALAPPDATA%\Packages\Microsoft.MinecraftUWP_8wekyb3d8bbwe\LocalState\games\com.mojang\development_behavior_packs\`
- **Android**: `/storage/emulated/0/games/com.mojang/development_behavior_packs/`

## Applying the Behavior Pack to a World

1. Open Minecraft Bedrock Edition
2. Edit the world you want to use SeedSight with
3. Go to **Behavior Packs** in the world settings
4. Find "SeedSight" and tap **Activate**
5. **Important**: Go to **Experiments** and enable **Beta APIs**
6. Save and enter the world

## Verifying It Works

When you join the world, you should see:
- A green message: `[SeedSight] Bedrock Edition loaded! Use !ss for commands.`
- An actionbar HUD showing coordinates, direction, time, and entity counts

Type `!ss help` in chat to see all available commands.

## Branch Selection

| Your Minecraft Version | Branch |
|----------------------|--------|
| 1.21.80 | `bedrock-1.21.80` |
| 1.21.132 | `bedrock-1.21.132` |
| Java Edition 1.21.x | See `java-1.21.x` branches or `main` |

## Notes

- Beta APIs must be enabled for the Script API to work
- The addon uses the actionbar for HUD display (coordinates, direction, time, entities)
- Commands use the `!ss` prefix in chat (e.g., `!ss help`, `!ss wp add Home`)
- Waypoints and death markers reset when the world is closed (session-based storage)
