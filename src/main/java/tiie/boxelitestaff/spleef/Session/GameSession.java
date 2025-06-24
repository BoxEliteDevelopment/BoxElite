package tiie.boxelitestaff.spleef.Session;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.spleef.Arena.ArenaManager;
import tiie.boxelitestaff.spleef.Arena.SpleefArena;
import tiie.boxelitestaff.spleef.Player.StatsManager;
import tiie.boxelitestaff.spleef.SpleefListener.SpleefGameListener;
import tiie.boxelitestaff.spleef.scoreboard.SpleefScoreboardManager;

import java.util.*;

public class GameSession {

    private final SpleefArena arena;
    private final List<Player> players;
    private final ArenaManager arenaManager;
    private final Set<UUID> activePlayers = new HashSet<>();


    private final Map<UUID, Location> assignedSpawns = new HashMap<>();

    private int countdownTaskId = -1;
    private int gameLoopTaskId = -1;
    private boolean ended = false;

    private int queueCountdownTaskId = -1;

    public GameSession(SpleefArena arena, List<Player> players, ArenaManager manager) {
        this.arena = arena;
        this.players = players;
        this.arenaManager = manager;
    }

    private int timeLeft = 120; // 2 minutes by default

    public int getTimeLeft() {
        return timeLeft;
    }

    public void startQueueCountdown(int seconds) {
        final int[] timeLeft = {seconds};

        queueCountdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                () -> {
                    if (timeLeft[0] == 0) {
                        Bukkit.getScheduler().cancelTask(queueCountdownTaskId);
                        queueCountdownTaskId = -1;
                        runCountdown(5); // ⬅️ Actually run 5s countdown now (will teleport)
                        return;
                    }

                    for (Player player : players) {
                        if (player.isOnline()) {
                            player.sendTitle("§eGame starting in", "§c" + timeLeft[0] + "s", 5, 15, 5);
                            BoxEliteStaff.getInstance().getScoreboardManager()
                                    .showGameBoard(player, timeLeft[0], players.size());
                        }
                    }

                    timeLeft[0]--;
                }, 0L, 20L
        );
    }

    public SpleefArena getArena() {
        return arena;
    }

    public void start() {
        arena.setState(SpleefArena.ArenaState.IN_GAME);
        arena.captureOriginalFloor();
        Bukkit.broadcastMessage("§a[Spleef] Game starting in arena §e" + arena.getName());

        List<Location> spawns = new ArrayList<>(arena.getGameSpawns());
        Collections.shuffle(spawns);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.getInventory().clear();

           BoxEliteStaff.getInstance().getSpleefGameListener().addPlayer(player);
            player.setGameMode(GameMode.SURVIVAL);
            BoxEliteStaff.getInstance().getScoreboardManager()
                    .showGameBoard(player, timeLeft, players.size());
            Location spawn = spawns.get(i % spawns.size());
            assignedSpawns.put(player.getUniqueId(), spawn);
            activePlayers.add(player.getUniqueId());

        }

        runCountdown(5); // 5-second countdown
    }

    private int countdownSecondsLeft = 5;

    private void runCountdown(int seconds) {
        countdownSecondsLeft = seconds;

        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                new Runnable() {
                    @Override
                    public void run() {
                        if (countdownSecondsLeft == 0) {
                            for (UUID uuid : activePlayers) {
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    Location spawn = assignedSpawns.get(uuid);
                                    player.getInventory().addItem(createEventShovel());
                                    if (spawn != null) player.teleport(spawn);
                                    player.sendTitle("§a§lGO!", "", 0, 20, 0);
                                }
                            }
                            startGameListener();
                            Bukkit.getScheduler().cancelTask(countdownTaskId);
                            return;
                        }

                        for (UUID uuid : activePlayers) {
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                player.sendTitle("§e" + countdownSecondsLeft, "", 0, 20, 0);
                            }
                        }

                        countdownSecondsLeft--;
                    }
                }, 0L, 20L
        );
    }

    private void startGameListener() {
        gameLoopTaskId = Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                () -> {
                    timeLeft--; // decrease each tick

                    Iterator<UUID> iter = activePlayers.iterator();
                    while (iter.hasNext()) {
                        UUID uuid = iter.next();
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.getLocation().getY() < arena.getRegion().getMinY() - 2) {
                            player.sendMessage("§cYou’ve been eliminated!");
                            iter.remove();
                            BoxEliteStaff.getInstance().getSpleefGameListener().removePlayer(player);
                            BoxEliteStaff.getInstance().getSpleefGameListener().addSpectator(player);
                            player.getInventory().clear();
                            player.setGameMode(GameMode.SPECTATOR);
                            player.teleport(arena.getSpectatorSpawn());
                        }
                    }


                    BoxEliteStaff.getInstance()
                            .getScoreboardManager()
                            .updateInGameBoards(activePlayers, timeLeft, activePlayers.size());


                    if (activePlayers.size() <= 1) {
                        end();
                    }

                    if (timeLeft == 0) {

                        //TODO triggerSuddenDeath(); class to be made
                    }
                },
                20L, 20L
        ).getTaskId();
    }





    public void end() {
        if (ended) return;
        ended = true;

        Bukkit.getScheduler().cancelTask(countdownTaskId);
        Bukkit.getScheduler().cancelTask(gameLoopTaskId);

        announceWinner();

        // Clear scoreboards for active players & spectators
        SpleefScoreboardManager scoreboardManager = BoxEliteStaff.getInstance().getScoreboardManager();

        // Clear active players' scoreboard
        scoreboardManager.clearAll(activePlayers);

        // Clear spectators' scoreboard
        scoreboardManager.clearAll(BoxEliteStaff.getInstance().getSpleefGameListener().getSpectators());

        Bukkit.getScheduler().runTaskLater(BoxEliteStaff.getInstance(), () -> {
            cleanupPlayers();
            cleanupSpectators();
            arena.restoreOriginalFloor();
            arena.setState(SpleefArena.ArenaState.IDLE);
            arenaManager.endGame(arena.getName());
        }, 20 * 10L);
    }

        private void announceWinner() {
        StatsManager stats = BoxEliteStaff.getInstance().getStatsManager();

        if (activePlayers.size() == 1) {
            Player winner = Bukkit.getPlayer(activePlayers.iterator().next());
            if (winner != null) {
                Bukkit.broadcastMessage("§6[Spleef] Winner: §a" + winner.getName());
                stats.getStats(winner.getUniqueId()).addWin();
            }
        } else {
            Bukkit.broadcastMessage("§6[Spleef] Everyone eliminated, no winner!");
        }
    }

    private void cleanupPlayers() {
        for (UUID uuid : new HashSet<>(activePlayers)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.teleport(arena.getLobbySpawn());
                player.sendMessage("§eGame over.");
                BoxEliteStaff.getInstance().getSpleefGameListener().removePlayer(player);
                player.getInventory().clear();
                player.setGameMode(GameMode.ADVENTURE);
            }
        }
        activePlayers.clear();
        assignedSpawns.clear();
    }

    private void cleanupSpectators() {
        SpleefGameListener listener = BoxEliteStaff.getInstance().getSpleefGameListener();
        for (UUID uuid : new HashSet<>(listener.getSpectators())) {
            Player spectator = Bukkit.getPlayer(uuid);
            if (spectator != null) {
                spectator.teleport(arena.getLobbySpawn());
                spectator.setGameMode(GameMode.SURVIVAL);
                spectator.sendMessage("§eGame over. You've been returned to the lobby.");
            }
        }
        listener.clearSpectators();
    }

    private ItemStack createEventShovel() {
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta meta = shovel.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true); // Efficiency V
            meta.setUnbreakable(true);
            meta.displayName(Component.text("§bEvent Shovel"));
            shovel.setItemMeta(meta);
        }
        return shovel;
    }

    public void endSpectating() {
        SpleefGameListener spleefGameListener = BoxEliteStaff.getInstance().getSpleefGameListener();
        for (UUID uuid : spleefGameListener.getSpectators()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.teleport(arena.getLobbySpawn());
                player.setGameMode(GameMode.ADVENTURE);
                player.sendMessage("§eYou are no longer spectating.");
                spleefGameListener.removeSpectator(player);
            }
        }
    }


    public void forceEliminate(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage("§cYou were eliminated for leaving.");
            activePlayers.remove(uuid);
            BoxEliteStaff.getInstance().getSpleefGameListener().removePlayer(player);
            BoxEliteStaff.getInstance().getSpleefGameListener().addSpectator(player);
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(arena.getSpectatorSpawn());
        }
    }


    public boolean isPlayerActive(UUID uuid) {
        return activePlayers.contains(uuid);
    }
}
