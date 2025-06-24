package tiie.boxelitestaff.spleef.Session;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaSessionManager {


    private final Map<UUID, ArenaSetupSession> setupSessions = new HashMap<>();

    // Live game sessions per arena name
    private final Map<String, GameSession> runningSessions = new HashMap<>();

    // Setup session methods
    public void startSetupSession(Player player, String name) {
        setupSessions.put(player.getUniqueId(), new ArenaSetupSession(player, name));
    }

    public ArenaSetupSession getSetupSession(Player player) {
        return setupSessions.get(player.getUniqueId());
    }

    public void endSetupSession(Player player) {
        setupSessions.remove(player.getUniqueId());
    }

    public boolean hasSetupSession(Player player) {
        return setupSessions.containsKey(player.getUniqueId());
    }

    // Game session methods
    public void addGameSession(String arenaName, GameSession session) {
        runningSessions.put(arenaName, session);
    }

    public GameSession getGameSession(String arenaName) {
        return runningSessions.get(arenaName);
    }

    public void removeGameSession(String arenaName) {
        runningSessions.remove(arenaName);
    }

    public boolean isArenaInGame(String arenaName) {
        return runningSessions.containsKey(arenaName);
    }

    public GameSession getSessionByPlayer(UUID uuid) {
        for (GameSession session : runningSessions.values()) {
            if (session.isPlayerActive(uuid)) return session;
        }
        return null;
    }

}
