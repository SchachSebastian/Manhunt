package main;

import manhunt.HealCommand;
import manhunt.Manhunt;
import manhunt.ManhuntCommands;
import manhunt.ManhuntHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginInitializer extends JavaPlugin {
    private static PluginInitializer plugin;
    private int updateRate; // every x ticks
    @Override
    public void onDisable() {
        super.onDisable();
        Manhunt.getInstance().finish();
    }
    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        FileConfiguration fileConfiguration = getPlugin().getConfig();
        int updateRate = fileConfiguration.getInt("updateRate");
        if (updateRate > 0 && updateRate < 10000) {
            this.updateRate = updateRate;
        } else {
            this.updateRate = 10;
        }
        // manhunt
        PluginCommand command;
        if ((command = getCommand("runner")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("addHunter")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("addRunner")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("removePlayer")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("compass")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("heal")) != null) command.setExecutor(new HealCommand());
        if ((command = getCommand("stopManhunt")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        if ((command = getCommand("updateRate")) != null)
            command.setExecutor(ManhuntCommands.getInstance());
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ManhuntHandler(), this);
        // only to trigger my brother
        //pluginManager.registerEvents(new AntiCheatHandler(), this);
    }
    public static PluginInitializer getPlugin() {
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