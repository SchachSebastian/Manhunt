package manhunt;

import main.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class Manhunt implements Listener {
    // attributes
    private final static Manhunt instance = new Manhunt();
    private final ArrayList<TrackingCompass> compasses;
    private final ArrayList<Player> deathRunners;
    private final ArrayList<Player> hunters;
    private boolean isRunning; // indicates if manhunt is currently running
    private final ArrayList<ManhuntPlayer> runners;
    // instance of Manhunt
    public static Manhunt getInstance() {
        return instance;
    }
    // is running
    public boolean isRunning() {
        return isRunning;
    }
    // extra command for bad players
    public static void heal(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
    }
    // compass menu
    @EventHandler
    public void CompassInteractEvent(PlayerInteractEvent event) {
        if (isRunning) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack item = event.getItem();
                if (item != null) {
                    if (item.getType() == Material.COMPASS) {
                        for (TrackingCompass compass : compasses) {
                            if (compass.getCompass().equals(event.getItem()) && runners.size() > 1) {
                                Player player = event.getPlayer();
                                player.openInventory(compass.getCompassMenu().getCompassMenu());
                            }
                        }
                    }
                }
            }
        }
    }
    public void addHunter(Player hunter) {
        this.hunters.add(hunter);
        if (isRunning) {
            hunter.sendMessage("You are selected as §4Hunter§r, kill the prey!");
            configHunter(hunter);
        }
    }
    public void addHunters(ArrayList<Player> hunters) {
        if (!isRunning) this.hunters.addAll(hunters);
    }
    // add/remove players
    public void addRunner(Player runner) {
        this.runners.add(new ManhuntPlayer(runner));
        if (isRunning) {
            runner.sendMessage("You are selected as §2Runner§r, run!!!!");
            configRunner(runner);
            updateCompassMenu();
        }
    }
    // set players
    public void addRunners(ArrayList<Player> runners) {
        if (!isRunning) {
            for (Player runner : runners) {
                this.runners.add(new ManhuntPlayer(runner));
            }
        }
    }
    @EventHandler
    public void antiCheat(PlayerCommandPreprocessEvent event) {
        if (isRunning) {
            if (!event.getPlayer().getUniqueId().equals(UUID.fromString("71f71202-7089-4017-8209-193dd6fa1003"))) {
                String command = "";
                if (event.getMessage().length() > 2 && event.getMessage().indexOf('/') == 0) {
                    command = event.getMessage().substring(1, event.getMessage().indexOf(' ') + 1 > 0 ?
                            event.getMessage().indexOf(' ') + 1 : event.getMessage().length());
                }
                PluginCommand pluginCommand = Bukkit.getPluginCommand(command);
                CommandExecutor commandExecutor = null;
                if (pluginCommand != null) {
                    commandExecutor = pluginCommand.getExecutor();
                }
                if (commandExecutor == null || !commandExecutor.equals(ManhuntCommands.getInstance())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cDo not abuse commands!");
                }
            } else if (event.getMessage().indexOf("status") == 1) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Manhunt Status:");
                event.getPlayer().sendMessage("------------------------------");
                event.getPlayer().sendMessage("Running: " + isRunning);
                event.getPlayer().sendMessage("Update rate: " + Main.getPlugin().getUpdateRate());
                event.getPlayer().sendMessage("Runners [" + runners.size() + "]: ");
                for (ManhuntPlayer manhuntPlayer : runners) {
                    event.getPlayer().sendMessage("   " + manhuntPlayer.getPlayer().getName());
                }
                event.getPlayer().sendMessage("Death runners [" + deathRunners.size() + "]: ");
                for (Player runner : deathRunners) {
                    event.getPlayer().sendMessage("   " + runner.getName());
                }
                event.getPlayer().sendMessage("Hunters [" + hunters.size() + "]: ");
                for (Player hunter : hunters) {
                    event.getPlayer().sendMessage("   " + hunter.getName());
                }
                event.getPlayer().sendMessage("Compasses [" + compasses.size() + "]: ");
                for (TrackingCompass compass : compasses) {
                    event.getPlayer().sendMessage("   Owner: " + compass.getOwner().getName() +
                            ", Tracking: " + compass.getPointingTo().getPlayer().getName());
                }
                event.getPlayer().sendMessage("------------------------------");
            }
        }
    }
    // anti_cheating
    @EventHandler
    public void antiGameMode(PlayerGameModeChangeEvent event) {
        if (isRunning) {
            if (!event.getPlayer().getUniqueId().equals(UUID.fromString("71f71202-7089-4017-8209-193dd6fa1003")) &&
                    event.getNewGameMode() != GameMode.SURVIVAL && !(deathRunners.contains(event.getPlayer()) && event.getNewGameMode() ==
                    GameMode.SPECTATOR)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cDo not abuse gameMode!");
            }
        }
    }
    // Events
    @EventHandler
    public void compassDropEvent(PlayerDropItemEvent event) {
        if (isRunning) {
            ItemStack item = event.getItemDrop().getItemStack();
            TrackingCompass compass = null;
            for (TrackingCompass c : compasses) {
                if (item.getItemMeta().getDisplayName().equals(c.getCompass().getItemMeta().getDisplayName())) {
                    compass = c;
                }
            }
            if (compass != null) {
                compass.setTracking(false);
                compasses.remove(compass);
                event.getItemDrop().remove();
            }
        }
    }
    @EventHandler
    public void compassMenuClickEvent(InventoryClickEvent event) {
        if (isRunning) {
            if (event.getWhoClicked() instanceof Player player) {
                TrackingCompass compass = null;
                for (TrackingCompass c : compasses) {
                    if (c.getOwner().equals(player)) {
                        compass = c;
                        break;
                    }
                }
                if (compass != null) {
                    if (event.getInventory().equals(compass.getCompassMenu().getCompassMenu())) {
                        event.setCancelled(true);
                        ManhuntPlayer tracked = compass.getCompassMenu().getClickedPlayer(event.getCurrentItem());
                        if (tracked != null) {
                            compass.setPointingTo(tracked);
                            player.closeInventory();
                            compass.getCompassMenu().resetPage();
                        } else {
                            compass.getCompassMenu().updateCompassMenu();
                            player.updateInventory();
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void enderDragonKilledEvent(EntityDeathEvent event) {
        if (isRunning) {
            if (event.getEntity() instanceof EnderDragon) {
                Bukkit.broadcastMessage("§2Runners won!");
                finish();
            }
        }
    }
    // finish manhunt and reset all to start
    public void finish() {
        for (TrackingCompass compass : compasses) {
            compass.setTracking(false);
            compass.getOwner().getInventory().remove(compass.getCompass());
            compass.getOwner().updateInventory();
        }
        this.compasses.clear();
        for (Player player : hunters) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setGameMode(GameMode.SURVIVAL);
        }
        this.hunters.clear();
        for (ManhuntPlayer manhuntPlayer : runners) {
            manhuntPlayer.setTracked(false);
            Player player = manhuntPlayer.getPlayer();
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setGameMode(GameMode.SURVIVAL);
        }
        this.runners.clear();
        for (Player player : deathRunners) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setGameMode(GameMode.SURVIVAL);
        }
        this.deathRunners.clear();
        isRunning = false;
    }
    // give compass
    public void giveHunterCompass(Player hunter) {
        if (isHunter(hunter) && !hasCompass(hunter)) {
            TrackingCompass compass = new TrackingCompass(hunter, runners.get(0), new CompassMenu(this.runners));
            compasses.add(compass);
            //
            if (hunter.getInventory().firstEmpty() != -1) {
                hunter.getInventory().addItem(compass.getCompass());
                ItemStack compassItem = compass.getCompass();
                for (ItemStack itemStack : compass.getOwner().getInventory().getContents()) {
                    if (itemStack != null) {
                        if (itemStack.isSimilar(compass.getCompass())) {
                            compassItem = itemStack;
                        }
                    }
                }
                compass.setCompass(compassItem);
            } else {
                hunter.sendMessage("§cYour inventory is full! Free some space and use the command again!");
            }
        }
    }
    // has compass
    public boolean hasCompass(Player hunter) {
        boolean ret = false;
        for (TrackingCompass compass : compasses) {
            if (compass.getOwner().equals(hunter)) {
                ret = true;
                break;
            }
        }
        return ret;
    }
    public int indexOfRunner(Player runner) {
        int index = -1;
        for (int i = 0; i < runners.size(); i++) {
            if (runners.get(i).getPlayer().equals(runner)) index = i;
        }
        return index;
    }
    // is Player hunter/runner/manhuntPlayer
    public boolean isHunter(Player hunter) {
        boolean ret = false;
        for (Player player : hunters) {
            if (player.getUniqueId().equals(hunter.getUniqueId())) {
                ret = true;
            }
        }
        return ret;
    }
    public boolean isPlayer(Player player) {
        return isRunner(player) || isHunter(player);
    }
    public boolean isRunner(Player runner) {
        return indexOfRunner(runner) >= 0;
    }
    @EventHandler
    public void playerDiedEvent(EntityDeathEvent event) {
        if (isRunning) {
            if (event.getEntity() instanceof Player deathPlayer) {
                if (isHunter(deathPlayer)) {
                    TrackingCompass compass = null;
                    for (TrackingCompass c : compasses) {
                        if (c.getOwner().equals(deathPlayer)) {
                            compass = c;
                        }
                    }
                    if (compass != null) {
                        if (event.getDrops().remove(compass.getCompass())) {
                            compass.setTracking(false);
                            compasses.remove(compass);
                        }
                    }
                } else if (isRunner(deathPlayer)) {
                    runnerDeath(deathPlayer);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoinedEvent(PlayerJoinEvent event) {
        if (isRunning) {
            Player loginPlayer = event.getPlayer();
            if (isHunter(loginPlayer)) {
                replaceHunter(loginPlayer);
                loginPlayer.sendMessage("§4Continue hunting!");
            } else if (isRunner(loginPlayer)) {
                replaceRunner(loginPlayer);
                loginPlayer.sendMessage("§2Continue running, fast!");
            }
        }
    }
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (isRunning) {
            Player quitPlayer = event.getPlayer();
            if (isHunter(quitPlayer)) {
                Bukkit.broadcastMessage("Hunter §4" + quitPlayer.getName() + "§r is offline!");
                TrackingCompass compass = null;
                for (TrackingCompass c : compasses) {
                    if (c.getOwner().equals(quitPlayer)) {
                        compass = c;
                    }
                }
                if (compass != null) {
                    compass.setTracking(false);
                    quitPlayer.getInventory().remove(compass.getCompass());
                    quitPlayer.updateInventory();
                    compasses.remove(compass);
                }
            } else if (isRunner(quitPlayer)) {
                Bukkit.broadcastMessage("Runner §2" + quitPlayer.getName() + "§r is offline!");
            }
        }
    }
    @EventHandler
    public void playerRespawnEvent(PlayerRespawnEvent event) {
        if (isRunning) {
            Player respawnedPlayer = event.getPlayer();
            if (isHunter(respawnedPlayer)) {
                configHunter(respawnedPlayer);
                respawnedPlayer.sendMessage("§4Hurry up, the runner is already far away!");
            }
        }
    }
    public boolean removeHunter(Player hunter) {
        TrackingCompass compass = null;
        for (TrackingCompass c : compasses) {
            if (c.getOwner().equals(hunter)) {
                compass = c;
            }
        }
        Bukkit.broadcastMessage(compass + "");
        if (compass != null) {
            compass.setTracking(false);
            hunter.getInventory().remove(compass.getCompass());
            hunter.updateInventory();
            compasses.remove(compass);
        }
        hunter.setDisplayName(hunter.getName());
        hunter.setPlayerListName(hunter.getName());
        hunter.setGameMode(GameMode.SURVIVAL);
        return this.hunters.remove(hunter);
    }
    public boolean removePlayer(Player player) {
        if (isRunner(player)) return removeRunner(player);
        if (isHunter(player)) return removeHunter(player);
        return false;
    }
    public boolean removeRunner(Player runner) {
        int index = indexOfRunner(runner);
        ManhuntPlayer manhuntPlayer = null;
        if (index >= 0) {
            manhuntPlayer = this.runners.get(index);
            manhuntPlayer.setTracked(false);
            Player player = manhuntPlayer.getPlayer();
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setGameMode(GameMode.SURVIVAL);
        }
        return this.runners.remove(manhuntPlayer);
    }
    // start manhunt, at least one runner needs to be known at this point
    public void start() {
        isRunning = true;
        for (ManhuntPlayer manhuntRunner : runners) {
            Player runner = manhuntRunner.getPlayer();
            runner.sendMessage("You are selected as §2Runner§r, run!");
            configRunner(runner);
            Manhunt.heal(runner);
        }
        for (Player hunter : hunters) {
            hunter.sendMessage("You are selected as §4Hunter§r, kill the prey!");
            if (hunter.getInventory().firstEmpty() == -1) {
                hunter.sendMessage("§cYour inventory is full! Free some space and use /compass to get a compass!");
            }
            configHunter(hunter);
            Manhunt.heal(hunter);
        }
        updateCompassMenu();
    }
    private void configHunter(Player hunter) {
        hunter.setDisplayName("§4Hunter §r" + hunter.getName());
        hunter.setPlayerListName("§4Hunter §r" + hunter.getName());
        hunter.setGameMode(GameMode.SURVIVAL);
        giveHunterCompass(hunter);
    }
    // config Players
    private void configRunner(Player runner) {
        runner.setDisplayName("§2Runner §r" + runner.getName());
        runner.setPlayerListName("§2Runner §r" + runner.getName());
        runner.setGameMode(GameMode.SURVIVAL);
    }
    private void replaceHunter(Player newHunter) {
        Player oldHunter = null;
        for (Player hunter : hunters) {
            if (newHunter.getUniqueId().equals(hunter.getUniqueId())) {
                oldHunter = hunter;
                break;
            }
        }
        if (oldHunter != null) {
            hunters.remove(oldHunter);
            hunters.add(newHunter);
            configHunter(newHunter);
            for (TrackingCompass compass : compasses) {
                if (compass.getOwner().equals(oldHunter)) {
                    compass.setOwner(newHunter);
                }
            }
        }
    }
    // some stuff to do because of the bukkit api
    private void replaceRunner(Player newRunner) {
        int index = runners.indexOf(newRunner);
        if (index >= 0) {
            runners.get(index).setPlayer(newRunner);
        }
    }
    // runner death
    private void runnerDeath(Player deathRunner) {
        deathRunners.add(deathRunner);
        int index = indexOfRunner(deathRunner);
        if (index >= 0) {
            runners.remove(index);
            deathRunner.sendMessage("§4It's over! The hunters caught you!");
            deathRunner.setGameMode(GameMode.SPECTATOR);
            updateCompassMenu();
            if (runners.size() == 0) {
                Bukkit.broadcastMessage("§2Hunters won!");
                finish();
            }
        }
    }
    // update compass menu
    private void updateCompassMenu() {
        for (TrackingCompass compass : compasses) {
            compass.getCompassMenu().updateCompassMenu();
        }
    }
    // constructor
    private Manhunt() {
        this.compasses = new ArrayList<>();
        this.hunters = new ArrayList<>();
        this.deathRunners = new ArrayList<>();
        this.runners = new ArrayList<>();
        this.isRunning = false;
    }
}