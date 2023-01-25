package manhunt.compass;

import main.PluginInitializer;
import manhunt.Helper;
import manhunt.Manhunt;
import manhunt.players.Hunter;
import manhunt.players.Runner;
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
    private static final String COMPASS_NAME = "Tracking ";
    // attribute
    private static final ItemStack compass;
    private Hunter owner;
    private boolean sendMessage = false;
    private int taskID;
    private boolean tracking = true;
    static {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = compass.getItemMeta();
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            itemMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, false);
        }
        compass.setItemMeta(itemMeta);
    }
    // constructor
    public TrackingCompass(Hunter owner) {
        setOwner(owner);
        taskID = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(PluginInitializer.getPlugin(), () -> {
                    if (!tracking) {
                        Bukkit.getScheduler().cancelTask(taskID);
                    } else {
                        updateCompassData();
                    }
                }, 0, PluginInitializer.getPlugin().getUpdateRate());
    }
    // getter/setter
    public static ItemStack getCompass(Runner runner) {
        return setName(TrackingCompass.compass, runner.getPlayer().getName());
    }
    public static ItemStack setName(ItemStack compass, String name) {
        ItemMeta itemMeta = compass.getItemMeta();
        if (itemMeta != null) itemMeta.setDisplayName(COMPASS_NAME + name);
        compass.setItemMeta(itemMeta);
        return compass;
    }
    @Helper
    public Runner getPointingTo(ItemMeta itemMeta) {
        if (itemMeta == null) return null;
        String name = itemMeta.getDisplayName().replace(COMPASS_NAME, "");
        return Manhunt.getInstance().getRunner(Bukkit.getPlayer(name));
    }
    public void changePointTo(ItemStack item, Player player) {
        if (!isCompass(item)) return;
        setName(item, player.getName());
    }
    public void updateCompassData() {
        if (!owner.exists()) return;
        for (ItemStack c : owner.getCurrentCompasses()) {
            Runner pointingTo = getPointingTo(c.getItemMeta());
            if (pointingTo == null) {
                if (Manhunt.getInstance().getRunners().isEmpty()) return;
                pointingTo = Manhunt.getInstance().getRunners().get(0);
                setName(c, pointingTo.getPlayer().getName());
            }
            Location loc = pointingTo.getLocation(owner.getEnvironment());
            if (loc == null) {
                if (sendMessage) {
                    sendMessage = false;
                    owner.getPlayer().sendMessage(
                            "§cRunner wasn't in this world! Tracking won't " + "work!");
                }
                return;
            }
            CompassMeta compassMeta = (CompassMeta) c.getItemMeta();
            // set position, compass should point to
            compassMeta.setLodestone(loc);
            compassMeta.setLodestoneTracked(false);
            c.setItemMeta(compassMeta);
            sendMessage = true;
        }
    }
    public void setOwner(Hunter owner) {
        this.owner = owner;
    }
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
    public static boolean isCompass(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != compass.getType()) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;
        if (!itemMeta.getEnchants().equals(compass.getItemMeta().getEnchants())) return false;
        return itemMeta.getItemFlags().equals(compass.getItemMeta().getItemFlags());
    }
}