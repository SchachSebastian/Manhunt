package manhunt;

import manhunt.players.Hunter;
import manhunt.players.ManhuntPlayer;
import manhunt.players.Runner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Manhunt {
    // attributes
    private static final Manhunt instance = new Manhunt();
    private final List<Hunter> hunters;
    private boolean isRunning; // indicates if manhunt is currently running
    private final List<ManhuntPlayer> nonPlayers;
    private final List<Runner> runners;
    // constructor
    private Manhunt() {
        this.hunters = new ArrayList<>();
        this.nonPlayers = new ArrayList<>();
        this.runners = new ArrayList<>();
        this.isRunning = false;
    }
    // instance of Manhunt
    public static Manhunt getInstance() {
        return instance;
    }
    public void addHunter(Player hunter) {
        if (isPlayer(hunter)) return;
        if (isNonPlayer(hunter)) removeNonPlayer(hunter);
        Hunter player = new Hunter(hunter);
        this.hunters.add(player);
        if (isRunning) {
            player.enable();
        }
    }
    public void addNonPlayer(Player nonPlayer) {
        if (isNonPlayer(nonPlayer)) return;
        ManhuntPlayer player = new ManhuntPlayer(nonPlayer);
        player.enable();
        this.nonPlayers.add(player);
    }
    public void addHunters(List<Player> hunters) {
        hunters.forEach(this::addHunter);
    }
    // add/remove players
    public void addRunner(Player runner) {
        if (isPlayer(runner)) return;
        if (isNonPlayer(runner)) removeNonPlayer(runner);
        Runner player = new Runner(runner);
        this.runners.add(player);
        if (isRunning) {
            player.enable();
        }
    }
    public void addRunners(List<Player> runners) {
        runners.forEach(this::addRunner);
    }
    public void finish() {
        isRunning = false;
        this.hunters.forEach(Hunter::disable);
        this.hunters.clear();
        this.runners.forEach(Runner::disable);
        this.runners.clear();
        this.nonPlayers.forEach(ManhuntPlayer::disable);
        this.nonPlayers.clear();
    }
    public boolean isHunter(Player hunter) {
        return hunters.stream().anyMatch(h -> h.isPlayer(hunter));
    }
    public boolean isPlayer(Player player) {
        return isRunner(player) || isHunter(player);
    }
    public boolean isRunner(Player runner) {
        return runners.stream().anyMatch(r -> r.isPlayer(runner));
    }
    public boolean isNonPlayer(Player nonPlayer) {
        return nonPlayers.stream().anyMatch(n -> n.isPlayer(nonPlayer));
    }
    public void removeHunter(Player hunter) {
        this.hunters.stream().filter(h -> h.isPlayer(hunter)).forEach(h -> {
            this.hunters.remove(h);
            h.disable();
            ((ManhuntPlayer) h).enable();
            this.nonPlayers.add(h);
        });
    }
    public void removeRunner(Player runner) {
        this.runners.stream().filter(r -> r.isPlayer(runner)).forEach(r -> {
            this.runners.remove(r);
            r.disable();
            ((ManhuntPlayer) r).enable();
            this.nonPlayers.add(r);
        });
    }
    public void removePlayer(Player player) {
        if (isRunner(player)) removeRunner(player);
        else if (isHunter(player)) removeHunter(player);
    }
    public Hunter getHunter(Player hunter) {
        List<Hunter> hunters = this.hunters.stream().filter(h -> h.isPlayer(hunter)).toList();
        return hunters.isEmpty() ? null : hunters.get(0);
    }
    public Runner getRunner(Player runner) {
        List<Runner> runners = this.runners.stream().filter(h -> h.isPlayer(runner)).toList();
        return runners.isEmpty() ? null : runners.get(0);
    }
    public void start() {
        if (hunters.size() == 0 || runners.size() == 0) return;
        this.hunters.forEach(Hunter::enable);
        this.runners.forEach(Runner::enable);
        isRunning = true;
    }
    public void runnerDeath(Player deathRunner) {
        deathRunner.sendMessage("§4It's over! The hunters caught you!");
        removeRunner(deathRunner);
        if (runners.size() == 0) {
            Bukkit.broadcastMessage("§2Hunters won!");
            finish();
        }
    }
    private void removeNonPlayer(Player nonPlayer) {
        this.nonPlayers.stream().filter(n -> n.isPlayer(nonPlayer)).forEach(n -> {
            this.nonPlayers.remove(n);
            n.disable();
        });
    }
    public List<Runner> getRunners() {
        return runners;
    }
    public boolean isRunning() {
        return isRunning;
    }
}