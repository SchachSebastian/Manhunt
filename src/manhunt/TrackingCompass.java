package manhunt;

import main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class TrackingCompass {
    // attribute
    private ItemStack compass;
    private final CompassMenu compassMenu;
    private Player owner;
    private ManhuntPlayer pointingTo;
    private boolean sendMessage = true;
    private int taskID;
    private boolean tracking = true;
    // getter/setter
    public ItemStack getCompass() {
        return compass;
    }
    public void setCompass(ItemStack compass) {
        this.compass = compass;
    }
    public CompassMenu getCompassMenu() {
        return compassMenu;
    }
    public Player getOwner() {
        return owner;
    }
    public void setOwner(Player owner) {
        this.owner = owner;
    }
    public ManhuntPlayer getPointingTo() {
        return pointingTo;
    }
    public void setPointingTo(ManhuntPlayer pointingTo) {
        this.pointingTo = pointingTo;
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) meta.setDisplayName("Tracking " + pointingTo.getPlayer().getName());
        compass.setItemMeta(meta);
    }
    public boolean isTracking() {
        return tracking;
    }
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
    // update compass meta - and define repeating task
    public void updateCompassData() {
        ItemMeta itemMeta = compass.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        }
        this.compass.setItemMeta(itemMeta);
        // scheduler
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            if (tracking) {
                if (owner.isOnline()) {
                    Location loc = null;
                    switch (owner.getLocation().getWorld().getEnvironment()) {
                        case NORMAL -> loc = pointingTo.getOverWorld();
                        case NETHER -> loc = pointingTo.getNether();
                        case THE_END -> loc = pointingTo.getEnd();
                        default -> Bukkit.broadcastMessage("Unknown World.Environment!");
                    }
                    if (loc != null) {
                        sendMessage = true;
                        try {
                            CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
                            // set position, compass should point to
                            compassMeta.setLodestone(loc);
                            compassMeta.setLodestoneTracked(false);
                            compass.setItemMeta(compassMeta);
                        } catch (Exception ex) {
                            Bukkit.broadcastMessage("§cSome error appeared!");
                        }
                    } else if (sendMessage) {
                        sendMessage = false;
                        owner.sendMessage("§cRunner wasn't in this world! Tracking won't work!");
                    }
                }
            } else {
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 0, Main.getPlugin().getUpdateRate());
    }
    // constructor
    public TrackingCompass(Player owner, ManhuntPlayer pointingTo, CompassMenu compassMenu) {
        this.compass = new ItemStack(Material.COMPASS);
        setPointingTo(pointingTo);
        setOwner(owner);
        this.compassMenu = compassMenu;
        updateCompassData();
    }
}