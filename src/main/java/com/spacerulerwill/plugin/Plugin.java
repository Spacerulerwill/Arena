package com.spacerulerwill.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public final class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void tryCreateCageBlock(World world, Location location, Material material){
        Block block = world.getBlockAt(location);
        if (block.isEmpty() || block.isLiquid() || !block.isSolid() || block.isPassable() || block.getType() == Material.BARRIER){
            block.setType(material);
        }
    }

    public void createPlayerCage(World world, Location location, Material material) {
        for (int i = 0; i < 2; i++){
            Location left = new Location(world, location.getX() - 1.0, location.getY() + i, location.getZ() );
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (command.getName().equalsIgnoreCase("arena")) {
            if (sender instanceof Player p) {

                // command argument handling
                int borderSize;

                try {
                    borderSize = Integer.parseInt(args[0]);
                }
                catch (ArrayIndexOutOfBoundsException e){
                    borderSize = 150;
                }
                catch (NumberFormatException e) {
                    TextComponent component = Component.text("§CArgument §EborderSize §Cmust be an integer value");
                    p.sendMessage(component);

                    return true;
                }

                World world = p.getWorld();
                Random rand = new Random();

                // set world border to radius
                WorldBorder worldBorder = world.getWorldBorder();
                worldBorder.setCenter(p.getLocation());
                worldBorder.setSize(borderSize);

                // set to daytime
                world.setTime(6000);

                Location newPlayerLocation = p.getLocation();

                // teleport players to random location inside world border
                int half = borderSize / 2;
                for (Player p_iter : world.getPlayers()) {

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

                    // all the other stuff
                    p_iter.getInventory().clear();
                    p_iter.setHealth(20.0);
                    p_iter.setFoodLevel(20);
                    p_iter.setNoDamageTicks(200); // invulnerable for 10 seconds

                    p_iter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 100, 255)); // cant break blocks
                    createPlayerCage(world, newPlayerLocation, Material.BARRIER);
                }

                // start countdown timer
                Location finalNewPlayerLocation = newPlayerLocation;
                CountdownTimer timer = new CountdownTimer(this,
                        5,
                        () -> {
                            p.getServer().showTitle(Title.title(Component.text("§EGo!"), Component.text("")));
                            createPlayerCage(world, finalNewPlayerLocation, Material.AIR);
                            world.getPlayers().forEach(p_iter -> p_iter.playSound(p_iter.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.0f));
                        },
                        (t) -> {
                            p.getServer().showTitle(Title.title(Component.text("§EGet Ready!"), Component.text("§EStarting in " + t.getSecondsLeft())));
                            world.getPlayers().forEach(p_iter -> p_iter.playSound(p_iter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f));
                        }
                );

                timer.scheduleTimer();
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
