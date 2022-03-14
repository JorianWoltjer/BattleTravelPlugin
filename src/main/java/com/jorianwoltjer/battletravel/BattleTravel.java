package com.jorianwoltjer.battletravel;

import com.jorianwoltjer.battletravel.command.BattleTravelCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static com.jorianwoltjer.battletravel.command.BattleTravelCommand.bossBar;

public final class BattleTravel extends JavaPlugin {

    public static Plugin plugin;
    public static CompassTracker compassTracker;
    BukkitTask updateTrackers;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("battletravel").setExecutor(new BattleTravelCommand());
        getCommand("battletravel").setTabCompleter(new BattleTravelCommand());
        Bukkit.getPluginManager().registerEvents(new BattleTravelCommand(), this);
        Bukkit.getPluginManager().registerEvents(new CompassTracker(), this);

        compassTracker = new CompassTracker();
        updateTrackers = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() >= 2) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        compassTracker.update(p);
                    }
                }
            }
        }.runTaskTimer(this, 0, 20); // 60t = 3s

        plugin = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        bossBar.removeAll();
    }
}
