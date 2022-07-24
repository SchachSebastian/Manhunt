package manhunt;

import manhunt.compass.TrackingCompass;
import manhunt.players.Hunter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class ManhuntHandler implements Listener {
    @EventHandler
    public void compassInteractEvent(PlayerInteractEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        Hunter hunter = Manhunt.getInstance().getHunter(event.getPlayer());
        if (hunter != null) hunter.openCompassMenu(item);
    }
    @EventHandler
    public void compassMenuClickEvent(InventoryClickEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack item = event.getCurrentItem();
            if (item == null) return;
            Hunter hunter = Manhunt.getInstance().getHunter(player);
            if (hunter == null ||
                    !hunter.getCompassMenuInventory().equals(event.getClickedInventory())) return;
            hunter.compassMenuClick(item);
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void enderDragonKilledEvent(EntityDeathEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        if (event.getEntity() instanceof EnderDragon) {
            Bukkit.broadcastMessage("§2Runners won!");
            Manhunt.getInstance().finish();
        }
    }
    @EventHandler
    public void playerDiedEvent(EntityDeathEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        if (event.getEntity() instanceof Player deathPlayer) {
            if (!Manhunt.getInstance().isRunner(deathPlayer)) return;
            Manhunt.getInstance().runnerDeath(deathPlayer);
        }
    }
    @EventHandler
    public void playerJoinedEvent(PlayerJoinEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        Player loginPlayer = event.getPlayer();
        if (!Manhunt.getInstance().isPlayer(loginPlayer)) return;
        Manhunt.getInstance().addNonPlayer(loginPlayer);
    }
    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        Player quitPlayer = event.getPlayer();
        if (Manhunt.getInstance().isHunter(quitPlayer)) {
            Bukkit.broadcastMessage("Hunter §4" + quitPlayer.getName() + "§r is offline!");
        } else if (Manhunt.getInstance().isRunner(quitPlayer)) {
            Bukkit.broadcastMessage("Runner §2" + quitPlayer.getName() + "§r is offline!");
        }
    }
    @EventHandler
    public void playerRespawnEvent(PlayerRespawnEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        Player respawnedPlayer = event.getPlayer();
        if (!Manhunt.getInstance().isHunter(respawnedPlayer)) return;
        respawnedPlayer.sendMessage("§4Hurry up, the runner is already far away!");
        if (Boolean.TRUE.equals(
                respawnedPlayer.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) return;
        Manhunt.getInstance().getHunter(respawnedPlayer).giveCompass();
    }
    @EventHandler
    public void compassDropEvent(PlayerDropItemEvent event) {
        if (!Manhunt.getInstance().isRunning()) return;
        ItemStack item = event.getItemDrop().getItemStack();
        if (!TrackingCompass.isCompass(item)) return;
        event.getItemDrop().remove();
    }
}