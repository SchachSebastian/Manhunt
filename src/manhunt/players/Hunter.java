package manhunt.players;

import manhunt.Manhunt;
import manhunt.compass.CompassMenu;
import manhunt.compass.TrackingCompass;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hunter extends ManhuntPlayer {
    private final CompassMenu compassMenu;
    private final TrackingCompass trackingCompass;
    public Hunter(Player player) {
        super(player);
        trackingCompass = new TrackingCompass(this);
        compassMenu = new CompassMenu(Manhunt.getInstance().getRunners());
    }
    @Override
    public void enable() {
        Player hunter = getPlayer();
        compassMenu.updateCompassMenu();
        if (hunter != null) {
            hunter.setDisplayName("§4Hunter §r" + hunter.getName());
            hunter.setPlayerListName("§4Hunter §r" + hunter.getName());
            hunter.setGameMode(GameMode.SURVIVAL);
            giveCompass();
            hunter.sendMessage("You are selected as §4Hunter§r, kill the prey!");
        }
    }
    @Override
    public void disable() {
        Player hunter = getPlayer();
        if (hunter != null) {
            hunter.setDisplayName(hunter.getName());
            hunter.setPlayerListName(hunter.getName());
            hunter.setGameMode(GameMode.SURVIVAL);
            trackingCompass.setTracking(false);
            removeCompasses();
        }
    }
    public void compassMenuClick(ItemStack item) {
        Runner runner = compassMenu.getClickedPlayer(item);
        if (runner == null) return;
        getPlayer().closeInventory();
        trackingCompass.changePointTo(getPlayer().getInventory().getItemInMainHand(),
                runner.getPlayer());
    }
    public void giveCompass() {
        if (!exists()) return;
        getPlayer().getInventory()
                .addItem(TrackingCompass.getCompass(Manhunt.getInstance().getRunners().get(0)));
    }
    public void openCompassMenu(ItemStack item) {
        if (!TrackingCompass.isCompass(item)) return;
        if (!exists()) return;
        if (Manhunt.getInstance().getRunners().size() <= 1) return;
        getPlayer().openInventory(compassMenu.getCompassMenu());
    }
    public void removeCompasses() {
        Player hunter = getPlayer();
        if (hunter == null) return;
        getCurrentCompasses().forEach(c -> hunter.getInventory().remove(c));
    }
    public Inventory getCompassMenuInventory() {
        return compassMenu.getCompassMenu();
    }
    public List<ItemStack> getCurrentCompasses() {
        if (!exists()) return new ArrayList<>();
        return Arrays.stream(getPlayer().getInventory().getContents())
                .filter(TrackingCompass::isCompass).toList();
    }
}