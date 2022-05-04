package server_management;

import main.Main;
import manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Welcome implements Listener {
    private static Welcome instance = new Welcome();
    private ArrayList<Role> playerRoles;
    private ArrayList<Player> players;
    private ItemStack selectRoleItem;
    private int taskID;
    private int timeUntilStart = 60;
    public static Welcome getInstance() {
        return instance;
    }
    @EventHandler
    public void itemInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null) {
                if (item.isSimilar(selectRoleItem)) {
                    event.getPlayer().openInventory(SelectRoleInventory.getInventory());
                }
            }
        }
    }
    @EventHandler
    public void itemMenuClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (event.getInventory().equals(SelectRoleInventory.getInventory())) {
                Role role = SelectRoleInventory.getRole(event.getCurrentItem());
                if (role != null) {
                    int index = players.indexOf(player);
                    if (playerRoles.get(index).equals(Role.RUNNER) && runnerCount() <= 1) {
                        player.sendMessage("Can't change role to RUNNER!");
                    } else if (playerRoles.get(index).equals(Role.HUNTER) && hunterCount() <= 1) {
                        player.sendMessage("Can't change role to HUNTER!");
                    } else {
                        playerRoles.set(index, role);
                        player.sendMessage("Set role to " + role.name());
                    }
                    player.closeInventory();
                }
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void playerJoinedEvent(PlayerJoinEvent event) {
        if (Manhunt.getInstance().isRunning()) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        } else {
            event.setJoinMessage("Player " + event.getPlayer().getName() + " joined the game!");
            players.add(event.getPlayer());
            if (players.size() == 1) {
                playerRoles.add(Role.RUNNER);
            } else {
                playerRoles.add(Role.HUNTER);
            }
            event.getPlayer().getInventory().addItem(selectRoleItem);
            if (players.size() >= 2) {
                startCounter();
            }
        }
    }
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage("Player " + event.getPlayer().getName() + " left the game!");
    }
    private int hunterCount() {
        int count = 0;
        for (Role role : playerRoles) {
            if (role.equals(Role.HUNTER)) count++;
        }
        return count;
    }
    private int runnerCount() {
        int count = 0;
        for (Role role : playerRoles) {
            if (role.equals(Role.RUNNER)) count++;
        }
        return count;
    }
    private void startCounter() {
        Bukkit.broadcastMessage("60 seconds left until start!");
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            if (players.size() < 2) {
                Bukkit.broadcastMessage("Start cancelled!");
                timeUntilStart = 60;
                Bukkit.getScheduler().cancelTask(taskID);
            } else if (timeUntilStart == 30) {
                Bukkit.broadcastMessage("30 seconds left until start!");
            } else if (timeUntilStart == 15) {
                Bukkit.broadcastMessage("15 seconds left until start!");
            } else if (timeUntilStart <= 10) {
                Bukkit.broadcastMessage(timeUntilStart + " seconds left until start!");
            }
            timeUntilStart--;
            if (timeUntilStart <= 0) {
                startManhunt();
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 0, 20);
    }
    private void startManhunt() {
        Manhunt manhunt = Manhunt.getInstance();
        manhunt.finish();
        for (int i = 0; i < players.size(); i++) {
            if (playerRoles.get(i).equals(Role.RUNNER)) {
                manhunt.addRunner(players.get(i));
            } else {
                manhunt.addHunter(players.get(i));
            }
        }
        manhunt.start();
    }
    private Welcome() {
        players = new ArrayList<>();
        playerRoles = new ArrayList<>();
        selectRoleItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = selectRoleItem.getItemMeta();
        meta.setDisplayName("Select role:");
        meta.addEnchant(Enchantment.MENDING, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        selectRoleItem.setItemMeta(meta);
    }
}