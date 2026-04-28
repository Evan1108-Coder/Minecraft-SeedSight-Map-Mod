# SeedSight Setup Guide

## Prerequisites

- **Minecraft Java Edition** 1.21.x
- **Java 21** (JDK) for building from source
- **Git** for cloning the repository

## Step 1: Install Fabric Loader

1. Go to [https://fabricmc.net/use/](https://fabricmc.net/use/)
2. Select your Minecraft version (1.21, 1.21.1, 1.21.2, 1.21.3, or 1.21.4)
3. Download and run the Fabric installer
4. Choose "Client" and click Install
5. A new "fabric-loader" profile will appear in the Minecraft Launcher

## Step 2: Install Fabric API

1. Download Fabric API from [Modrinth](https://modrinth.com/mod/fabric-api) for your Minecraft version
2. Place the Fabric API `.jar` file in your `.minecraft/mods/` folder
   - **Windows**: `%APPDATA%\.minecraft\mods\`
   - **macOS**: `~/Library/Application Support/minecraft/mods/`
   - **Linux**: `~/.minecraft/mods/`
3. Create the `mods` folder if it doesn't exist

## Step 3: Choose Your Branch

Each Minecraft version has its own branch. Clone the one matching your version:

| Your Minecraft Version | Branch to Use |
|----------------------|---------------|
| 1.21 (Tricky Trials) | `java-1.21.0` |
| 1.21.1 | `java-1.21.1` |
| 1.21.2 | `java-1.21.2` |
| 1.21.3 | `java-1.21.3` |
| 1.21.4 (Garden Awakens) | `java-1.21.4` or `main` |

## Step 4: Build from Source

```bash
# Clone the repository
git clone https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod.git
cd Minecraft-SeedSight-Map-Mod

# Switch to your version's branch (skip for 1.21.4, which is the default)
git checkout java-1.21.1   # replace with your version

# Build the mod
./gradlew build
```

The built JAR will be at `build/libs/seedsight-1.0.0.jar`.

## Step 5: Install the Mod

1. Copy `build/libs/seedsight-1.0.0.jar` into your `.minecraft/mods/` folder
2. Launch Minecraft using the Fabric profile
3. SeedSight will appear as a minimap HUD in the top-right corner

## Step 6: Configure (Optional)

- Press **H** to toggle the HUD on/off
- Press **N** to cycle through tabs (Minimap, Stats, Perf, Waypoints, Calculator, Settings)
- Press **M** to toggle expanded/compact view
- Press **B** to add a waypoint at your current position

All keybindings are configurable in Minecraft's Controls menu under the "SeedSight" category.

## Multiplayer Setup

On multiplayer servers, SeedSight cannot automatically read the world seed. To enable seed-based features:

1. If you have operator access, run `/seed` in-game to get the seed
2. Press **N** until you reach the Settings tab
3. Enter the seed in the seed override field

Without a seed, the minimap still works for real-time terrain scanning, entity tracking, and all non-prediction features.

## Bedrock Edition

Bedrock branches (`bedrock-1.21.80`, `bedrock-1.21.132`) use the Minecraft Script API and run as behavior pack addons. See the Bedrock branch README for specific instructions.
