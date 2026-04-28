# SeedSight Bedrock Edition - Troubleshooting

## Build Errors

### `Cannot find module '@minecraft/server'`
**Cause**: Dependencies not installed.
**Fix**: Run `npm install` in the project directory before building.

### TypeScript compilation errors
**Cause**: Wrong Node.js or TypeScript version.
**Fix**: Ensure Node.js 18+ is installed:
```bash
node --version   # should be 18+
npm install
npm run build
```

## Runtime Errors

### Addon doesn't load / no message on world join
**Cause**: Beta APIs not enabled or behavior pack not activated.
**Fix**:
1. Edit the world settings
2. Go to **Behavior Packs** and confirm SeedSight is activated
3. Go to **Experiments** and enable **Beta APIs**
4. Re-enter the world

### `[Scripting] Error: Module @minecraft/server not found`
**Cause**: Minecraft version doesn't match the addon branch.
**Fix**: Use the correct branch for your Minecraft version:
- Minecraft 1.21.80 → `bedrock-1.21.80` branch
- Minecraft 1.21.132 → `bedrock-1.21.132` branch

### Commands not working (`!ss` does nothing)
**Cause**: The chat command listener may not be active.
**Fix**:
1. Make sure you type `!ss` exactly (lowercase, no space before `!`)
2. Verify the addon loaded (you should see the green welcome message on world join)
3. Try `/reload` to re-initialize scripts

### HUD / actionbar not showing
**Cause**: Another addon or command may be overwriting the actionbar.
**Fix**:
1. Type `!ss toggle hud` to make sure HUD is enabled
2. Remove other behavior packs that use the actionbar
3. Check that Beta APIs are enabled in Experiments

### Entity counts always show 0
**Cause**: Entity query may be failing silently.
**Fix**: This can happen in certain world states. Walk to an area with visible mobs and wait a few seconds. The count updates every second (20 ticks).

## Installation Issues

### `.mcpack` file doesn't open in Minecraft
**Cause**: File association not set, or Minecraft not installed from the official store.
**Fix**:
1. Right-click the `.mcpack` file → Open With → Minecraft
2. Alternatively, rename it to `.zip`, extract, and manually copy the `behavior_pack` folder to `development_behavior_packs`

### Pack shows "outdated" or version warning
**Cause**: Minecraft version doesn't match the addon's `min_engine_version`.
**Fix**: Update Minecraft to the version matching the branch you're using, or switch to the branch that matches your version.

## Getting Help

If your issue isn't listed here:
1. Verify Beta APIs are enabled
2. Confirm the correct branch for your Minecraft version
3. Try removing and re-adding the behavior pack

File an issue at [GitHub Issues](https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod/issues) with your Minecraft version and a description of the problem.
