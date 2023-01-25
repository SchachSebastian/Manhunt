package manhunt.players;

import main.PluginInitializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Runner extends ManhuntPlayer {
    private Location end;
    private Location nether;
    private Location overWorld;
    private int taskID;
    private boolean tracked = true;
    public Runner(Player player) {
        super(player);
        startTracking();
    }
    @Override
    public void enable() {
        Player runner = getPlayer();
        if (runner != null) {
            runner.setDisplayName("§2Runner §r" + runner.getName());
            runner.setPlayerListName("§2Runner §r" + runner.getName());
            runner.setGameMode(GameMode.SURVIVAL);
            runner.sendMessage("You are selected as §2Runner§r, run!!!!");
        }
    }
    @Override
    public void disable() {
        super.disable();
        tracked = false;
    }
    public Location getLocation(World.Environment environment) {
        return switch (environment) {
            case NORMAL -> overWorld;
            case NETHER -> nether;
            case THE_END -> end;
            default -> null;
        };
    }
    private void startTracking() {
        taskID = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(PluginInitializer.getPlugin(), () -> {
                    Player player = getPlayer();
                    if (!tracked || player == null) {
                        Bukkit.getScheduler().cancelTask(taskID);
                        return;
                    }
                    World world = player.getLocation().getWorld();
                    if (world == null) return;
                    switch (world.getEnvironment()) {
                        case NORMAL -> overWorld = player.getLocation();
                        case NETHER -> nether = player.getLocation();
                        case THE_END -> end = player.getLocation();
                        default -> Bukkit.broadcastMessage("§cPlugin only works in vanilla " +
                                "dimensions (overworld,nether,end)!");
                    }
                }, 0, PluginInitializer.getPlugin().getUpdateRate());
    }
}