package manhunt;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s,
                             String[] args) {
        if (!commandSender.hasPermission("commands.heal") || !command.getName().equals("heal"))
            return false;
        if (args.length > 1) {
            commandSender.sendMessage("§cSyntax: heal [§oplayer§c]");
            return false;
        }
        if (commandSender instanceof Player player) {
            if (args.length == 1) player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                commandSender.sendMessage("§cInvalid Player!");
                return false;
            }
            if (!player.isOnline()) {
                commandSender.sendMessage("§cPlayer must be online!");
                return false;
            }
            heal(player);
            Bukkit.broadcastMessage("Player §a" + player.getDisplayName() + "§r was healed!");
        } else {
            commandSender.sendMessage("§cOnly players are allowed to use this command!");
        }
        return true;
    }
    @Helper
    public static void heal(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) player.setHealth(maxHealth.getBaseValue());
        else player.setHealth(20);
        player.setFoodLevel(20);
    }
}