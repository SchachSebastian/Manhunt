package manhunt;

import main.PluginInitializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ManhuntCommands implements CommandExecutor {
    private static final ManhuntCommands instance = new ManhuntCommands();
    private ManhuntCommands() {
    }
    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command,
                             @Nonnull String s, @Nonnull String[] args) {
        switch (command.getName()) {
            case "runner" -> runner(commandSender, args);
            case "addHunter" -> addHunter(commandSender, args);
            case "addRunner" -> addRunner(commandSender, args);
            case "removePlayer" -> removePlayer(commandSender, args);
            case "compass" -> compass(commandSender, args);
            case "stopManhunt" -> stopManhunt(commandSender, args);
            case "updateRate" -> updateRate(commandSender, args);
            default -> {
                commandSender.sendMessage(
                        "§cYou don't have the permission to execute this command!");
                return false;
            }
        }
        return true;
    }
    public static ManhuntCommands getInstance() {
        return instance;
    }
    private void runner(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.runner")) return;
        if (Manhunt.getInstance().isRunning()) Manhunt.getInstance().finish();
        if (args.length <= 0) {
            commandSender.sendMessage("§cSyntax: runner §oplayer1 §c[§oplayer2, ...§c]");
            return;
        }
        ArrayList<Player> runners = new ArrayList<>();
        ArrayList<Player> hunters = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (String name : args) {
            Player runner = Bukkit.getPlayer(name);
            if (runner == null || !runner.isOnline() || runners.contains(runner)) {
                commandSender.sendMessage("§cInvalid player list!");
                return;
            }
            runners.add(runner);
            hunters.remove(runner);
        }
        if (hunters.isEmpty()) {
            commandSender.sendMessage(
                    "§cYou need a minimum of one hunter and one runner to play Manhunt!");
            return;
        }
        Manhunt.getInstance().addHunters(hunters);
        Manhunt.getInstance().addRunners(runners);
        Manhunt.getInstance().start();
    }
    @Helper
    private Player getPlayer(CommandSender commandSender, String[] args) {
        Player ret = null;
        if (args.length == 1) {
            ret = Bukkit.getPlayer(args[0]);
        } else if (args.length == 0 && commandSender instanceof Player player) {
            ret = player;
        }
        return ret;
    }
    private void addHunter(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.addHunter")) return;
        if (isNotRunning(commandSender)) return;
        Player hunter = getPlayer(commandSender, args);
        if (hunter == null) {
            commandSender.sendMessage("§cSyntax: add_hunter [§oplayer§c]");
            return;
        }
        if (isOffline(commandSender, hunter)) return;
        if (Manhunt.getInstance().isHunter(hunter)) {
            commandSender.sendMessage("§cPlayer is already a hunter!");
            return;
        }
        if (Manhunt.getInstance().isRunner(hunter)) {
            commandSender.sendMessage("§cPlayer can't be a hunter! He is already a runner!");
            return;
        }
        Manhunt.getInstance().addHunter(hunter);
    }
    private void addRunner(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.addRunner")) return;
        if (isNotRunning(commandSender)) return;
        Player runner = getPlayer(commandSender, args);
        if (runner == null) {
            commandSender.sendMessage("§cSyntax: add_runner [§oplayer§c]");
            return;
        }
        if (isOffline(commandSender, runner)) return;
        if (Manhunt.getInstance().isRunner(runner)) {
            commandSender.sendMessage("§cPlayer is already a runner!");
            return;
        }
        if (Manhunt.getInstance().isHunter(runner)) {
            commandSender.sendMessage("§cPlayer can't be a runner! He is already a hunter!");
            return;
        }
        Manhunt.getInstance().addRunner(runner);
    }
    private void removePlayer(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.removePlayer")) return;
        if (isNotRunning(commandSender)) return;
        Player player = getPlayer(commandSender, args);
        if (player == null) {
            commandSender.sendMessage("§cSyntax: removePlayer [§oplayer§c]");
            return;
        }
        if (!Manhunt.getInstance().isPlayer(player)) {
            commandSender.sendMessage(
                    "§cYou can not remove a Player that is not a part of this Manhunt!");
            return;
        }
        Manhunt.getInstance().removePlayer(player);
        Bukkit.broadcastMessage(
                "§aPlayer " + player.getDisplayName() + "§a is no longer a part of this manhunt!");
    }
    private void compass(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.compass")) return;
        if (isNotRunning(commandSender)) return;
        if (args.length != 0) {
            commandSender.sendMessage("§cSyntax: compass");
            return;
        }
        if (commandSender instanceof Player player) {
            if (Manhunt.getInstance().isRunner(player)) {
                commandSender.sendMessage("§cYou are the runner! Don't play around, run!");
                return;
            }
            if (!Manhunt.getInstance().isHunter(player)) {
                commandSender.sendMessage(
                        "§cYou are not a part of this Manhunt, use add_hunter to start as Hunter!");
                return;
            }
            Manhunt.getInstance().getHunter(player).giveCompass();
        } else {
            commandSender.sendMessage("§cOnly players are allowed to use this command!");
        }
    }
    @Helper
    private boolean isNotRunning(CommandSender commandSender) {
        if (!Manhunt.getInstance().isRunning()) {
            commandSender.sendMessage(
                    "§cManhunt hasn't started yet! Use the runner command to start a new game!");
        }
        return !Manhunt.getInstance().isRunning();
    }
    @Helper
    private boolean isOffline(CommandSender commandSender, Player player) {
        if (!player.isOnline()) commandSender.sendMessage("§cPlayer must be online!");
        return !player.isOnline();
    }
    private void stopManhunt(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.stopManhunt")) return;
        if (isNotRunning(commandSender)) return;
        if (args.length != 0) {
            commandSender.sendMessage("§cSyntax: stopManhunt");
            return;
        }
        Manhunt.getInstance().finish();
        Bukkit.broadcastMessage("§cManhunt stopped!");
    }
    private void updateRate(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("commands.updateRate")) return;
        if (args.length == 0) {
            commandSender.sendMessage(
                    "§aCurrent updateRate: " + PluginInitializer.getPlugin().getUpdateRate());
            return;
        }
        if (args.length != 1) {
            commandSender.sendMessage("§cSyntax: setUpdateRate §orate");
            return;
        }
        try {
            int rate = Integer.parseInt(args[0]);
            if (rate <= 0 || rate >= 10000) {
                commandSender.sendMessage("§cRate must be between 1 and 9999!");
                return;
            }
            PluginInitializer.getPlugin().setUpdateRate(rate);
            commandSender.sendMessage("§aSet updateRate to: " + rate);
        } catch (NumberFormatException ex) {
            commandSender.sendMessage("§cThe parameter §orate §cmust be an integer!");
        }
    }
}