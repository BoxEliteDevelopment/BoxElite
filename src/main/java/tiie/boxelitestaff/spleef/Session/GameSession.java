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
import tiie.boxelitestaff.spleef.SpleefListener.SpleefGameListener;

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

    public GameSession(SpleefArena arena, List<Player> players, ArenaManager manager) {
        this.arena = arena;
        this.players = players;
        this.arenaManager = manager;
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
            Location spawn = spawns.get(i % spawns.size());
            assignedSpawns.put(player.getUniqueId(), spawn);
            activePlayers.add(player.getUniqueId());
        }

        runCountdown(5); // 5-second countdown
    }

    private void runCountdown(int seconds) {
        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                new Runnable() {
                    int timeLeft = seconds;

                    @Override
                    public void run() {
                        if (timeLeft == 0) {
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
                                player.sendTitle("§e" + timeLeft, "", 0, 20, 0);
                            }
                        }

                        timeLeft--;
                    }
                },
                0L, 20L
        );
    }

    private void startGameListener() {
        gameLoopTaskId = Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                () -> {
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


                    if (activePlayers.size() <= 1) {
                        end();
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

        // Announce winner or no winner
        if (activePlayers.size() == 1) {
            Player winner = Bukkit.getPlayer(activePlayers.iterator().next());
            if (winner != null) {
                Bukkit.broadcastMessage("§6[Spleef] Winner: §a" + winner.getName());
            }
        } else if (activePlayers.isEmpty()) {
            Bukkit.broadcastMessage("§6[Spleef] Everyone eliminated, no winner!");
        }

        // Wait 10 seconds before cleanup and teleporting out
        Bukkit.getScheduler().runTaskLater(BoxEliteStaff.getInstance(), () -> {
            // Teleport out and clean active players
            for (UUID uuid : activePlayers) {
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

            // Teleport and clean spectators (calls the method you added)
            endSpectating();

            arena.setState(SpleefArena.ArenaState.RESETTING);

            arena.restoreOriginalFloor();
            arena.setState(SpleefArena.ArenaState.IDLE);
            arenaManager.endGame(arena.getName());
        }, 20 * 10L); // 10 seconds delay
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
}
