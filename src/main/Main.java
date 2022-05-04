package main;

import manhunt.Manhunt;
import manhunt.ManhuntCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import server_management.Welcome;

public class Main extends JavaPlugin {
    private static Main plugin;
    private static int updateRate; // every x ticks
    public void onEnable() {
        plugin = this;
        super.onEnable();
        FileConfiguration fileConfiguration = getPlugin().getConfig();
        Integer updateRate = fileConfiguration.getInt("updateRate");
        if (updateRate != null && updateRate > 0 && updateRate < 10000) {
            this.updateRate = updateRate;
        } else {
            this.updateRate = 10;
        }
        // manhunt
        PluginCommand command;
        if ((command = getCommand("runner")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("add_hunter")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("add_runner")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("manhunt_remove")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("compass")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("heal")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("stop_manhunt")) != null) command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("updaterate")) != null) command.setExecutor(ManhuntCommands.getInstance());
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(Manhunt.getInstance(), this);
        pluginManager.registerEvents(Welcome.getInstance(), this);
    }
    public static Main getPlugin() {
        return plugin;
    }
    public int getUpdateRate() {
        return updateRate;
    }
    public void setUpdateRate(int updateRate) {
        FileConfiguration fileConfiguration = this.getConfig();
        fileConfiguration.set("updateRate", updateRate);
        this.updateRate = updateRate;
        plugin.saveConfig();
    }
}