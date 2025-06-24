package tiie.boxelitestaff.spleef.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.spleef.player.PlayerStats;
import tiie.boxelitestaff.spleef.player.StatsManager;

import java.util.*;

public class SpleefScoreboardManager {

    private final Map<UUID, Scoreboard> boards = new HashMap<>();
    private final StatsManager statsManager = BoxEliteStaff.getInstance().getStatsManager();
    private final ScoreboardManager manager = Bukkit.getScoreboardManager();

    public void showQueueBoard(Player player, int current, int max) {
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.getObjective("queue");
        if (obj == null) {
            obj = board.registerNewObjective("queue", Criteria.DUMMY, "§b§lSpleef Queue");
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(3);
        obj.getScore("§fPlayers: §a" + current + "/" + max).setScore(2);
        obj.getScore("  ").setScore(1);
        obj.getScore("§eWaiting...").setScore(0);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);
    }

    public void showGameBoard(Player player, int secondsLeft, int playersLeft) {
        PlayerStats stats = statsManager.getStats(player.getUniqueId());
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.getObjective("game");
        if (obj == null) {
            obj = board.registerNewObjective("game", Criteria.DUMMY, "§6§lSpleef Match");
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(6);
        obj.getScore("§fTime Left: §a" + secondsLeft + "s").setScore(5);
        obj.getScore("§fPlayers Left: §a" + playersLeft).setScore(4);
        obj.getScore("  ").setScore(3);
        obj.getScore("§fWins: §b" + stats.getWins()).setScore(2);
        obj.getScore("§fLosses: §b" + stats.getLosses()).setScore(1);
        obj.getScore("§fGames: §b" + stats.getGamesPlayed()).setScore(0);

        player.setScoreboard(board);
        boards.put(player.getUniqueId(), board);
    }

    public void updateInGameBoards(Collection<UUID> activePlayers, int secondsLeft, int playersLeft) {
        for (UUID uuid : activePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            showGameBoard(player, secondsLeft, playersLeft);
        }
    }

    public void clear(Player player) {
        player.setScoreboard(manager.getNewScoreboard());
        boards.remove(player.getUniqueId());
    }

    public void clearAll(Collection<UUID> players) {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) clear(player);
        }
    }

    public void clearQueueBoard(Player player) {
        clear(player);
    }


}
