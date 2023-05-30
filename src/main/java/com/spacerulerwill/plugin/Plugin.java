package com.spacerulerwill.plugin;

import com.github.shynixn.structureblocklib.api.bukkit.StructureBlockLibApi;
import com.spacerulerwill.plugin.commands.ArenaCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;

public final class Plugin extends JavaPlugin implements Listener {


    // is arena mode being played
    private boolean isPlaying = false;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public Path arena_portal_path;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getServer().getPluginManager().registerEvents(this, this);

        arena_portal_path = getDataFolder().toPath().resolve("arena_portal.nbt");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
