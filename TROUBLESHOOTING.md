# SeedSight Troubleshooting

## Build Errors

### `java.lang.UnsupportedClassVersionError` or Java version mismatch
**Cause**: You're using the wrong Java version.
**Fix**: SeedSight requires Java 21. Check your version:
```bash
java -version
```
If it's not 21, install JDK 21 and set `JAVA_HOME`:
```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew build
```

### `Could not resolve all files for configuration`
**Cause**: Gradle can't download dependencies (network issue or wrong Minecraft version).
**Fix**:
1. Ensure you have internet access
2. Verify you're on the correct branch for your Minecraft version
3. Run `./gradlew build --refresh-dependencies`

### `Execution failed for task ':compileJava'`
**Cause**: Source code compilation error, usually from branch/version mismatch.
**Fix**: Make sure you're on the right branch:
```bash
git checkout java-1.21.4   # or your target version
./gradlew clean build
```

### Build succeeds but no JAR in `build/libs/`
**Cause**: The `remapJar` task may have been skipped.
**Fix**: Run the full build:
```bash
./gradlew clean build
```
Look for `seedsight-1.0.0.jar` (not the `-dev` or `-sources` JAR).

## Runtime Errors

### Mod doesn't load / not visible in mod list
**Cause**: Missing Fabric API or wrong Fabric Loader version.
**Fix**:
1. Ensure Fabric API `.jar` is in your `mods/` folder
2. Update Fabric Loader to 0.16.0+ from [fabricmc.net](https://fabricmc.net/use/)
3. Check that the mod JAR is the remapped one (not `-dev`)

### Crash on startup: `Mixin apply failed`
**Cause**: Version mismatch between the mod and your Minecraft version.
**Fix**: Use the branch that matches your exact Minecraft version. For example, `java-1.21.2` for Minecraft 1.21.2. Do not use the `java-1.21.4` branch with Minecraft 1.21.1.

### HUD not appearing
**Cause**: HUD might be toggled off or hidden.
**Fix**:
1. Press **H** to toggle HUD visibility
2. Press **F1** to make sure Minecraft's HUD isn't hidden
3. Check if another mod is conflicting (try with only SeedSight + Fabric API)

### Minimap is blank / shows only black
**Cause**: The map hasn't loaded chunks yet, or you're in a dimension transition.
**Fix**:
1. Walk around for a few seconds to let chunks load
2. The map cache clears on dimension change — this is normal
3. If underground, the map should show cave terrain; if it's blank, toggle cave mode by going above/below Y=56

### Seed-based features not working (no structures/biomes predicted)
**Cause**: No world seed available.
**Fix**:
1. In singleplayer, the seed is read automatically
2. In multiplayer, enter the seed manually in Settings tab (press **N** to cycle to it)
3. Check the seed is correct — wrong seeds produce wrong predictions

### Low FPS / performance issues
**Cause**: Map scanning is CPU-intensive, especially at high zoom levels.
**Fix**:
1. Reduce zoom level (press **-**)
2. Use compact mode instead of expanded (**M** to toggle)
3. SeedSight is compatible with Sodium — install it for better overall performance
4. Close the expanded view when not needed

### Waypoints not saving
**Cause**: Config directory permission issue.
**Fix**:
1. Check that `.minecraft/config/` exists and is writable
2. Waypoints are stored in `.minecraft/config/seedsight.json`
3. If the file is corrupted, delete it and restart — SeedSight will create a fresh one

### Crash: `NoSuchMethodError` or `NoSuchFieldError`
**Cause**: Minecraft version doesn't match the mod branch. Yarn mappings differ between versions.
**Fix**: Use the exact branch for your version. Don't mix branches.

## Compatibility

### Works with
- Fabric API (required)
- Sodium (recommended for performance)
- Most client-side Fabric mods

### Known conflicts
- Other minimap mods (Xaero's, JourneyMap) may overlap HUD positions — adjust the corner position with **G** key
- Mods that heavily modify chunk rendering may affect terrain scanning accuracy

## Getting Help

If your issue isn't listed here, check:
1. The correct branch is checked out for your Minecraft version
2. Fabric Loader and Fabric API are up to date
3. Java 21 is installed and active

File an issue at [GitHub Issues](https://github.com/Evan1108-Coder/Minecraft-SeedSight-Map-Mod/issues) with your Minecraft version, mod version, and the full crash log.
