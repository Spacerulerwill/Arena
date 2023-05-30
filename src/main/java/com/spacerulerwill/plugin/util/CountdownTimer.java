package com.spacerulerwill.plugin.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class CountdownTimer implements Runnable{

    private JavaPlugin plugin;

    private Integer assignedTaskID;

    private int seconds;
    private int secondsLeft;

    // Actions to perform while counting down, before and after
    private Consumer<CountdownTimer> everySecond;
    private Runnable afterTimer;

    // Construct a timer
    public CountdownTimer(JavaPlugin plugin, int seconds,
                          Runnable afterTimer,
                          Consumer<CountdownTimer> everySecond) {
        // Initializing fields
        this.plugin = plugin;

        this.seconds = seconds;
        this.secondsLeft = seconds;

        this.afterTimer = afterTimer;
        this.everySecond = everySecond;
    }
    @Override
    public void run() {
        // Is the timer up?
        if (secondsLeft < 1) {
            // Do what was supposed to happen after the timer
            afterTimer.run();

            // Cancel timer
            if (assignedTaskID != null) cancelTask();
            return;
        }

        // Do what's supposed to happen every second
        everySecond.accept(this);

        // Decrement the seconds left
        secondsLeft--;
    }

    public int getTotalSeconds() {
        return seconds;
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void scheduleTimer() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
    }

    public void cancelTask() {
        Bukkit.getScheduler().cancelTask(assignedTaskID);
    }
}
