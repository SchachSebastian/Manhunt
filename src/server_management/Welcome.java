package server_management;

import main.PluginInitializer;
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

import java.util.HashMap;
import java.util.Map;

public class Welcome implements Listener {
    private static final Welcome instance = new Welcome();
    private Map<Player, Role> changeRoleOffer;
    private Map<Player, Role> players;
    private final ItemStack selectRoleItem;
    private int taskID;
    private int timeUntilStart = 60;
    public static Welcome getInstance() {
        return instance;
    }
    @EventHandler
    public void itemInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {
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
                    changeRole(player, role);
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
            players.put(event.getPlayer(), players.size() == 1 ? Role.RUNNER : Role.HUNTER);
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
    private void changeRole(Player player, Role newRole) {
        Role currentRole = players.get(player);
        if (currentRole.equals(newRole)) player.sendMessage("You are already a " + currentRole);
        else if (newRole.equals(Role.RUNNER) && runnerCount() > 1) players.replace(player, newRole);
        else if (newRole.equals(Role.HUNTER) && hunterCount() > 1) players.replace(player, newRole);
        else {
            Player otherPlayer = null;
            for (Map.Entry<Player, Role> entry : changeRoleOffer.entrySet()) {
                if (entry.getValue().equals(currentRole)) {
                    otherPlayer = entry.getKey();
                    break;
                }
            }
            if (otherPlayer != null) {
                players.replace(player, newRole);
                players.replace(otherPlayer, currentRole);
                changeRoleOffer.remove(otherPlayer);
                player.sendMessage("Changed Role to " + newRole);
                player.sendMessage("Changed Role to " + currentRole);
            } else {
                changeRoleOffer.put(player, newRole);
                player.sendMessage("Added to waiting list...");
            }
        }
    }
    private int hunterCount() {
        int count = 0;
        for (Role role : players.values()) {
            if (role.equals(Role.HUNTER)) count++;
        }
        return count;
    }
    private int runnerCount() {
        int count = 0;
        for (Role role : players.values()) {
            if (role.equals(Role.RUNNER)) count++;
        }
        return count;
    }
    private void startCounter() {
        Bukkit.broadcastMessage("60 seconds left until start!");
        taskID = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(PluginInitializer.getPlugin(), () -> {
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
        for (Map.Entry<Player, Role> player : players.entrySet()) {
            if (player.getValue().equals(Role.RUNNER)) {
                manhunt.addRunner(player.getKey());
            } else {
                manhunt.addHunter(player.getKey());
            }
        }
        manhunt.start();
    }
    private Welcome() {
        players = new HashMap<>();
        changeRoleOffer = new HashMap<>();
        selectRoleItem = new ItemStack(Material.COMPASS);
        ItemMeta meta = selectRoleItem.getItemMeta();
        meta.setDisplayName("Select role:");
        meta.addEnchant(Enchantment.MENDING, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        selectRoleItem.setItemMeta(meta);
    }
}