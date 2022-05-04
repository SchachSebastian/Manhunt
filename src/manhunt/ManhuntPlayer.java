package manhunt;

import main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ManhuntPlayer {
    Player player;
    private Location overWorld, nether, end;
    private int taskID;
    private boolean tracked = true;
    public Location getEnd() {
        return end;
    }
    public Location getNether() {
        return nether;
    }
    public Location getOverWorld() {
        return overWorld;
    }
    public boolean isTracked() {
        return tracked;
    }
    public void setTracked(boolean tracked) {
        if (!this.tracked && tracked) {
            this.tracked = true;
            startTracking();
        } else {
            this.tracked = tracked;
        }
    }
    // method
    private void startTracking() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            if (tracked) {
                if (player.isOnline()) {
                    switch (player.getLocation().getWorld().getEnvironment()) {
                        case NORMAL -> overWorld = player.getLocation();
                        case NETHER -> nether = player.getLocation();
                        case THE_END -> end = player.getLocation();
                        default -> Bukkit.broadcastMessage("Unknown World.Environment!");
                    }
                }
            } else {
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 0, Main.getPlugin().getUpdateRate());
    }
    // override methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(player.getClass() == o.getClass() || getClass() == o.getClass())) return false;
        Player that;
        if (o instanceof ManhuntPlayer) {
            that = ((ManhuntPlayer) o).getPlayer();
        } else {
            that = (Player) o;
        }
        return that.getUniqueId().equals(player.getUniqueId());
    }
    // getter/setter
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    // constructor
    public ManhuntPlayer(Player player) {
        this.player = player;
        startTracking();
    }
}