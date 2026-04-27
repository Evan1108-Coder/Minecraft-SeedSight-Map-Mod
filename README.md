# SeedSight - Minecraft Minimap & Intelligence Mod

A client-side Fabric mod for Minecraft Java Edition 1.21.x that provides a feature-rich minimap HUD with seed-based world generation prediction, game statistics, performance monitoring, waypoints, and more.

## Features

### Minimap
- Real-time minimap HUD in the top-right corner of your screen
- Chunk-based terrain scanning with accurate block colors
- Seed-based biome and structure prediction beyond render distance
- Zoom in/out with configurable levels
- North-locked or rotation-following modes
- **Rotation-aware overlays**: entities, waypoints, and structures rotate with the minimap in rotation-following mode
- Rotating compass labels (N/S/E/W track actual world directions in rotation mode)
- Grid overlay and coordinate display (X / Y / Z)
- Entity markers with type-specific colors and sizes:
  - **Players**: Aqua dots (3px) with name labels
  - **Ender Dragon / Wither**: Large purple dots (4px)
  - **Warden**: Dark aqua dots (3px)
  - **Hostile mobs**: Red dots (2px)
  - **Villagers**: Gold dots (2px)
  - **Passive mobs**: Green dots (2px)
- Structure markers with labels (Villages, Temples, Monuments, etc.)
- Nearest structure indicator with distance + direction arrow at minimap edge
- **Structure proximity alert**: "Near: Village" popup when within 100 blocks of a predicted structure
- Hostile mob count badge (yellow/red warning)
- Biome change notification (3-second popup on minimap)
- Expandable map mode (M key: 128px ↔ 256px, panel also scales 1.5x)
- Zoom level indicator with **cave mode label** (shows "CAVE" when underground)
- **Underground cave mode**: auto-detects when player is underground and shows cave terrain instead of surface
- **Nether terrain rendering**: scans downward from player Y to show actual Nether floor, not bedrock ceiling
- **Edge waypoint indicators**: off-screen waypoints show colored markers at minimap edges
- **Player breadcrumb trail**: last 20 positions shown as fading green dots (configurable on/off)
- **Danger-pulse border**: minimap border pulses red when 5+ hostile mobs nearby
- **Render distance boundary**: faint circle showing the edge of loaded chunks
- **World spawn marker**: white cross at coordinates 0,0 (Overworld only)
- **Visual scale bar**: 32-block reference ruler at top-left of minimap
- **Improved player arrow**: directional arrow with wings and tail, rotates with facing
- **Dropped items**: shown as white dots (1px) on minimap
- **Mode change notifications**: centered popup when toggling circular/north-lock modes
- **Expanded mode biome name**: coordinate bar shows biome in expanded view
- **Night vision boost**: minimap brightens 30% when player has Night Vision effect
- **Water depth shading**: deeper water renders progressively darker blue (up to 10 blocks)
- **Lava glow**: lava surface blocks render with brighter orange on minimap
- Smooth gradient-based height shading for terrain elevation
- Slime chunk overlay (Overworld, north-locked mode)
- Map cache auto-clears on dimension change and surface/underground transitions

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
- In-game time (12-hour format), day count, and weather (Clear/Rain/Thunder)
- **Player vitals**: HP with absorption hearts (color-coded), food level with saturation, armor value, XP level, movement speed (m/s), fire/freezing status warnings
- **Target block display**: shows name of block you're looking at
- **Session tracker**: time played and distance walked this session
- **Nearest structure bearing**: cardinal direction + distance to closest predicted structure
- **Air/oxygen meter**: appears when underwater with color-coded threshold
- **Chunk coordinates** in stats panel
- Light level with **block/sky breakdown** (color-coded: red=spawn danger, yellow=dim, green=safe), facing direction
- **Movement mode display**: Sprint (yellow), Swim (cyan), Elytra (purple), Riding (shows mount name)
- Dimension indicator (shows when in Nether or End)
- **Cross-dimension coordinates**: always shows corresponding Nether/Overworld coordinates
- **Distance to Home**: shows cardinal bearing + distance to your bed/home waypoint
- **Death location distance**: shows distance to your last death marker
- **Elytra flight indicator**: speed label changes to "Elytra" with purple accent when gliding
- **Riding indicator**: shows mount name + speed when on horse/boat/etc
- **Diamond Y-level marker**: coordinate bar shows diamond icon when at optimal mining depth (Y -64 to 16)
- **Day/night progress bar**: visual timeline with sunset marker
- Hostile/passive/villager entity counts with total within 128 blocks
- Slime chunk indicator
- Sound indicators (25+ mob types: Creeper, Zombie, Skeleton, Enderman, Witch, Pillager, Ravager, Evoker, and more)
- **Sound source markers**: hostile sounds appear as fading red dots on minimap, other sounds as yellow
- **Player name labels**: other players show their name above their minimap marker
- **Circular minimap mode**: toggle between square and circular shape (C key)
- Compass bearing (degrees) shown alongside facing direction
- Day/night countdown timer + moon phase display during night
- Active status effects (up to 3, with name + level + timer)
- Tool durability with color-coded warning (green/yellow/red)
- Ore Y-level reference in Calculator (Diamond, Iron, Gold, Lapis, Emerald, Ancient Debris)
- Danger-responsive HUD border (tints red near hostiles or at light level 0)
- Low HP warning (coordinate bar flashes red at 2 hearts)
- **Automatic "Home" waypoint** when sleeping in a bed (per-dimension)
- Pulsing cross-shaped death markers on minimap
- Waypoint list sorted by distance (same dimension first)
- Keybinding hints reference in Settings tab

### Performance Stats
- FPS and TPS monitoring
- Memory usage (used/max)
- Loaded chunk count
- Render distance
- Multiplayer ping
- Entity count
- **Minecraft version display**
- Server brand + online player count

### Waypoint System
- Quick-add waypoints at current position (B key)
- **Delete nearest waypoint** (X key, within 32 blocks)
- **HUD corner cycling** (G key: Top-Right → Top-Left → Bottom-Right → Bottom-Left)
- Death markers (auto-created on death, per-dimension)
- Dimension-aware: waypoints record their dimension, only show on correct dimension's minimap
- Color-coded waypoints with distance display (cross-dimension waypoints show [Nether]/[End]/[OW])
- JSON-based persistence
- Visible on minimap with labels

### Calculator
- Dimension-aware Nether portal coordinate converter (auto-detects current dimension)
- XP level reference (Level 30, Level 50)
- Spawn chunk radius calculator
- Enchanting power calculator (bookshelves needed)
- **Ore Y-level reference**: optimal depths for Diamond, Iron, Gold, Lapis, Emerald, Ancient Debris
- **Mob spawning reference**: light levels, despawn range, mob cap
- **Elytra flight reference**: max speed, glide angle, durability
- **Furnace timing reference**: furnace, blast furnace, smoker speeds
- **Crop growth reference**: wheat, sugar cane, bamboo growth times
- **Redstone reference**: signal range, repeater delays, hopper speed
- **Villager trading reference**: zombie curing, restock schedule, Hero of the Village discounts
- **Fall damage reference**: safe height, fatal height, water landing

### Settings
- Toggle all features on/off
- Configurable HUD position, scale, opacity
- Map zoom range
- North lock toggle
- Sound indicator toggle
- Player trail toggle
- HUD corner position (G key)
- Seed override for multiplayer servers

### Seed-Based Prediction
- Predicts biome types for any coordinate using the world seed
- Predicts structure locations using Minecraft's spacing/separation/salt algorithm
- Dimension-aware: only shows structures for the current dimension
- **Overworld** (15 types): Village, Desert Temple, Jungle Temple, Swamp Hut, Igloo, Pillager Outpost, Ocean Monument, Woodland Mansion, Stronghold (ring algorithm), Ocean Ruin, Shipwreck, Ruined Portal, Ancient City, Trail Ruins, Trial Chamber (1.21.2+)
- **Nether** (3 types): Bastion Remnant, Nether Fortress (mutually exclusive per region), Ruined Portal
- **End** (1 type): End City (outer islands only, >1024 blocks from origin)
- Slime chunk calculation and minimap overlay (Overworld only)
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
| `C` | Toggle circular/square minimap |
| `L` | Toggle north-locked/rotation-following mode |
| `V` | Copy coordinates to clipboard |
| `X` | Delete nearest waypoint (within 32 blocks) |
| `G` | Cycle HUD corner position |

All keybindings are configurable in Minecraft's Controls settings under the "SeedSight" category.

## Version-Specific Features

| Feature | 1.21.0–1.21.3 | 1.21.4 |
|---------|:-:|:-:|
| Pale Garden biome colors | — | ✓ |
| Creaking mob sound indicator | — | ✓ |
| Trial Chamber structure prediction | ✓ (1.21.2+) | ✓ |
| All other features | ✓ | ✓ |

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

Bedrock branches use the [Script API](https://learn.microsoft.com/en-us/minecraft/creator/scriptapi/) (`@minecraft/server`) and run as behavior pack addons with TypeScript.

| Branch | Minecraft | Script API |
|--------|-----------|------------|
| `bedrock-1.21.80` | 1.21.80 | @minecraft/server 1.17.0-beta |
| `bedrock-1.21.132` | 1.21.132 | @minecraft/server 1.18.0-beta |

## Installation

### Requirements
- Minecraft Java Edition 1.21.x
- [Fabric Loader](https://fabricmc.net/use/) 0.16.0 or later
- [Fabric API](https://modrinth.com/mod/fabric-api)

### Steps
1. Install Fabric Loader for your Minecraft version
2. Download the Fabric API mod and place it in your `mods` folder
3. Download the SeedSight JAR for your Minecraft version from [Releases](https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod/releases)
4. Place the SeedSight JAR in your `mods` folder
5. Launch Minecraft with the Fabric profile

### Multiplayer Note
On multiplayer servers, the world seed is not sent to clients. To use seed-based prediction features:
- Use the `/seed` command if you have operator permissions
- Or manually enter the seed in SeedSight Settings (press `N` to cycle to Settings tab)

## Building from Source

### Requirements
- Java 21 (JDK)
- Git

### Build
```bash
git clone https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod.git
cd Minecraft-SeedSight-Map-Mod
./gradlew build
```

The built mod JAR will be in `build/libs/seedsight-1.0.0.jar`.

## Configuration

Config file is stored at `.minecraft/config/seedsight.json`. All settings can be changed in-game via the Settings tab.

## License

MIT License - see [LICENSE](LICENSE) for details.

## Credits

Created by [Evan1108-Coder](https://github.com/Evan1108-Coder)
