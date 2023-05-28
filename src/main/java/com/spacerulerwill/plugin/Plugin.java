package com.spacerulerwill.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public final class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (command.getName().equalsIgnoreCase("arena")) {
            if (sender instanceof Player p) {
                int borderSize;

                try {
                    borderSize = Integer.valueOf(args[0]);
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
                worldBorder.setSize(Integer.valueOf(borderSize));

                // teleport players to random location inside world border
                // clear inventories
                // give blindness and slowness

                int half = borderSize / 2;
                for (Player p_iter : world.getPlayers()) {
                    int x = rand.nextInt(half) - half;
                    int z = rand.nextInt(half) - half;
                    Location playerLocation = p.getLocation();
                    Location location = new Location(world, playerLocation.blockX() + x + 0.5, playerLocation.blockY(), playerLocation.blockZ() + z + 0.5);
                    location = world.getHighestBlockAt(location).getLocation();
                    p_iter.teleport(location);
                    p_iter.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 500));
                    p_iter.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                    p_iter.getInventory().clear();
                }

                for (Player p_iter : world.getPlayers()) {
                    p_iter.teleport(p.getLocation());
                }
            }
        } else {

        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
