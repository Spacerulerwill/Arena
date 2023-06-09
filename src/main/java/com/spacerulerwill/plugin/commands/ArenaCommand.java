package com.spacerulerwill.plugin.commands;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.spacerulerwill.plugin.util.CountdownTimer;
import com.spacerulerwill.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ArenaCommand implements CommandExecutor {
    private final Plugin plugin;
    private CountdownTimer timer;
    private boolean isCountingDown = false;

    public ArenaCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    // try and create block for cage around player
    public void tryCreateCageBlock(World world, Location location, Material material) {
        Block block = world.getBlockAt(location);
        if (block.isEmpty() || block.isLiquid() || !block.isSolid() || block.isPassable() || block.getType() == Material.BARRIER) {
            block.setType(material);
        }
    }

    // create cage around player
    public void createPlayerCage(World world, Location location, Material material) {
        for (int i = 0; i < 2; i++) {
            Location left = new Location(world, location.getX() - 1.0, location.getY() + i, location.getZ());
            Location right = new Location(world, location.getX() + 1.0, location.getY() + i, location.getZ());
            Location forward = new Location(world, location.getX(), location.getY() + i, location.getZ() + 1.0);
            Location back = new Location(world, location.getX(), location.getY() + i, location.getZ() - 1.0);

            tryCreateCageBlock(world, left, material);
            tryCreateCageBlock(world, right, material);
            tryCreateCageBlock(world, forward, material);
            tryCreateCageBlock(world, back, material);
        }

        Location top = new Location(world, location.getX(), location.getY() + 2.0, location.getZ());
        tryCreateCageBlock(world, top, material);

        Location below = new Location(world, location.getX(), location.getY() - 1.0, location.getZ());
        tryCreateCageBlock(world, below, material);
    }

    public boolean startArena(Player p, String[] args) {
        int borderSize;

        try {
            borderSize = Integer.parseInt(args[1]);

            if (borderSize < 150) {
                p.sendMessage(Component.text("§CArgument §EborderSize §Cmust be atleast 150!"));
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            borderSize = 150;
        } catch (NumberFormatException e) {
            p.sendMessage(Component.text("§CArgument §EborderSize §Cmust be an integer value"));
            return true;
        }

        plugin.setPlaying(true);

        p.getServer().sendMessage(Component.text("§aArena starting!"));

        World world = p.getWorld();
        Random rand = new Random();

        // set world border to radius
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(p.getLocation());
        worldBorder.setSize(borderSize);

        // set to daytime
        world.setTime(6000);

        Location playerLocation = p.getLocation();
        Location newPlayerLocation = playerLocation;

        // teleport players to random location inside world border
        int half = borderSize / 2;
        for (Player p_iter : world.getPlayers()) {
            // if it's the command instigating player - don't move, create end portal surrounding
            if (p_iter == p) {
                Location endPortalLocation = new Location(world, playerLocation.getX() - 2.0, playerLocation.getY() - 1.0, playerLocation.getZ() - 2.0);
                StructureBlockLibApi.INSTANCE
                        .loadStructure(plugin)
                        .at(endPortalLocation)
                        .loadFromPath(plugin.arena_portal_path);
            } else { // otherwise teleport to random location within arena and create cage
                // calculate new position
                int x = rand.nextInt(half) - half;
                int z = rand.nextInt(half) - half;

                // move to new position
                newPlayerLocation = new Location(world, newPlayerLocation.blockX() + x, newPlayerLocation.blockY(), newPlayerLocation.blockZ() + z);

                // find location of highest block on new location
                Location highestBlock = world.getHighestBlockAt(newPlayerLocation).getLocation();

                // move out of the ground and into center of block
                newPlayerLocation.set(highestBlock.getX() + 0.5, highestBlock.getY() + 1.0, highestBlock.getZ() + 0.5);
                p_iter.teleport(newPlayerLocation);
            }

            // all the other stuff
            p_iter.getInventory().clear();
            p_iter.setHealth(20.0);
            p_iter.setFoodLevel(20);
            p_iter.setNoDamageTicks(200); // invulnerable for 10 seconds

            // cant break blocks
            p_iter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 255)); // cant break blocks

            // barrier cage to stop them moving during countdown
            createPlayerCage(world, newPlayerLocation, Material.BARRIER);
        }

        // start countdown timer
        Location finalNewPlayerLocation = newPlayerLocation;
        timer = new CountdownTimer(this.plugin,
                5,
                () -> {
                    p.getServer().showTitle(Title.title(Component.text("§EGo!"), Component.text("")));
                    isCountingDown = false;
                    createPlayerCage(world, finalNewPlayerLocation, Material.AIR);
                    world.getPlayers().forEach(p_iter -> p_iter.playSound(p_iter.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.0f));
                },
                (t) -> {
                    p.getServer().showTitle(Title.title(Component.text("§EGet Ready!"), Component.text("§EStarting in " + t.getSecondsLeft())));
                    world.getPlayers().forEach(p_iter -> p_iter.playSound(p_iter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
                }
        );

        isCountingDown = true;
        timer.scheduleTimer();

        return true;
    }

    public boolean cancelArena(Player p) {
        World world = p.getWorld();
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(world.getSpawnLocation());
        worldBorder.setSize(5.9999968E7);

        if (isCountingDown) {
            world.getPlayers().forEach(p_iter -> createPlayerCage(world, p.getLocation(), Material.AIR));
            isCountingDown = false;
            timer.cancelTask();
        }

        plugin.setPlaying(false);
        p.getServer().sendMessage(Component.text("§CCancelling arena!"));

        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player p) {

            String action;

            try {
                action = args[0];

            } catch (ArrayIndexOutOfBoundsException e) {
                p.sendMessage(Component.text("§CMissing required argument: §Eaction. (start/cancel)"));
                return true;
            }

            switch (action.toLowerCase().strip()) {
                case "start":
                    return startArena(p, args);
                case "cancel":
                    return cancelArena(p);
                default:
                    p.sendMessage(Component.text("§CArgument 1 must be either §Estart §Cor §Ecancel"));
                    return true;
            }
        }
        return true;
    }
}

