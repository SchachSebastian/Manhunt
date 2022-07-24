package manhunt.compass;

import manhunt.players.Runner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.List;

public class CompassMenu {
    private static final int MENU_SIZE = 9;
    private static final ItemStack nextPage;
    private static final ItemStack prevPage;
    private final Inventory menu;
    private int page = 0;
    private final List<Runner> players;
    static {
        prevPage = new ItemStack(Material.ARROW);
        ItemMeta itemMeta = prevPage.getItemMeta();
        itemMeta.setDisplayName("Previous Page");
        prevPage.setItemMeta(itemMeta);
        nextPage = new ItemStack(Material.ARROW);
        itemMeta = nextPage.getItemMeta();
        itemMeta.setDisplayName("Next page");
        nextPage.setItemMeta(itemMeta);
    }
    public CompassMenu(@Nonnull List<Runner> players) {
        this.players = players;
        this.menu = Bukkit.createInventory(null, MENU_SIZE, "Tracking: ");
        updateCompassMenu();
    }
    public Runner getClickedPlayer(ItemStack clickedItem) {
        if (clickedItem == null) return null;
        if (clickedItem.isSimilar(nextPage)) {
            page++;
            updateCompassMenu();
            return null;
        }
        if (clickedItem.isSimilar(prevPage)) {
            page--;
            updateCompassMenu();
            return null;
        }
        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null) return null;
        String name = itemMeta.getDisplayName();
        for (Runner p : players) {
            if (name.equals(p.getPlayer().getName())) {
                resetPage();
                return p;
            }
        }
        return null;
    }
    public void resetPage() {
        this.page = 0;
    }
    public void updateCompassMenu() {
        menu.clear();
        int start = 0;
        int runnerIndexStart = page * 7;
        if (page > 0) {
            menu.setItem(0, prevPage);
            start = 1;
            runnerIndexStart++;
        }
        for (int i = start; i < MENU_SIZE && i + runnerIndexStart < players.size(); ++i) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(players.get(i + runnerIndexStart).getPlayer());
            skullMeta.setDisplayName(players.get(i + runnerIndexStart).getPlayer().getName());
            head.setItemMeta(skullMeta);
            menu.setItem(i, head);
        }
        if (runnerIndexStart + MENU_SIZE - 1 < players.size()) {
            menu.setItem(MENU_SIZE - 1, nextPage);
        }
    }
    public Inventory getCompassMenu() {
        return menu;
    }
}