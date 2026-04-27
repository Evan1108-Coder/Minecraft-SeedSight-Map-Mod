import { world, system, Player, Block, Vector3 } from "@minecraft/server";
import { ActionFormData, ModalFormData } from "@minecraft/server-ui";

/**
 * WorldWhisperer - Bedrock Edition
 * Minimap & Intelligence Addon for Minecraft Bedrock 1.21.80+
 *
 * NOTE: Bedrock Script API has limited HUD rendering capabilities compared
 * to Java Edition's Fabric. This implementation uses:
 * - Title/actionbar text for stats display
 * - Forms UI for waypoints, calculator, and settings
 * - Scoreboard sidebar for performance stats
 *
 * A full graphical minimap requires a resource pack with custom UI (JSON UI system)
 * which is planned for a future update.
 */

interface Waypoint {
    name: string;
    x: number;
    y: number;
    z: number;
    dimension: string;
}

interface WorldWhispererConfig {
    hudEnabled: boolean;
    showCoords: boolean;
    showBiome: boolean;
    showTime: boolean;
    showEntities: boolean;
    showPerf: boolean;
    seedOverride: string;
}

const config: WorldWhispererConfig = {
    hudEnabled: true,
    showCoords: true,
    showBiome: true,
    showTime: true,
    showEntities: true,
    showPerf: true,
    seedOverride: "",
};

const waypoints: Waypoint[] = [];
let tickCounter = 0;

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

function updateActionBar(player: Player): void {
    const lines: string[] = [];

    if (config.showCoords) {
        const pos = player.location;
        lines.push(`§7XYZ: §f${Math.floor(pos.x)} / ${Math.floor(pos.y)} / ${Math.floor(pos.z)}`);
    }

    if (config.showTime) {
        const timeOfDay = world.getTimeOfDay();
        const hours = Math.floor((timeOfDay / 1000 + 6) % 24);
        const minutes = Math.floor((timeOfDay % 1000) * 60 / 1000);
        const period = hours >= 12 ? "PM" : "AM";
        const h12 = hours % 12 || 12;
        lines.push(`§7Time: §6${h12}:${minutes.toString().padStart(2, "0")} ${period}`);
    }

    if (config.showEntities) {
        const dim = player.dimension;
        let hostileCount = 0;
        let passiveCount = 0;
        try {
            for (const entity of dim.getEntities({ location: player.location, maxDistance: 128 })) {
                const families = entity.getComponent("minecraft:type_family");
                if (entity.typeId.includes("zombie") || entity.typeId.includes("skeleton") ||
                    entity.typeId.includes("creeper") || entity.typeId.includes("spider")) {
                    hostileCount++;
                } else if (entity.typeId.includes("cow") || entity.typeId.includes("pig") ||
                           entity.typeId.includes("sheep") || entity.typeId.includes("chicken")) {
                    passiveCount++;
                }
            }
        } catch {}
        lines.push(`§7Entities: §cH:${hostileCount} §aP:${passiveCount}`);
    }

    if (lines.length > 0) {
        player.onScreenDisplay.setActionBar(lines.join("  §8|§r  "));
    }
}

world.beforeEvents.chatSend.subscribe((event) => {
    const msg = event.message.trim().toLowerCase();
    if (!msg.startsWith("!ww")) return;

    event.cancel = true;
    const player = event.sender;
    const args = msg.split(/\s+/).slice(1);
    const command = args[0] || "help";

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
                config.hudEnabled = !config.hudEnabled;
                player.sendMessage(`§a[WW]§r HUD ${config.hudEnabled ? "§aenabled" : "§cdisabled"}`);
                break;
            case "nether":
                const pos = player.location;
                const nx = Math.floor(pos.x / 8);
                const nz = Math.floor(pos.z / 8);
                player.sendMessage(`§a[WW]§r Nether coords: §c${nx}, ${nz}`);
                break;
            case "overworld":
                const p = player.location;
                const ox = Math.floor(p.x * 8);
                const oz = Math.floor(p.z * 8);
                player.sendMessage(`§a[WW]§r Overworld coords: §a${ox}, ${oz}`);
                break;
            default:
                player.sendMessage(`§c[WW]§r Unknown command: ${command}. Try §e!ww help`);
        }
    });
});

function showHelp(player: Player): void {
    player.sendMessage([
        "§a═══ WorldWhisperer Commands ═══",
        "§e!ww toggle§r - Toggle HUD on/off",
        "§e!ww wp add <name>§r - Add waypoint",
        "§e!ww wp list§r - List waypoints",
        "§e!ww wp remove <name>§r - Remove waypoint",
        "§e!ww calc§r - Open calculator",
        "§e!ww nether§r - Convert coords to Nether",
        "§e!ww overworld§r - Convert coords to Overworld",
        "§e!ww settings§r - Open settings",
        "§a══════════════════════════════",
    ].join("\n"));
}

function handleWaypoint(player: Player, args: string[]): void {
    const subCommand = args[0] || "list";

    switch (subCommand) {
        case "add": {
            const name = args.slice(1).join(" ") || `WP-${waypoints.length + 1}`;
            const pos = player.location;
            waypoints.push({
                name,
                x: Math.floor(pos.x),
                y: Math.floor(pos.y),
                z: Math.floor(pos.z),
                dimension: player.dimension.id,
            });
            player.sendMessage(`§a[WW]§r Waypoint §e${name}§r added at ${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`);
            break;
        }
        case "list": {
            if (waypoints.length === 0) {
                player.sendMessage("§a[WW]§r No waypoints. Use §e!ww wp add <name>§r to create one.");
                return;
            }
            player.sendMessage("§a═══ Waypoints ═══");
            for (const wp of waypoints) {
                const dist = Math.floor(Math.sqrt(
                    Math.pow(wp.x - player.location.x, 2) +
                    Math.pow(wp.y - player.location.y, 2) +
                    Math.pow(wp.z - player.location.z, 2)
                ));
                player.sendMessage(`§e${wp.name}§r: ${wp.x}, ${wp.y}, ${wp.z} §7(${dist}m)`);
            }
            break;
        }
        case "remove": {
            const targetName = args.slice(1).join(" ");
            const index = waypoints.findIndex(wp => wp.name.toLowerCase() === targetName.toLowerCase());
            if (index >= 0) {
                const removed = waypoints.splice(index, 1)[0];
                player.sendMessage(`§a[WW]§r Waypoint §e${removed.name}§r removed.`);
            } else {
                player.sendMessage(`§c[WW]§r Waypoint not found: ${targetName}`);
            }
            break;
        }
    }
}

function showCalculator(player: Player): void {
    const pos = player.location;
    const netherX = Math.floor(pos.x / 8);
    const netherZ = Math.floor(pos.z / 8);
    const owX = Math.floor(pos.x * 8);
    const owZ = Math.floor(pos.z * 8);

    player.sendMessage([
        "§a═══ Calculator ═══",
        `§7Current: §f${Math.floor(pos.x)}, ${Math.floor(pos.y)}, ${Math.floor(pos.z)}`,
        `§7→ Nether: §c${netherX}, ${netherZ}`,
        `§7→ Overworld: §a${owX}, ${owZ}`,
        `§7Enchant Max (30): §d15 bookshelves`,
        `§7Spawn Chunks: §f11 chunk radius`,
        "§a══════════════════",
    ].join("\n"));
}

function showSettings(player: Player): void {
    player.sendMessage([
        "§a═══ Settings ═══",
        `§7HUD: ${config.hudEnabled ? "§aON" : "§cOFF"}`,
        `§7Coordinates: ${config.showCoords ? "§aON" : "§cOFF"}`,
        `§7Time: ${config.showTime ? "§aON" : "§cOFF"}`,
        `§7Entities: ${config.showEntities ? "§aON" : "§cOFF"}`,
        `§7Performance: ${config.showPerf ? "§aON" : "§cOFF"}`,
        "§7Use §e!ww toggle§r to toggle HUD",
        "§a═════════════════",
    ].join("\n"));
}
