package manhunt;

import main.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class ManhuntCommands implements CommandExecutor {
    private static final ManhuntCommands instance = new ManhuntCommands();
    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String s, @Nonnull String[] args) {
        Manhunt manhunt = Manhunt.getInstance();
        if (command.getName().equals("runner") && commandSender.hasPermission("commands.runner")) {
            manhunt.finish();
            if (args.length > 0) {
                ArrayList<Player> runners = new ArrayList<>();
                ArrayList<Player> hunters = new ArrayList<>(Bukkit.getOnlinePlayers());
                int invalidPlayers = 0;
                for (String name : args) {
                    Player runner = Bukkit.getPlayer(name);
                    if (runner != null) {
                        if (runner.isOnline()) {
                            if (!runners.contains(runner)) {
                                runners.add(runner);
                                hunters.remove(runner);
                            }
                        } else {
                            commandSender.sendMessage("§cPlayer must be online!");
                        }
                    } else {
                        invalidPlayers++;
                    }
                }
                // start manhunt
                if (invalidPlayers > 1) {
                    commandSender.sendMessage("§cInvalid players!");
                } else if (invalidPlayers > 0) {
                    commandSender.sendMessage("§cInvalid player!");
                } else if (!hunters.isEmpty()) {
                    manhunt.addHunters(hunters);
                    manhunt.addRunners(runners);
                    manhunt.start();
                } else {
                    commandSender.sendMessage("§cYou need a minimum of one hunter and one runner to play Manhunt!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: runner §oplayer1 §c[§oplayer2, ...§c]");
            }
        } else if (command.getName().equals("add_hunter")) {
            if (!manhunt.isRunning()) {
                commandSender.sendMessage("§cManhunt hasn't started yet! Use the runner command to start a new game!");
            } else if (args.length == 1 && commandSender.hasPermission("commands.add_hunter")) {
                Player hunter = Bukkit.getPlayer(args[0]);
                if (hunter != null) {
                    if (hunter.isOnline()) {
                        if (manhunt.isHunter(hunter)) {
                            commandSender.sendMessage("§cPlayer is already a hunter!");
                        } else if (manhunt.isRunner(hunter)) {
                            commandSender.sendMessage("§cPlayer can't be a hunter! He is already a runner!");
                        } else {
                            manhunt.addHunter(hunter);
                        }
                    } else {
                        commandSender.sendMessage("§cPlayer must be online!");
                    }
                } else {
                    commandSender.sendMessage("§cInvalid Player!");
                }
            } else if (args.length == 0) {
                if (commandSender instanceof Player hunter) {
                    if (manhunt.isHunter(hunter)) {
                        commandSender.sendMessage("§cYou are already a hunter!");
                    } else if (manhunt.isRunner(hunter)) {
                        commandSender.sendMessage("§cYou can't be a hunter! You are already a runner!");
                    } else {
                        manhunt.addHunter(hunter);
                    }
                } else {
                    commandSender.sendMessage("§cOnly players are allowed to use this command!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: add_hunter [§oplayer§c]");
            }
        } else if (command.getName().equals("add_runner")) {
            if (!manhunt.isRunning()) {
                commandSender.sendMessage("§cManhunt hasn't started yet! Use the runner command to start a new game!");
            } else if (args.length == 1 && commandSender.hasPermission("commands.add_runner")) {
                Player runner = Bukkit.getPlayer(args[0]);
                if (runner != null) {
                    if (runner.isOnline()) {
                        if (manhunt.isRunner(runner)) {
                            commandSender.sendMessage("§cPlayer is already a runner!");
                        } else if (manhunt.isHunter(runner)) {
                            commandSender.sendMessage("§cPlayer can't be a runner! He is already a hunter!");
                        } else {
                            manhunt.addRunner(runner);
                        }
                    } else {
                        commandSender.sendMessage("§cPlayer must be online!");
                    }
                } else {
                    commandSender.sendMessage("§cInvalid Player!");
                }
            } else if (args.length == 0) {
                if (commandSender instanceof Player runner) {
                    if (manhunt.isRunner(runner)) {
                        commandSender.sendMessage("§cYou are already a runner!");
                    } else if (manhunt.isHunter(runner)) {
                        commandSender.sendMessage("§cYou can't be a runner! You are already a hunter!");
                    } else {
                        manhunt.addRunner(runner);
                    }
                } else {
                    commandSender.sendMessage("§cOnly players are allowed to use this command!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: add_runner [§oplayer§c]");
            }
        } else if (command.getName().equals("manhunt_remove")) { //
            if (!manhunt.isRunning()) {
                commandSender.sendMessage("§cManhunt hasn't started yet! Use the runner command to start a new game!");
            } else if (args.length == 1 && commandSender.hasPermission("commands.manhunt_remove")) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player != null) {
                    if (player.isOnline()) {
                        if (manhunt.isPlayer(player)) {
                            manhunt.removePlayer(player);
                            Bukkit.broadcastMessage("§aPlayer " + player.getDisplayName() + "§a is no longer a part of this manhunt!");
                        } else {
                            commandSender.sendMessage("§cYou can not remove a Player that is not a part of this Manhunt!");
                        }
                    } else {
                        commandSender.sendMessage("§cPlayer must be online!");
                    }
                } else {
                    commandSender.sendMessage("§cInvalid Player!");
                }
            } else if (args.length == 0) {
                if (commandSender instanceof Player player) {
                    if (manhunt.isPlayer(player)) {
                        manhunt.removePlayer(player);
                        Bukkit.broadcastMessage("§aPlayer " + player.getDisplayName() + "§a is no longer a part of this manhunt!");
                    } else {
                        commandSender.sendMessage("§cYou can not remove yourself, because you are not a part of this Manhunt!");
                    }
                } else {
                    commandSender.sendMessage("§cOnly players are allowed to use this command!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: add_runner [§oplayer§c]");
            }
        } else if (command.getName().equals("compass")) {
            if (commandSender instanceof Player player) {
                if (manhunt.isHunter(player)) {
                    if (!manhunt.hasCompass(player)) {
                        if (player.getInventory().firstEmpty() != -1) {
                            manhunt.giveHunterCompass(player);
                        } else {
                            commandSender.sendMessage("§cYour inventory is full! Free some space and use the command again!");
                        }
                    } else {
                        commandSender.sendMessage("§cYou have already a compass, use it!");
                    }
                } else if (manhunt.isRunner(player)) {
                    commandSender.sendMessage("§cYou are the runner! Don't play around, run!");
                } else {
                    commandSender.sendMessage("§cYou are not a part of this Manhunt, use add_hunter to start as Hunter!");
                }
            } else {
                commandSender.sendMessage("§cOnly players are allowed to use this command!");
            }
        } else if (command.getName().equals("stop_manhunt") && commandSender.hasPermission("commands.stop_manhunt")) {
            if (args.length == 0) {
                if (manhunt.isRunning()) {
                    manhunt.finish();
                    Bukkit.broadcastMessage("§cManhunt stopped!");
                } else {
                    commandSender.sendMessage("§cYou can't stop a Manhunt that isn't running!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: stopManhunt");
            }
        } else if (command.getName().equals("heal")) {
            if (args.length <= 1) {
                if (commandSender instanceof Player player) {
                    if (args.length == 1) player = Bukkit.getPlayer(args[0]);
                    if (player != null) {
                        if (player.isOnline()) {
                            Manhunt.heal(player);
                            Bukkit.broadcastMessage("Player §a" + player.getDisplayName() + "§r was healed!");
                        } else {
                            commandSender.sendMessage("§cPlayer must be online!");
                        }
                    } else {
                        commandSender.sendMessage("§cInvalid Player!");
                    }
                } else {
                    commandSender.sendMessage("§cOnly players are allowed to use this command!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: heal [§oplayer§c]");
            }
        } else if (command.getName().equals("updaterate")) {
            if (args.length == 0) {
                commandSender.sendMessage("§aCurrent updateRate: " + Main.getPlugin().getUpdateRate());
            } else if (args.length == 1) {
                try {
                    int rate = Integer.parseInt(args[0]);
                    if (rate > 0 && rate < 10000) {
                        Main.getPlugin().setUpdateRate(rate);
                        commandSender.sendMessage("§aSet updateRate to: " + rate);
                    } else {
                        commandSender.sendMessage("§cRate must be between 1 and 9999!");
                    }
                } catch (NumberFormatException ex) {
                    commandSender.sendMessage("§cThe parameter §orate §cmust be an integer!");
                }
            } else {
                commandSender.sendMessage("§cSyntax: setUpdateRate §orate");
            }
        } else {
            commandSender.sendMessage("§cYou don't have the permission to execute this command!");
        }
        return true;
    }
    public static ManhuntCommands getInstance() {
        return instance;
    }
    // constructor
    private ManhuntCommands() {
    }
}