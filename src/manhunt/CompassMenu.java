package manhunt;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class CompassMenu {
    private final Inventory menu;
    private final int menuSize = 9;
    private int page = 1;
    private final ArrayList<ManhuntPlayer> players;
    // compass menu
    public Inventory getCompassMenu() {
        return menu;
    }
    public ManhuntPlayer getClickedPlayer(ItemStack clickedItem) {
        ManhuntPlayer player = null;
        if (clickedItem != null) {
            for (ManhuntPlayer p : players) {
                String name = clickedItem.getItemMeta().getDisplayName();
                if (name.equals(p.getPlayer().getName())) {
                    player = p;
                    break;
                } else if (name.equals("Next page")) {
                    page++;
                    break;
                } else if (name.equals("Go back")) {
                    page--;
                    break;
                }
            }
        }
        return player;
    }
    public void resetPage() {
        this.page = 1;
    }
    public void updateCompassMenu() {
        menu.clear();
        int start = menuSize * (page - 1);
        for (int i = page - 1; i > 0; --i) {
            start -= (i == 1 ? 1 : 2);
        }
        int end = menuSize * page;
        boolean next = false;
        for (int i = page; i > 0; --i) {
            end -= (i == 1 ? 1 : 2);
        }
        if (end < players.size() - 1) {
            end--;
            next = true;
        }
        if (page > 1) {
            ItemStack goBack = new ItemStack(Material.ARROW);
            ItemMeta itemMeta = goBack.getItemMeta();
            itemMeta.setDisplayName("Go back");
            goBack.setItemMeta(itemMeta);
            menu.addItem(goBack);
        }
        for (int i = start; i <= end && i < players.size(); ++i) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwningPlayer(players.get(i).getPlayer());
            skullMeta.setDisplayName(players.get(i).getPlayer().getName());
            head.setItemMeta(skullMeta);
            menu.addItem(head);
        }
        if (next) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta itemMeta = nextPage.getItemMeta();
            itemMeta.setDisplayName("Next page");
            nextPage.setItemMeta(itemMeta);
            menu.addItem(nextPage);
        }
    }
    public CompassMenu(@Nonnull ArrayList<ManhuntPlayer> players) {
        this.players = players;
        this.menu = Bukkit.createInventory(null, menuSize, "Tracking: ");
        updateCompassMenu();
    }
}