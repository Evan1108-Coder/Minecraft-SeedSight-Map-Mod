import { world, system, Player, Entity, Vector3 } from "@minecraft/server";

interface Waypoint {
    name: string;
    x: number;
    y: number;
    z: number;
    dimension: string;
    color: string;
}

interface WorldWhispererConfig {
    hudEnabled: boolean;
    showCoords: boolean;
    showDirection: boolean;
    showTime: boolean;
    showEntities: boolean;
    showLight: boolean;
    seedOverride: string;
}

const HOSTILE_TYPES = [
    "zombie", "skeleton", "creeper", "spider", "enderman", "witch",
    "blaze", "ghast", "slime", "magma_cube", "phantom", "drowned",
    "husk", "stray", "wither_skeleton", "pillager", "vindicator",
    "evoker", "ravager", "guardian", "elder_guardian", "piglin_brute",
    "warden", "breeze", "bogged",
];

const PASSIVE_TYPES = [
    "cow", "pig", "sheep", "chicken", "horse", "donkey", "mule",
    "rabbit", "fox", "bee", "goat", "frog", "camel", "sniffer",
    "cat", "wolf", "parrot", "axolotl", "turtle", "dolphin",
    "squid", "glow_squid", "cod", "salmon", "tropical_fish", "pufferfish",
];

const WAYPOINT_COLORS = ["§c", "§a", "§b", "§e", "§d", "§6", "§5", "§3", "§9", "§7"];

const config: WorldWhispererConfig = {
    hudEnabled: true,
    showCoords: true,
    showDirection: true,
    showTime: true,
    showEntities: true,
    showLight: true,
    seedOverride: "",
};

const waypoints: Waypoint[] = [];
let tickCounter = 0;
let colorIndex = 0;

const deathMarkers: Map<string, Waypoint> = new Map();

world.afterEvents.worldInitialize.subscribe(() => {
    world.sendMessage("§a[WorldWhisperer]§r Bedrock Edition loaded! Use §e!ww§r for commands.");
});

system.runInterval(() => {
    tickCounter++;
    if (!config.hudEnabled) return;

    for (const player of world.getAllPlayers()) {
        if (tickCounter % 20 === 0) {
            updateActionBar(player);
        }
    }
}, 1);

world.afterEvents.entityDie.subscribe((event) => {
    const entity = event.deadEntity;
    if (entity.typeId !== "minecraft:player") return;
    const pos = entity.location;
    const name = entity.nameTag || "Player";
    const marker: Waypoint = {
        name: `§cDeath`,
        x: Math.floor(pos.x),
        y: Math.floor(pos.y),
        z: Math.floor(pos.z),
        dimension: entity.dimension.id,
        color: "§c",
    };
    deathMarkers.set(name, marker);
    try {
        (entity as Player).sendMessage(
            `§c[WW]§r Death marker set at §e${marker.x}, ${marker.y}, ${marker.z}`
        );
    } catch {}
});

function getDirection(yaw: number): string {
    const normalized = ((yaw % 360) + 360) % 360;
    if (normalized >= 315 || normalized < 45) return "S (+Z)";
    if (normalized >= 45 && normalized < 135) return "W (-X)";
    if (normalized >= 135 && normalized < 225) return "N (-Z)";
    return "E (+X)";
}

function updateActionBar(player: Player): void {
    const lines: string[] = [];
    const pos = player.location;

    if (config.showCoords) {
        lines.push(`§7XYZ: §f${Math.floor(pos.x)} / ${Math.floor(pos.y)} / ${Math.floor(pos.z)}`);
    }

    if (config.showDirection) {
        const dir = getDirection(player.getRotation().y);
        lines.push(`§7Dir: §f${dir}`);
    }

    if (config.showTime) {
        const timeOfDay = world.getTimeOfDay();
        const hours = Math.floor((timeOfDay / 1000 + 6) % 24);
        const minutes = Math.floor((timeOfDay % 1000) * 60 / 1000);
        const period = hours >= 12 ? "PM" : "AM";
        const h12 = hours % 12 || 12;
        const day = Math.floor(world.getAbsoluteTime() / 24000);
        lines.push(`§7D${day} §6${h12}:${minutes.toString().padStart(2, "0")} ${period}`);
    }

    if (config.showEntities) {
        const dim = player.dimension;
        let hostileCount = 0;
        let passiveCount = 0;
        try {
            for (const entity of dim.getEntities({ location: pos, maxDistance: 128 })) {
                const typeId = entity.typeId.replace("minecraft:", "");
                if (HOSTILE_TYPES.some((t) => typeId.includes(t))) {
                    hostileCount++;
                } else if (PASSIVE_TYPES.some((t) => typeId.includes(t))) {
                    passiveCount++;
                }
            }
        } catch {}
        lines.push(`§cH:${hostileCount} §aP:${passiveCount}`);
    }

    if (lines.length > 0) {
        player.onScreenDisplay.setActionBar(lines.join("  §8|§r  "));
    }
}

world.beforeEvents.chatSend.subscribe((event) => {
    const msg = event.message.trim();
    if (!msg.toLowerCase().startsWith("!ww")) return;

    event.cancel = true;
    const player = event.sender;
    const args = msg.split(/\s+/).slice(1);
    const command = (args[0] || "help").toLowerCase();

    system.run(() => {
        switch (command) {
            case "help":
                showHelp(player);
                break;
            case "wp":
            case "waypoint":
                handleWaypoint(player, args.slice(1));
                break;
            case "calc":
            case "calculator":
                showCalculator(player);
                break;
            case "settings":
                showSettings(player);
                break;
            case "toggle":
                handleToggle(player, args.slice(1));
                break;
            case "nether": {
                const p = player.location;
                player.sendMessage(`§a[WW]§r Nether coords: §c${Math.floor(p.x / 8)}, ${Math.floor(p.z / 8)}`);
                break;
            }
            case "overworld": {
                const p = player.location;
                player.sendMessage(`§a[WW]§r Overworld coords: §a${Math.floor(p.x * 8)}, ${Math.floor(p.z * 8)}`);
                break;
            }
            case "death":
                showDeathMarker(player);
                break;
            default:
                player.sendMessage(`§c[WW]§r Unknown command: ${command}. Try §e!ww help`);
        }
    });
});

function handleToggle(player: Player, args: string[]): void {
    const setting = (args[0] || "hud").toLowerCase();
    switch (setting) {
        case "hud":
            config.hudEnabled = !config.hudEnabled;
            player.sendMessage(`§a[WW]§r HUD ${config.hudEnabled ? "§aON" : "§cOFF"}`);
            break;
        case "coords":
            config.showCoords = !config.showCoords;
            player.sendMessage(`§a[WW]§r Coords ${config.showCoords ? "§aON" : "§cOFF"}`);
            break;
        case "time":
            config.showTime = !config.showTime;
            player.sendMessage(`§a[WW]§r Time ${config.showTime ? "§aON" : "§cOFF"}`);
            break;
        case "entities":
            config.showEntities = !config.showEntities;
            player.sendMessage(`§a[WW]§r Entities ${config.showEntities ? "§aON" : "§cOFF"}`);
            break;
        case "direction":
        case "dir":
            config.showDirection = !config.showDirection;
            player.sendMessage(`§a[WW]§r Direction ${config.showDirection ? "§aON" : "§cOFF"}`);
            break;
        default:
            player.sendMessage("§c[WW]§r Toggle options: hud, coords, time, entities, direction");
    }
}

function showHelp(player: Player): void {
    player.sendMessage([
        "§a═══ WorldWhisperer Commands ═══",
        "§e!ww toggle [setting]§r - Toggle HUD/coords/time/entities/direction",
        "§e!ww wp add <name>§r - Add waypoint",
        "§e!ww wp list§r - List waypoints with distance",
        "§e!ww wp remove <name>§r - Remove waypoint",
        "§e!ww wp nearest§r - Show nearest waypoint",
        "§e!ww calc§r - Show calculator",
        "§e!ww nether§r - Convert coords to Nether",
        "§e!ww overworld§r - Convert coords to Overworld",
        "§e!ww death§r - Show last death location",
        "§e!ww settings§r - Show current settings",
        "§a══════════════════════════════",
    ].join("\n"));
}

function handleWaypoint(player: Player, args: string[]): void {
    const subCommand = (args[0] || "list").toLowerCase();

    switch (subCommand) {
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
                `§a[WW]§r Waypoint ${color}${name}§r added at ${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`
            );
            break;
        }
        case "list": {
            if (waypoints.length === 0) {
                player.sendMessage("§a[WW]§r No waypoints. Use §e!ww wp add <name>§r to create one.");
                return;
            }
            player.sendMessage("§a═══ Waypoints ═══");
            for (const wp of waypoints) {
                const dist = Math.floor(
                    Math.sqrt(
                        (wp.x - player.location.x) ** 2 +
                        (wp.y - player.location.y) ** 2 +
                        (wp.z - player.location.z) ** 2
                    )
                );
                player.sendMessage(`${wp.color}${wp.name}§r: ${wp.x}, ${wp.y}, ${wp.z} §7(${dist}m)`);
            }
            break;
        }
        case "remove": {
            const targetName = args.slice(1).join(" ").toLowerCase();
            const index = waypoints.findIndex((wp) => wp.name.toLowerCase() === targetName);
            if (index >= 0) {
                const removed = waypoints.splice(index, 1)[0];
                player.sendMessage(`§a[WW]§r Waypoint ${removed.color}${removed.name}§r removed.`);
            } else {
                player.sendMessage(`§c[WW]§r Waypoint not found: ${targetName}`);
            }
            break;
        }
        case "nearest": {
            if (waypoints.length === 0) {
                player.sendMessage("§a[WW]§r No waypoints set.");
                return;
            }
            let nearest = waypoints[0];
            let nearestDist = Infinity;
            for (const wp of waypoints) {
                const dist = Math.sqrt(
                    (wp.x - player.location.x) ** 2 +
                    (wp.y - player.location.y) ** 2 +
                    (wp.z - player.location.z) ** 2
                );
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = wp;
                }
            }
            player.sendMessage(
                `§a[WW]§r Nearest: ${nearest.color}${nearest.name}§r at ${nearest.x}, ${nearest.y}, ${nearest.z} §7(${Math.floor(nearestDist)}m)`
            );
            break;
        }
        default:
            player.sendMessage("§c[WW]§r Usage: !ww wp [add|list|remove|nearest]");
    }
}

function showDeathMarker(player: Player): void {
    const name = player.nameTag || "Player";
    const marker = deathMarkers.get(name);
    if (!marker) {
        player.sendMessage("§a[WW]§r No death marker recorded yet.");
        return;
    }
    const dist = Math.floor(
        Math.sqrt(
            (marker.x - player.location.x) ** 2 +
            (marker.y - player.location.y) ** 2 +
            (marker.z - player.location.z) ** 2
        )
    );
    player.sendMessage(
        `§c[WW]§r Last death: §e${marker.x}, ${marker.y}, ${marker.z}§r §7(${dist}m away)`
    );
}

function showCalculator(player: Player): void {
    const pos = player.location;
    const netherX = Math.floor(pos.x / 8);
    const netherZ = Math.floor(pos.z / 8);
    const owX = Math.floor(pos.x * 8);
    const owZ = Math.floor(pos.z * 8);

    const isNether = player.dimension.id === "minecraft:nether";

    player.sendMessage([
        "§a═══ Calculator ═══",
        `§7Current: §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
        isNether
            ? `§7→ Overworld: §a${owX}, ${owZ}`
            : `§7→ Nether: §c${netherX}, ${netherZ}`,
        `§7Enchant Max (30): §d15 bookshelves`,
        `§7Spawn Chunks: §f11 chunk radius`,
        `§7XP for Level 30: §a1395 XP`,
        "§a══════════════════",
    ].join("\n"));
}

function showSettings(player: Player): void {
    const on = "§aON";
    const off = "§cOFF";
    player.sendMessage([
        "§a═══ Settings ═══",
        `§7HUD: ${config.hudEnabled ? on : off}`,
        `§7Coordinates: ${config.showCoords ? on : off}`,
        `§7Direction: ${config.showDirection ? on : off}`,
        `§7Time: ${config.showTime ? on : off}`,
        `§7Entities: ${config.showEntities ? on : off}`,
        "§7Use §e!ww toggle <setting>§r to change",
        "§a═════════════════",
    ].join("\n"));
}
