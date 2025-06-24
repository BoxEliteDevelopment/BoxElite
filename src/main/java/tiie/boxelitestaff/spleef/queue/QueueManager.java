package tiie.boxelitestaff.spleef.queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.spleef.arena.ArenaManager;
import tiie.boxelitestaff.spleef.arena.SpleefArena;
import tiie.boxelitestaff.spleef.player.QueuedPlayer;
import tiie.boxelitestaff.spleef.session.GameSession;

import java.util.*;

public class QueueManager {

    private final ArenaManager arenaManager;
    private final Queue<QueuedPlayer> joinQueue = new LinkedList<>();

    private int countdown = 30; // start at 30 seconds
    private int taskId = -1;
    private boolean usingTitleCountdown = false;

    public QueueManager(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    public void addPlayer(Player player) {
        if (joinQueue.stream().anyMatch(qp -> qp.getPlayer().getUniqueId().equals(player.getUniqueId()))) {
            player.sendMessage("§cYou're already in the queue.");
            return;
        }

        joinQueue.add(new QueuedPlayer(player));
        player.sendMessage("§aYou have been added to the Spleef queue.");

        int current = joinQueue.size();
        int max = 8;

        // Update scoreboard for everyone in queue
        for (QueuedPlayer qp : joinQueue) {
            Player p = qp.getPlayer();
            if (p.isOnline()) {
                BoxEliteStaff.getInstance().getScoreboardManager().showQueueBoard(p, current, max);
                if (!p.equals(player)) {
                    p.sendMessage("§7[Spleef] §e" + player.getName() + " joined the queue. §7(" + current + "/" + max + ")");
                }
            }
        }

        if (taskId == -1) {
            startCountdownTask(30);
        } else {
            checkAndAdjustCountdown();
        }
    }

    public void removePlayer(Player player) {
        boolean removed = joinQueue.removeIf(qp -> qp.getPlayer().getUniqueId().equals(player.getUniqueId()));
        if (removed) {
            player.sendMessage("§cYou have left the Spleef queue.");
            BoxEliteStaff.getInstance().getScoreboardManager().clearQueueBoard(player);  // Clear scoreboard
        } else {
            player.sendMessage("§cYou are not in the Spleef queue.");
        }

        if (joinQueue.isEmpty() && taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            broadcastMessage("§c[Spleef] Queue is empty. Countdown cancelled.");
        }
    }

    private void startCountdownTask(int startSeconds) {
        countdown = startSeconds;
        usingTitleCountdown = countdown <= 5;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Bukkit.getPluginManager().getPlugin("BoxEliteStaff"),
                () -> {
                    if (countdown <= 0) {
                        tryStartGame();
                        Bukkit.getScheduler().cancelTask(taskId);
                        taskId = -1;
                        usingTitleCountdown = false;
                        countdown = 30;
                        return;
                    }

                    if (usingTitleCountdown) {
                        for (QueuedPlayer qp : joinQueue) {
                            Player p = qp.getPlayer();
                            if (p != null && p.isOnline()) {
                               // p.sendTitle("§e" + countdown, "", 0, 20, 0);
                            }
                        }
                    } else {

                        if (countdown % 5 == 0) {
                            broadcastQueueCountdown(countdown);
                        }
                        if (countdown == 5) {
                            usingTitleCountdown = true;
                        }
                    }

                    countdown--;
                },
                20L, 20L
        );
    }

    private void checkAndAdjustCountdown() {
        int maxPlayers = 0;
        if (!joinQueue.isEmpty()) {
            maxPlayers = arenaManager.getAvailableArenas().stream()
                    .mapToInt(SpleefArena::getMaxPlayers)
                    .max().orElse(0);
        }

        if (maxPlayers > 0 && joinQueue.size() >= maxPlayers && countdown > 5) {
            countdown = 5;
            broadcastQueueCountdown(countdown);
        }
    }

    public void tryStartGame() {
        if (joinQueue.isEmpty()) return;

        // Get first available arena
        List<SpleefArena> availableArenas = arenaManager.getAvailableArenas();
        if (availableArenas.isEmpty()) return;

        SpleefArena arena = availableArenas.get(0);
        int maxPlayers = arena.getMaxPlayers();

        List<Player> playersToStart = new ArrayList<>();

        while (!joinQueue.isEmpty() && playersToStart.size() < maxPlayers) {
            QueuedPlayer qp = joinQueue.poll();
            if (qp.getPlayer().isOnline()) {
                playersToStart.add(qp.getPlayer());
            }
        }

        // Check if enough players to start
        if (playersToStart.size() < arena.getMinPlayers()) {
            playersToStart.forEach(p -> joinQueue.add(new QueuedPlayer(p)));
            broadcastMessage("§cNot enough players to start the game.");
            return;
        }

        for (Player player : playersToStart) {
            BoxEliteStaff.getInstance().getQueueManager().clearQueueBoard(player);
        }


        GameSession session = new GameSession(arena, playersToStart, arenaManager);
        arenaManager.registerSession(arena.getName(), session);

        broadcastMessage("§aEnough players! Game starting in 5 seconds...");

        broadcastMessage("§aEnough players! Game starting in 5 seconds...");

        session.start();

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcastQueueCountdown(int seconds) {
        broadcastMessage("§e[Spleef] Game starting in §6" + seconds + "§e seconds! Join the queue now!");
    }

    private void broadcastMessage(String message) {
        joinQueue.forEach(qp -> {
            Player p = qp.getPlayer();
            if (p.isOnline()) {
                p.sendMessage(message);
            }
        });
    }

    public boolean isInQueue(Player player) {
        return joinQueue.stream().anyMatch(qp -> qp.getPlayer().getUniqueId().equals(player.getUniqueId()));
    }
    public void clearQueueBoard(Player player) {
        BoxEliteStaff.getInstance().getScoreboardManager().clearQueueBoard(player);
    }
}
