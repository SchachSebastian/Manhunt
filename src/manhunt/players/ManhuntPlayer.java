package manhunt.players;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ManhuntPlayer {
    protected UUID player;
    // constructor
    public ManhuntPlayer(Player player) {
        this.player = player.getUniqueId();
    }
    public boolean isPlayer(Player player) {
        return this.player.equals(player.getUniqueId());
    }
    public void enable() {
        if (exists()) {
            Player player = getPlayer();
            player.setDisplayName("§7" + player.getName());
            player.setPlayerListName("§7" + player.getName());
            getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }
    public void disable() {
        Player player = getPlayer();
        if (player != null) {
            player.setDisplayName(player.getName());
            player.setPlayerListName(player.getName());
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
    public boolean exists() {
        return getPlayer() != null;
    }
    public World.Environment getEnvironment() {
        if (!exists()) return null;
        Player player = getPlayer();
        return player.getWorld().getEnvironment();
    }
    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }
    public void setPlayer(Player player) {
        this.player = player.getUniqueId();
    }
}