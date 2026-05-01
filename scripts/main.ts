import { world, system, Player, Vector3 } from "@minecraft/server";

// ─── Interfaces ───

interface Waypoint {
    name: string;
    x: number;
    y: number;
    z: number;
    dimension: string;
    color: string;
}

interface SeedSightConfig {
    hudEnabled: boolean;
    showCoords: boolean;
    showDirection: boolean;
    showTime: boolean;
    showEntities: boolean;
    showHealth: boolean;
    showWeather: boolean;
    scanRadius: number;
    hudMode: "compact" | "full";
}

interface SessionStats {
    startTime: number;
    blocksWalked: number;
    mobsKilled: number;
    deaths: number;
}

interface PlayerTracker {
    lastPos: Vector3 | null;
    sessionStats: SessionStats;
}

// ─── Constants ───

const HOSTILE_TYPES = [
    "zombie", "skeleton", "creeper", "spider", "enderman", "witch",
    "blaze", "ghast", "slime", "magma_cube", "phantom", "drowned",
    "husk", "stray", "wither_skeleton", "pillager", "vindicator",
    "evoker", "ravager", "guardian", "elder_guardian", "piglin_brute",
    "warden", "breeze", "bogged", "hoglin", "zoglin", "shulker",
    "vex", "silverfish", "endermite", "cave_spider",
];

const PASSIVE_TYPES = [
    "cow", "pig", "sheep", "chicken", "horse", "donkey", "mule",
    "rabbit", "fox", "bee", "goat", "frog", "camel", "sniffer",
    "cat", "wolf", "parrot", "axolotl", "turtle", "dolphin",
    "squid", "glow_squid", "cod", "salmon", "tropical_fish", "pufferfish",
    "mooshroom", "panda", "polar_bear", "ocelot", "bat", "strider",
    "allay", "armadillo", "snow_golem", "iron_golem",
];

const NEUTRAL_TYPES = [
    "trader_llama", "llama", "piglin", "zombified_piglin",
];

const WAYPOINT_COLORS = ["§c", "§a", "§b", "§e", "§d", "§6", "§5", "§3", "§9", "§7"];

const DIRECTION_ARROWS: Record<string, string> = {
    "N": "↑ N", "NE": "↗ NE", "E": "→ E", "SE": "↘ SE",
    "S": "↓ S", "SW": "↙ SW", "W": "← W", "NW": "↖ NW",
};

const OVERWORLD_STRUCTURES = [
    { name: "Village", spacing: 34, separation: 8, salt: 10387312 },
    { name: "Desert Pyramid", spacing: 32, separation: 8, salt: 14357617 },
    { name: "Jungle Pyramid", spacing: 32, separation: 8, salt: 14357619 },
    { name: "Swamp Hut", spacing: 32, separation: 8, salt: 14357620 },
    { name: "Pillager Outpost", spacing: 32, separation: 8, salt: 165745296 },
    { name: "Ocean Monument", spacing: 32, separation: 5, salt: 10387313 },
    { name: "Woodland Mansion", spacing: 80, separation: 20, salt: 10387319 },
    { name: "Igloo", spacing: 32, separation: 8, salt: 14357618 },
    { name: "Ruined Portal", spacing: 40, separation: 15, salt: 34222645 },
    { name: "Shipwreck", spacing: 24, separation: 4, salt: 165745295 },
    { name: "Ocean Ruin", spacing: 20, separation: 8, salt: 14357621 },
    { name: "Trial Chamber", spacing: 34, separation: 12, salt: 94251327 },
];

const NETHER_STRUCTURES = [
    { name: "Fortress", spacing: 27, separation: 4, salt: 30084232 },
    { name: "Bastion", spacing: 27, separation: 4, salt: 30084232 },
];

// ─── State ───

const config: SeedSightConfig = {
    hudEnabled: true,
    showCoords: true,
    showDirection: true,
    showTime: true,
    showEntities: true,
    showHealth: true,
    showWeather: false,
    scanRadius: 128,
    hudMode: "compact",
};

const waypoints: Waypoint[] = [];
let tickCounter = 0;
let colorIndex = 0;
const deathMarkers: Map<string, Waypoint[]> = new Map();
const playerTrackers: Map<string, PlayerTracker> = new Map();
let worldSeed: string = "";

// ─── Initialization ───

world.afterEvents.worldInitialize.subscribe(() => {
    world.sendMessage(
        "§a[SeedSight]§r Bedrock Edition v2.1 loaded! §e!ss help§r for commands.\n" +
        "§7Features: HUD, waypoints, structures, slime chunks, stats, calculator"
    );
});

// ─── Main tick loop ───

system.runInterval(() => {
    try {
        tickCounter++;
        if (!config.hudEnabled) return;

        for (const player of world.getAllPlayers()) {
            const tracker = getTracker(player);

            if (tickCounter % 2 === 0) {
                trackMovement(player, tracker);
            }

            if (tickCounter % 20 === 0) {
                updateActionBar(player, tracker);
            }
        }
    } catch {}
}, 1);

function getTracker(player: Player): PlayerTracker {
    const name = player.name || "Player";
    if (!playerTrackers.has(name)) {
        playerTrackers.set(name, {
            lastPos: null,
            sessionStats: {
                startTime: Date.now(),
                blocksWalked: 0,
                mobsKilled: 0,
                deaths: 0,
            },
        });
    }
    return playerTrackers.get(name)!;
}

function trackMovement(player: Player, tracker: PlayerTracker): void {
    const pos = player.location;
    if (tracker.lastPos) {
        const dx = pos.x - tracker.lastPos.x;
        const dz = pos.z - tracker.lastPos.z;
        const dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0.05 && dist < 20) {
            tracker.sessionStats.blocksWalked += dist;
        }
    }
    tracker.lastPos = { x: pos.x, y: pos.y, z: pos.z };
}

// ─── Death tracking ───

world.afterEvents.entityDie.subscribe((event: any) => {
    const entity = event.deadEntity;
    if (entity.typeId !== "minecraft:player") return;

    const pos = entity.location;
    const name = entity.nameTag || "Player";
    const dimId = entity.dimension.id.replace("minecraft:", "");
    const dimLabel = dimId === "the_nether" ? " §c(Nether)" : dimId === "the_end" ? " §d(End)" : "";

    const marker: Waypoint = {
        name: `Death${dimLabel}`,
        x: Math.floor(pos.x),
        y: Math.floor(pos.y),
        z: Math.floor(pos.z),
        dimension: entity.dimension.id,
        color: "§c",
    };

    if (!deathMarkers.has(name)) deathMarkers.set(name, []);
    const markers = deathMarkers.get(name)!;
    markers.unshift(marker);
    if (markers.length > 5) markers.pop();

    const tracker = playerTrackers.get(name);
    if (tracker) tracker.sessionStats.deaths++;

    try {
        (entity as Player).sendMessage(
            `§c[SS]§r Death marker set at §e${marker.x}, ${marker.y}, ${marker.z}${dimLabel}`
        );
    } catch {}
});

// ─── Entity kill tracking ───

world.afterEvents.entityDie.subscribe((event: any) => {
    if (!event.damageSource?.damagingEntity) return;
    const killer = event.damageSource.damagingEntity;
    if (killer.typeId !== "minecraft:player") return;
    const name = killer.nameTag || (killer as Player).name || "Player";
    const tracker = playerTrackers.get(name);
    if (tracker) tracker.sessionStats.mobsKilled++;
});

// ─── Direction helpers ───

function getDirection8(yaw: number): string {
    const n = ((yaw % 360) + 360) % 360;
    if (n >= 337.5 || n < 22.5) return "S";
    if (n < 67.5) return "SW";
    if (n < 112.5) return "W";
    if (n < 157.5) return "NW";
    if (n < 202.5) return "N";
    if (n < 247.5) return "NE";
    if (n < 292.5) return "E";
    return "SE";
}

function getDirectionArrow(yaw: number): string {
    return DIRECTION_ARROWS[getDirection8(yaw)] || "?";
}

// ─── Action bar HUD ───

function updateActionBar(player: Player, tracker: PlayerTracker): void {
    const lines: string[] = [];
    const pos = player.location;

    if (config.showCoords) {
        const dimId = player.dimension.id.replace("minecraft:", "");
        const dimLabel = dimId === "the_nether" ? " §c[Nether]" :
                         dimId === "the_end" ? " §d[End]" : "";
        lines.push(`§f${Math.floor(pos.x)} ${Math.floor(pos.y)} ${Math.floor(pos.z)}${dimLabel}`);
    }

    if (config.showDirection) {
        lines.push(`§f${getDirectionArrow(player.getRotation().y)}`);
    }

    if (config.showHealth) {
        try {
            const hp = Math.ceil(player.getComponent("minecraft:health")?.currentValue ?? 20);
            const hpColor = hp > 14 ? "§a" : hp > 7 ? "§e" : "§c";
            lines.push(`${hpColor}${hp}hp`);
        } catch {}
    }

    if (config.showTime) {
        const timeOfDay = ((world.getTimeOfDay() % 24000) + 24000) % 24000;
        const hours = Math.floor((timeOfDay / 1000 + 6) % 24);
        const minutes = Math.floor((timeOfDay % 1000) * 60 / 1000);
        const period = hours >= 12 ? "PM" : "AM";
        const h12 = hours % 12 || 12;
        lines.push(`§6${h12}:${minutes.toString().padStart(2, "0")}${period}`);
    }

    if (config.showEntities) {
        const counts = countEntities(player);
        lines.push(`§c${counts.hostile}§7/§a${counts.passive}`);
    }

    if (config.showWeather) {
        const weather = getWeatherString();
        lines.push(weather);
    }

    if (config.hudMode === "full") {
        const walked = Math.floor(tracker.sessionStats.blocksWalked);
        if (walked > 0) lines.push(`§7${walked}m`);
        lines.push(`§c⚔${tracker.sessionStats.mobsKilled}`);
    }

    if (lines.length > 0) {
        player.onScreenDisplay.setActionBar(lines.join(" §8|§r "));
    }
}

// ─── Entity counting ───

interface EntityCounts {
    hostile: number;
    passive: number;
    neutral: number;
    total: number;
    breakdown: Map<string, number>;
}

function countEntities(player: Player): EntityCounts {
    const counts: EntityCounts = {
        hostile: 0, passive: 0, neutral: 0, total: 0,
        breakdown: new Map(),
    };

    try {
        for (const entity of player.dimension.getEntities({
            location: player.location,
            maxDistance: config.scanRadius,
        })) {
            if (entity.typeId === "minecraft:player") continue;
            const typeId = entity.typeId.replace("minecraft:", "");
            counts.total++;
            counts.breakdown.set(typeId, (counts.breakdown.get(typeId) || 0) + 1);

            if (HOSTILE_TYPES.some((t) => typeId === t)) {
                counts.hostile++;
            } else if (NEUTRAL_TYPES.some((t) => typeId === t)) {
                counts.neutral++;
            } else if (PASSIVE_TYPES.some((t) => typeId === t)) {
                counts.passive++;
            } else {
                counts.neutral++;
            }
        }
    } catch {}

    return counts;
}

// ─── Weather ───

function getWeatherString(): string {
    try {
        const timeOfDay = world.getTimeOfDay();
        const isDay = timeOfDay >= 0 && timeOfDay < 12000;
        if (!isDay) return "§8☾ Night";
        return "§e☀ Day";
    } catch {}
    return "§7?";
}

// ─── Slime chunk finder ───

function isSlimeChunk(chunkX: number, chunkZ: number, seed: number): boolean {
    let x = seed + (chunkX * chunkX * 4987142 | 0) +
            (chunkX * 5947611 | 0) +
            (chunkZ * chunkZ * 4392871 | 0) +
            (chunkZ * 389711 | 0);
    x = x ^ (x >>> 16);
    x = (x * 0x45d9f3b) | 0;
    x = x ^ (x >>> 16);
    return (Math.abs(x) % 10) === 0;
}

function findSlimeChunksNear(playerX: number, playerZ: number, seed: number, radius: number): { x: number, z: number }[] {
    const chunks: { x: number, z: number }[] = [];
    const cx = Math.floor(playerX / 16);
    const cz = Math.floor(playerZ / 16);
    const r = Math.ceil(radius / 16);

    for (let dx = -r; dx <= r; dx++) {
        for (let dz = -r; dz <= r; dz++) {
            if (isSlimeChunk(cx + dx, cz + dz, seed)) {
                chunks.push({ x: cx + dx, z: cz + dz });
            }
        }
    }
    return chunks;
}

// ─── Structure finder (seed-based prediction) ───

function predictStructurePos(
    seed: number, chunkX: number, chunkZ: number,
    spacing: number, separation: number, salt: number
): { x: number, z: number } {
    const regionX = Math.floor(chunkX / spacing);
    const regionZ = Math.floor(chunkZ / spacing);
    const range = spacing - separation;

    let hash = (regionX * 341873128712 + regionZ * 132897987541 + seed + salt) | 0;
    hash = hash ^ (hash >>> 16);
    hash = (hash * 0x45d9f3b) | 0;
    hash = hash ^ (hash >>> 16);

    const offX = Math.abs(hash % range);
    hash = (hash * 0x45d9f3b) | 0;
    const offZ = Math.abs(hash % range);

    return {
        x: (regionX * spacing + offX) * 16 + 8,
        z: (regionZ * spacing + offZ) * 16 + 8,
    };
}

function findNearbyStructures(
    playerX: number, playerZ: number, seed: number,
    dimension: string
): { name: string, x: number, z: number, dist: number }[] {
    const results: { name: string, x: number, z: number, dist: number }[] = [];
    const isNether = dimension === "minecraft:the_nether";
    const structures = isNether ? NETHER_STRUCTURES :
                       dimension === "minecraft:the_end" ? [] : OVERWORLD_STRUCTURES;

    const chunkX = Math.floor(playerX / 16);
    const chunkZ = Math.floor(playerZ / 16);
    const netherRegionPicked: Map<string, string> = new Map();

    for (const struct of structures) {
        let nearest = { x: 0, z: 0, dist: Infinity };

        for (let rx = -3; rx <= 3; rx++) {
            for (let rz = -3; rz <= 3; rz++) {
                const searchChunkX = chunkX + rx * struct.spacing;
                const searchChunkZ = chunkZ + rz * struct.spacing;
                const pos = predictStructurePos(seed, searchChunkX, searchChunkZ, struct.spacing, struct.separation, struct.salt);

                if (isNether) {
                    const regionX = Math.floor(searchChunkX / struct.spacing);
                    const regionZ = Math.floor(searchChunkZ / struct.spacing);
                    const regionKey = `${regionX},${regionZ}`;
                    if (!netherRegionPicked.has(regionKey)) {
                        let rHash = (seed + regionX * 341873128712 + regionZ * 132897987541 + 30084232) | 0;
                        rHash = rHash ^ (rHash >>> 16);
                        netherRegionPicked.set(regionKey, (Math.abs(rHash) % 5 < 2) ? "Fortress" : "Bastion");
                    }
                    if (netherRegionPicked.get(regionKey) !== struct.name) continue;
                }

                const dist = Math.sqrt((pos.x - playerX) ** 2 + (pos.z - playerZ) ** 2);
                if (dist < nearest.dist) {
                    nearest = { x: pos.x, z: pos.z, dist };
                }
            }
        }

        if (nearest.dist < 10000) {
            results.push({ name: struct.name, x: nearest.x, z: nearest.z, dist: Math.floor(nearest.dist) });
        }
    }

    // Strongholds (Overworld only, ring-based)
    if (dimension === "minecraft:overworld") {
        const strongholds = predictStrongholds(seed);
        let nearestSH = { x: 0, z: 0, dist: Infinity };
        for (const sh of strongholds) {
            const dist = Math.sqrt((sh.x - playerX) ** 2 + (sh.z - playerZ) ** 2);
            if (dist < nearestSH.dist) {
                nearestSH = { x: sh.x, z: sh.z, dist };
            }
        }
        if (nearestSH.dist < Infinity) {
            results.push({ name: "Stronghold", x: nearestSH.x, z: nearestSH.z, dist: Math.floor(nearestSH.dist) });
        }
    }

    // End City
    if (dimension === "minecraft:the_end") {
        const endCity = predictEndCity(seed, playerX, playerZ);
        if (endCity) {
            results.push(endCity);
        }
    }

    results.sort((a, b) => a.dist - b.dist);
    return results;
}

function predictStrongholds(seed: number): { x: number, z: number }[] {
    const results: { x: number, z: number }[] = [];
    const counts = [3, 6, 10, 15, 21, 28, 36, 9];
    const startDist = [1408, 4480, 7552, 10624, 13696, 16768, 19840, 22912];
    let hash = seed;

    for (let ring = 0; ring < 3; ring++) {
        const count = counts[ring];
        const dist = startDist[ring];
        const angleStep = (2 * Math.PI) / count;
        hash = (hash * 6364136223846793005 + 1442695040888963407) | 0;
        let angle = (Math.abs(hash) % 628318) / 100000;

        for (let i = 0; i < count; i++) {
            const x = Math.floor(Math.cos(angle) * dist);
            const z = Math.floor(Math.sin(angle) * dist);
            results.push({ x, z });
            angle += angleStep;
        }
    }
    return results;
}

function predictEndCity(seed: number, playerX: number, playerZ: number): { name: string, x: number, z: number, dist: number } | null {
    let nearest = { x: 0, z: 0, dist: Infinity };
    const gridSize = 20;
    const gx = Math.floor(playerX / (gridSize * 16));
    const gz = Math.floor(playerZ / (gridSize * 16));

    for (let rx = -2; rx <= 2; rx++) {
        for (let rz = -2; rz <= 2; rz++) {
            const regionX = gx + rx;
            const regionZ = gz + rz;
            let hash = (seed + regionX * 341873128712 + regionZ * 132897987541 + 10387313) | 0;
            hash = hash ^ (hash >>> 16);
            const cx = regionX * gridSize * 16 + Math.abs(hash % (gridSize * 16));
            hash = (hash * 0x45d9f3b) | 0;
            const cz = regionZ * gridSize * 16 + Math.abs(hash % (gridSize * 16));
            const dist = Math.sqrt((cx - playerX) ** 2 + (cz - playerZ) ** 2);
            if (dist < nearest.dist) {
                nearest = { x: cx, z: cz, dist };
            }
        }
    }

    if (nearest.dist < Infinity) {
        return { name: "End City", x: nearest.x, z: nearest.z, dist: Math.floor(nearest.dist) };
    }
    return null;
}

// ─── Chat command handler ───

world.beforeEvents.chatSend.subscribe((event: any) => {
    const msg = event.message.trim();
    if (!msg.toLowerCase().startsWith("!ss ") && msg.toLowerCase() !== "!ss") return;

    event.cancel = true;
    const player = event.sender;
    const args = msg.split(/\s+/).slice(1);
    const command = (args[0] || "help").toLowerCase();

    system.run(() => {
        switch (command) {
            case "help":
                showHelp(player);
                break;
            case "help2":
                showHelp2(player);
                break;
            case "wp":
            case "waypoint":
                handleWaypoint(player, args.slice(1));
                break;
            case "calc":
            case "calculator":
                handleCalc(player, args.slice(1));
                break;
            case "settings":
                showSettings(player);
                break;
            case "toggle":
                handleToggle(player, args.slice(1));
                break;
            case "portal":
            case "nether":
            case "overworld": {
                showPortalCalc(player);
                break;
            }
            case "death":
                showDeathMarkers(player);
                break;
            case "stats":
                showStats(player);
                break;
            case "entities":
            case "scan":
                showEntityScan(player);
                break;
            case "seed":
                handleSeed(player, args.slice(1));
                break;
            case "slime":
                showSlimeChunks(player);
                break;
            case "structures":
            case "struct":
                showStructures(player);
                break;
            case "stronghold":
            case "sh":
                showStrongholds(player);
                break;
            case "xp":
                showXpCalc(player, args.slice(1));
                break;
            case "hud":
                handleHudMode(player, args.slice(1));
                break;
            case "radius":
                handleRadius(player, args.slice(1));
                break;
            case "home":
                setHomeWaypoint(player);
                break;
            case "distance":
            case "dist":
                showDistanceTo(player, args.slice(1));
                break;
            default:
                player.sendMessage(`§c[SS]§r Unknown command: §e${command}§r. Try §e!ss help`);
        }
    });
});

// ─── Toggle commands ───

function handleToggle(player: Player, args: string[]): void {
    const setting = (args[0] || "").toLowerCase();
    const toggleMap: Record<string, { key: keyof SeedSightConfig, label: string }> = {
        "hud": { key: "hudEnabled", label: "HUD" },
        "coords": { key: "showCoords", label: "Coordinates" },
        "time": { key: "showTime", label: "Time" },
        "entities": { key: "showEntities", label: "Entities" },
        "direction": { key: "showDirection", label: "Direction" },
        "dir": { key: "showDirection", label: "Direction" },
        "health": { key: "showHealth", label: "Health" },
        "hp": { key: "showHealth", label: "Health" },
        "weather": { key: "showWeather", label: "Weather" },
    };

    if (!setting || !toggleMap[setting]) {
        player.sendMessage("§c[SS]§r Options: hud, coords, time, entities, direction, health, weather");
        return;
    }

    const t = toggleMap[setting];
    (config as any)[t.key] = !(config as any)[t.key];
    player.sendMessage(`§a[SS]§r ${t.label} ${(config as any)[t.key] ? "§aON" : "§cOFF"}`);
}

// ─── Help ───

function showHelp(player: Player): void {
    player.sendMessage([
        "§a═══ SeedSight v2.1 Commands ═══",
        "§6--- HUD & Display ---",
        "§e!ss toggle <opt>§r - Toggle: hud/coords/time/entities/dir/health/weather",
        "§e!ss hud [compact|full]§r - Switch HUD display mode",
        "§e!ss radius <blocks>§r - Set entity scan radius (default 128)",
        "",
        "§6--- Navigation ---",
        "§e!ss wp add <name>§r - Save current location as waypoint",
        "§e!ss wp list§r - List all waypoints with distances",
        "§e!ss wp remove <name>§r - Delete a waypoint",
        "§e!ss wp nearest§r - Show nearest waypoint + direction",
        "§e!ss home§r - Save current location as Home waypoint",
        "§e!ss death§r - Show death history (last 5)",
        "§e!ss dist <x> <z>§r - Distance to coordinates",
        "",
        "§7Type §e!ss help2§r for more commands",
        "§a══════════════════════════════════",
    ].join("\n"));
}

function showHelp2(player: Player): void {
    player.sendMessage([
        "§a═══ SeedSight v2.1 Commands (2/2) ═══",
        "§6--- World Analysis ---",
        "§e!ss seed <number>§r - Set world seed for predictions",
        "§e!ss structures§r - Find nearby structures",
        "§e!ss stronghold§r - Find nearest strongholds",
        "§e!ss slime§r - Check slime chunks nearby",
        "§e!ss scan§r - Detailed entity breakdown",
        "",
        "§6--- Utilities ---",
        "§e!ss portal§r - Nether/Overworld coordinate conversion",
        "§e!ss calc§r - General calculator & reference",
        "§e!ss calc circle <r>§r - Circle area calculator",
        "§e!ss xp <level>§r - XP needed for a level",
        "§e!ss stats§r - Session statistics",
        "§e!ss settings§r - View all current settings",
        "§a═════════════════════════════════════",
    ].join("\n"));
}

// ─── Waypoint system ───

function handleWaypoint(player: Player, args: string[]): void {
    const sub = (args[0] || "list").toLowerCase();

    switch (sub) {
        case "add": {
            const name = args.slice(1).join(" ") || `WP-${waypoints.length + 1}`;
            const pos = player.location;
            const color = WAYPOINT_COLORS[colorIndex % WAYPOINT_COLORS.length];
            colorIndex++;
            waypoints.push({
                name,
                x: Math.floor(pos.x),
                y: Math.floor(pos.y),
                z: Math.floor(pos.z),
                dimension: player.dimension.id,
                color,
            });
            player.sendMessage(
                `§a[SS]§r Waypoint ${color}${name}§r saved at §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`
            );
            break;
        }
        case "list": {
            const dimWps = waypoints.filter(wp => wp.dimension === player.dimension.id);
            if (dimWps.length === 0) {
                player.sendMessage("§a[SS]§r No waypoints in this dimension. Use §e!ss wp add <name>§r to create one.");
                return;
            }
            player.sendMessage(`§a═══ Waypoints (${dimWps.length}) ═══`);
            for (const wp of dimWps) {
                const dist = distance2D(wp.x, wp.z, player.location.x, player.location.z);
                const dir = directionTo(player, wp.x, wp.z);
                player.sendMessage(`${wp.color}${wp.name}§r: §f${wp.x}, ${wp.y}, ${wp.z} §7(${dist}m ${dir})`);
            }
            if (waypoints.length > dimWps.length) {
                player.sendMessage(`§7(+${waypoints.length - dimWps.length} in other dimensions)`);
            }
            break;
        }
        case "remove":
        case "delete": {
            const targetName = args.slice(1).join(" ").toLowerCase();
            if (!targetName) {
                const nearest = findNearestWaypoint(player);
                if (nearest && distance3D(nearest, player.location) < 32) {
                    const idx = waypoints.indexOf(nearest);
                    if (idx >= 0) waypoints.splice(idx, 1);
                    player.sendMessage(`§a[SS]§r Removed nearest: ${nearest.color}${nearest.name}§r`);
                } else {
                    player.sendMessage("§c[SS]§r Specify name or stand within 32 blocks of a waypoint.");
                }
                return;
            }
            const index = waypoints.findIndex((wp) => wp.name.toLowerCase() === targetName);
            if (index >= 0) {
                const removed = waypoints.splice(index, 1)[0];
                player.sendMessage(`§a[SS]§r Removed ${removed.color}${removed.name}§r`);
            } else {
                player.sendMessage(`§c[SS]§r Not found: §e${targetName}`);
            }
            break;
        }
        case "nearest": {
            const nearest = findNearestWaypoint(player);
            if (!nearest) {
                player.sendMessage("§a[SS]§r No waypoints set.");
                return;
            }
            const dist = distance3D(nearest, player.location);
            const dir = directionTo(player, nearest.x, nearest.z);
            player.sendMessage(
                `§a[SS]§r Nearest: ${nearest.color}${nearest.name}§r at §f${nearest.x}, ${nearest.y}, ${nearest.z} §7(${dist}m ${dir})`
            );
            break;
        }
        case "clear": {
            const count = waypoints.length;
            waypoints.length = 0;
            player.sendMessage(`§a[SS]§r Cleared ${count} waypoints.`);
            break;
        }
        default:
            player.sendMessage("§c[SS]§r Usage: !ss wp [add|list|remove|nearest|clear]");
    }
}

function findNearestWaypoint(player: Player): Waypoint | null {
    if (waypoints.length === 0) return null;
    let nearest: Waypoint | null = null;
    let nearestDist = Infinity;
    for (const wp of waypoints) {
        if (wp.dimension !== player.dimension.id) continue;
        const dist = distance3D(wp, player.location);
        if (dist < nearestDist) {
            nearestDist = dist;
            nearest = wp;
        }
    }
    return nearest;
}

function setHomeWaypoint(player: Player): void {
    const existing = waypoints.findIndex(wp => wp.name === "Home" && wp.dimension === player.dimension.id);
    if (existing >= 0) waypoints.splice(existing, 1);

    const pos = player.location;
    waypoints.push({
        name: "Home",
        x: Math.floor(pos.x),
        y: Math.floor(pos.y),
        z: Math.floor(pos.z),
        dimension: player.dimension.id,
        color: "§a",
    });
    player.sendMessage(`§a[SS]§r Home set at §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`);
}

// ─── Distance helpers ───

function distance2D(x1: number, z1: number, x2: number, z2: number): number {
    return Math.floor(Math.sqrt((x1 - x2) ** 2 + (z1 - z2) ** 2));
}

function distance3D(wp: { x: number, y: number, z: number }, pos: Vector3): number {
    return Math.floor(Math.sqrt((wp.x - pos.x) ** 2 + (wp.y - pos.y) ** 2 + (wp.z - pos.z) ** 2));
}

function directionTo(player: Player, targetX: number, targetZ: number): string {
    const dx = targetX - player.location.x;
    const dz = targetZ - player.location.z;
    const angle = Math.atan2(dx, -dz) * 180 / Math.PI;
    const n = ((angle % 360) + 360) % 360;
    if (n >= 337.5 || n < 22.5) return "N";
    if (n < 67.5) return "NE";
    if (n < 112.5) return "E";
    if (n < 157.5) return "SE";
    if (n < 202.5) return "S";
    if (n < 247.5) return "SW";
    if (n < 292.5) return "W";
    return "NW";
}

function showDistanceTo(player: Player, args: string[]): void {
    if (args.length < 2) {
        player.sendMessage("§c[SS]§r Usage: §e!ss dist <x> <z>§r or §e!ss dist <x> <y> <z>");
        return;
    }
    const x = parseInt(args[0]);
    const z = args.length >= 3 ? parseInt(args[2]) : parseInt(args[1]);
    const y = args.length >= 3 ? parseInt(args[1]) : Math.floor(player.location.y);

    if (isNaN(x) || isNaN(z)) {
        player.sendMessage("§c[SS]§r Invalid coordinates.");
        return;
    }

    const dist = distance3D({ x, y, z }, player.location);
    const dir = directionTo(player, x, z);
    player.sendMessage(`§a[SS]§r Distance to §f${x}, ${y}, ${z}§r: §e${dist}m §7(${dir})`);
}

// ─── Death markers ───

function showDeathMarkers(player: Player): void {
    const name = player.name || "Player";
    const markers = deathMarkers.get(name);
    if (!markers || markers.length === 0) {
        player.sendMessage("§a[SS]§r No deaths recorded this session.");
        return;
    }
    player.sendMessage(`§c═══ Death History (${markers.length}) ═══`);
    for (let i = 0; i < markers.length; i++) {
        const m = markers[i];
        const dist = distance3D(m, player.location);
        const dir = directionTo(player, m.x, m.z);
        player.sendMessage(`§c#${i + 1}§r ${m.name}: §f${m.x}, ${m.y}, ${m.z} §7(${dist}m ${dir})`);
    }
}

// ─── Entity scanner ───

function showEntityScan(player: Player): void {
    const counts = countEntities(player);
    if (counts.total === 0) {
        player.sendMessage(`§a[SS]§r No entities within ${config.scanRadius} blocks.`);
        return;
    }

    player.sendMessage([
        `§a═══ Entity Scan (${config.scanRadius}m radius) ═══`,
        `§cHostile: ${counts.hostile} §8|§r §aPassive: ${counts.passive} §8|§r §7Other: ${counts.neutral}`,
        `§7Total: §f${counts.total}`,
    ].join("\n"));

    const sorted = [...counts.breakdown.entries()].sort((a, b) => b[1] - a[1]);
    const top = sorted.slice(0, 10);
    if (top.length > 0) {
        player.sendMessage("§7--- Top entities ---");
        for (const [type, count] of top) {
            const isHostile = HOSTILE_TYPES.some(t => type === t);
            const color = isHostile ? "§c" : "§a";
            player.sendMessage(`  ${color}${type}§r: §f${count}`);
        }
        if (sorted.length > 10) {
            player.sendMessage(`  §7... and ${sorted.length - 10} more types`);
        }
    }
}

// ─── Seed management ───

function handleSeed(player: Player, args: string[]): void {
    if (args.length === 0) {
        if (worldSeed) {
            player.sendMessage(`§a[SS]§r Current seed: §e${worldSeed}`);
        } else {
            player.sendMessage("§a[SS]§r No seed set. Use §e!ss seed <number>§r to enable structure/slime predictions.");
        }
        return;
    }
    const seed = args[0];
    worldSeed = seed;
    player.sendMessage(`§a[SS]§r Seed set to §e${seed}§r. Structure and slime chunk predictions enabled.`);
}

// ─── Slime chunks ───

function showSlimeChunks(player: Player): void {
    if (!worldSeed) {
        player.sendMessage("§a[SS]§r Set seed first: §e!ss seed <number>");
        return;
    }
    if (player.dimension.id !== "minecraft:overworld") {
        player.sendMessage("§c[SS]§r Slime chunks only exist in the Overworld.");
        return;
    }

    const seed = parseInt(worldSeed) || hashString(worldSeed);
    const pos = player.location;
    const cx = Math.floor(pos.x / 16);
    const cz = Math.floor(pos.z / 16);
    const currentIsSlime = isSlimeChunk(cx, cz, seed);

    const nearbyChunks = findSlimeChunksNear(pos.x, pos.z, seed, 80);

    player.sendMessage([
        "§a═══ Slime Chunks ═══",
        `§7Current chunk (${cx}, ${cz}): ${currentIsSlime ? "§a YES - Slime Chunk!" : "§c No"}`,
        `§7Nearby slime chunks (5 chunk radius): §f${nearbyChunks.length}`,
    ].join("\n"));

    if (nearbyChunks.length > 0) {
        const closest = nearbyChunks
            .map(c => ({
                ...c,
                dist: Math.sqrt(((c.x * 16 + 8) - pos.x) ** 2 + ((c.z * 16 + 8) - pos.z) ** 2)
            }))
            .sort((a, b) => a.dist - b.dist)
            .slice(0, 5);

        player.sendMessage("§7--- Nearest slime chunks ---");
        for (const c of closest) {
            const blockX = c.x * 16 + 8;
            const blockZ = c.z * 16 + 8;
            const dir = directionTo(player, blockX, blockZ);
            player.sendMessage(`  §aChunk (${c.x}, ${c.z})§r → §fX:${blockX} Z:${blockZ} §7(${Math.floor(c.dist)}m ${dir})`);
        }
    }

    player.sendMessage("§7Slime spawns: Y < 40, Overworld only, light level 7 or less.");
}

// ─── Structure finder ───

function showStructures(player: Player): void {
    if (!worldSeed) {
        player.sendMessage("§a[SS]§r Set seed first: §e!ss seed <number>");
        return;
    }

    const seed = parseInt(worldSeed) || hashString(worldSeed);
    const pos = player.location;
    const structures = findNearbyStructures(pos.x, pos.z, seed, player.dimension.id);

    if (structures.length === 0) {
        player.sendMessage("§a[SS]§r No predicted structures nearby for this dimension.");
        return;
    }

    const dimName = player.dimension.id.replace("minecraft:", "").replace("_", " ");
    player.sendMessage(`§a═══ Nearby Structures (${dimName}) ═══`);

    for (const s of structures.slice(0, 12)) {
        const dir = directionTo(player, s.x, s.z);
        const distColor = s.dist < 500 ? "§a" : s.dist < 2000 ? "§e" : "§7";
        player.sendMessage(`§6${s.name}§r: §f${s.x}, ${s.z} §7(${distColor}${s.dist}m ${dir}§7)`);
    }

    player.sendMessage("§7Note: Predictions are approximate and may vary from actual generation.");
}

function showStrongholds(player: Player): void {
    if (!worldSeed) {
        player.sendMessage("§a[SS]§r Set seed first: §e!ss seed <number>");
        return;
    }
    if (player.dimension.id !== "minecraft:overworld") {
        player.sendMessage("§c[SS]§r Strongholds only exist in the Overworld.");
        return;
    }

    const seed = parseInt(worldSeed) || hashString(worldSeed);
    const strongholds = predictStrongholds(seed);
    const pos = player.location;

    const sorted = strongholds
        .map(sh => ({ ...sh, dist: Math.floor(Math.sqrt((sh.x - pos.x) ** 2 + (sh.z - pos.z) ** 2)) }))
        .sort((a, b) => a.dist - b.dist);

    player.sendMessage(`§a═══ Strongholds (nearest 5 of ${strongholds.length}) ═══`);
    for (const sh of sorted.slice(0, 5)) {
        const dir = directionTo(player, sh.x, sh.z);
        player.sendMessage(`§d Stronghold§r: §f${sh.x}, ${sh.z} §7(${sh.dist}m ${dir})`);
    }
    player.sendMessage("§7Tip: Strongholds generate at Y~30. Use Eye of Ender to pinpoint exact location.");
}

// ─── Calculator ───

function handleCalc(player: Player, args: string[]): void {
    if (args.length > 0 && args[0].toLowerCase() === "circle") {
        const r = parseFloat(args[1] || "0");
        if (r <= 0) {
            player.sendMessage("§c[SS]§r Usage: §e!ss calc circle <radius>");
            return;
        }
        const area = Math.PI * r * r;
        const circum = 2 * Math.PI * r;
        player.sendMessage([
            `§a═══ Circle Calculator (r=${r}) ═══`,
            `§7Area: §f${area.toFixed(1)} blocks²`,
            `§7Circumference: §f${circum.toFixed(1)} blocks`,
            `§7Diameter: §f${(r * 2).toFixed(1)} blocks`,
            "§a════════════════════════════",
        ].join("\n"));
        return;
    }

    const pos = player.location;
    const inNether = player.dimension.id === "minecraft:the_nether";
    const inEnd = player.dimension.id === "minecraft:the_end";

    const lines = [
        "§a═══ SeedSight Calculator ═══",
        `§7Position: §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
        `§7Chunk: §f${Math.floor(pos.x / 16)}, ${Math.floor(pos.z / 16)}`,
    ];

    if (inNether) {
        lines.push(`§7→ Overworld: §a${Math.floor(pos.x * 8)}, ${Math.floor(pos.z * 8)}`);
    } else if (!inEnd) {
        lines.push(`§7→ Nether: §c${Math.floor(pos.x / 8)}, ${Math.floor(pos.z / 8)}`);
    }

    lines.push(
        "",
        "§6--- Reference ---",
        "§7Enchant Lv30: §d15 bookshelves",
        "§7Diamond ore: §fY -64 to 16 (best Y -59)",
        "§7Ancient debris: §fY 8 to 22 (best Y 15)",
        "§7Iron ore: §fY -24 to 56 (best Y 16)",
        "§7Gold ore: §fY -64 to 32 (best Y -16)",
        "§7Emerald ore: §fY -16 to 320 (mountains)",
        "§7Lapis lazuli: §fY -64 to 64 (best Y 0)",
        "§7Redstone: §fY -64 to 16 (best Y -59)",
        "",
        "§7Spawn chunks: §f11 chunk radius",
        "§7Mob spawning: §f24-128 blocks from player",
        "§a════════════════════════════",
    );

    player.sendMessage(lines.join("\n"));
}

function showPortalCalc(player: Player): void {
    const pos = player.location;
    const inNether = player.dimension.id === "minecraft:the_nether";
    const inEnd = player.dimension.id === "minecraft:the_end";

    if (inEnd) {
        player.sendMessage("§c[SS]§r Portal calculation not available in The End.");
        return;
    }

    if (inNether) {
        const owX = Math.floor(pos.x * 8);
        const owZ = Math.floor(pos.z * 8);
        player.sendMessage([
            `§a[SS]§r §cNether §7→ §aOverworld`,
            `§7Current: §c${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
            `§7Target:  §a${owX}, ~, ${owZ}`,
            `§7Build portal at these Overworld coordinates.`,
        ].join("\n"));
    } else {
        const nX = Math.floor(pos.x / 8);
        const nZ = Math.floor(pos.z / 8);
        player.sendMessage([
            `§a[SS]§r §aOverworld §7→ §cNether`,
            `§7Current: §a${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
            `§7Target:  §c${nX}, ~, ${nZ}`,
            `§7Build portal at these Nether coordinates.`,
        ].join("\n"));
    }
}

// ─── XP Calculator ───

function showXpCalc(player: Player, args: string[]): void {
    const level = parseInt(args[0] || "30");
    if (isNaN(level) || level < 1) {
        player.sendMessage("§c[SS]§r Usage: §e!ss xp <level>");
        return;
    }

    let totalXp = 0;
    for (let i = 0; i < level; i++) {
        if (i < 16) totalXp += 2 * i + 7;
        else if (i < 31) totalXp += 5 * i - 38;
        else totalXp += 9 * i - 158;
    }

    player.sendMessage([
        `§a═══ XP Calculator ═══`,
        `§7XP to reach level §e${level}§7: §a${totalXp} XP`,
        `§7Level 30: §a1395 XP §7(enchanting)`,
        `§7Level 50: §a5345 XP`,
        `§7Tip: Each ore gives ~3-7 XP, mobs give 1-5 XP`,
        `§a═════════════════════`,
    ].join("\n"));
}

// ─── Session stats ───

function showStats(player: Player): void {
    const name = player.name || "Player";
    const tracker = playerTrackers.get(name);
    if (!tracker) {
        player.sendMessage("§a[SS]§r No stats recorded yet. Play for a bit!");
        return;
    }

    const elapsed = Date.now() - tracker.sessionStats.startTime;
    const minutes = Math.floor(elapsed / 60000);
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    const timeStr = hours > 0 ? `${hours}h ${mins}m` : `${mins}m`;

    const walked = Math.floor(tracker.sessionStats.blocksWalked);

    player.sendMessage([
        "§a═══ Session Statistics ═══",
        `§7Play time: §f${timeStr}`,
        `§7Distance walked: §f${walked} blocks${walked > 1000 ? ` (${(walked / 1000).toFixed(1)}km)` : ""}`,
        `§7Mobs killed: §c${tracker.sessionStats.mobsKilled}`,
        `§7Deaths: §4${tracker.sessionStats.deaths}`,
        `§7Waypoints: §e${waypoints.length}`,
        `§7K/D Ratio: §f${tracker.sessionStats.deaths > 0 ? (tracker.sessionStats.mobsKilled / tracker.sessionStats.deaths).toFixed(1) : tracker.sessionStats.mobsKilled.toString()}`,
        "§a══════════════════════════",
    ].join("\n"));
}

// ─── Settings ───

function showSettings(player: Player): void {
    const on = "§aON";
    const off = "§cOFF";
    player.sendMessage([
        "§a═══ SeedSight Settings ═══",
        `§7HUD: ${config.hudEnabled ? on : off}`,
        `§7  Coordinates: ${config.showCoords ? on : off}`,
        `§7  Direction: ${config.showDirection ? on : off}`,
        `§7  Time: ${config.showTime ? on : off}`,
        `§7  Entities: ${config.showEntities ? on : off}`,
        `§7  Health: ${config.showHealth ? on : off}`,
        `§7  Weather: ${config.showWeather ? on : off}`,
        `§7HUD Mode: §e${config.hudMode}`,
        `§7Scan Radius: §e${config.scanRadius} blocks`,
        `§7World Seed: ${worldSeed ? `§e${worldSeed}` : "§7not set"}`,
        `§7Waypoints: §e${waypoints.length}`,
        "",
        "§7Use §e!ss toggle <setting>§r to change",
        "§a═══════════════════════════",
    ].join("\n"));
}

// ─── HUD mode ───

function handleHudMode(player: Player, args: string[]): void {
    const mode = (args[0] || "").toLowerCase();
    if (mode === "compact" || mode === "full") {
        config.hudMode = mode;
        player.sendMessage(`§a[SS]§r HUD mode: §e${mode}`);
    } else {
        player.sendMessage(`§a[SS]§r Current: §e${config.hudMode}§r. Options: §ecompact§r, §efull`);
    }
}

// ─── Scan radius ───

function handleRadius(player: Player, args: string[]): void {
    const r = parseInt(args[0] || "0");
    if (r < 16 || r > 256) {
        player.sendMessage(`§a[SS]§r Current radius: §e${config.scanRadius}§r. Set 16-256: §e!ss radius <blocks>`);
        return;
    }
    config.scanRadius = r;
    player.sendMessage(`§a[SS]§r Scan radius set to §e${r} blocks`);
}

// ─── Utility ───

function hashString(s: string): number {
    let hash = 0;
    for (let i = 0; i < s.length; i++) {
        hash = ((hash << 5) - hash + s.charCodeAt(i)) | 0;
    }
    return hash;
}
