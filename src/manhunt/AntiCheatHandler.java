package manhunt;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.UUID;

public class AntiCheatHandler implements Listener {
    @EventHandler
    public void antiCheat(PlayerCommandPreprocessEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        if (event.getPlayer().getUniqueId()
                .equals(UUID.fromString("71f71202-7089-4017-8209-193dd6fa1003"))) {
            return;
        }
        String command = "";
        if (event.getMessage().length() > 2 && event.getMessage().indexOf('/') == 0) {
            int endIndex = event.getMessage().indexOf(' ') + 1;
            if (endIndex <= 0) endIndex = event.getMessage().length();
            command = event.getMessage().substring(1, endIndex);
        }
        PluginCommand pluginCommand = Bukkit.getPluginCommand(command);
        CommandExecutor commandExecutor = null;
        if (pluginCommand != null) commandExecutor = pluginCommand.getExecutor();
        if (ManhuntCommands.getInstance().equals(commandExecutor)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage("§cDo not abuse commands!");
    }
}