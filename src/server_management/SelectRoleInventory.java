package server_management;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SelectRoleInventory {
    private static final Inventory inventory;
    private static final int menuSize = 9;
    public static Inventory getInventory() {
        return inventory;
    }
    public static Role getRole(ItemStack clickedItem) {
        if (clickedItem == null) return null;
        Role role;
        try {
            role = Role.valueOf(clickedItem.getItemMeta().getDisplayName());
        } catch (IllegalArgumentException ex) {
            role = null;
        }
        return role;
    }
    static {
        inventory = Bukkit.createInventory(null, menuSize, "Select your role");
        ItemStack hunter = new ItemStack(Material.COMPASS);
        ItemStack runner = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta hunterMeta = hunter.getItemMeta();
        hunterMeta.setDisplayName(Role.HUNTER.name());
        hunter.setItemMeta(hunterMeta);
        ItemMeta runnerMeta = runner.getItemMeta();
        runnerMeta.setDisplayName(Role.RUNNER.name());
        runner.setItemMeta(runnerMeta);
        inventory.setItem(4, runner);
        inventory.setItem(5, hunter);
    }
    private SelectRoleInventory() {
    }
}
