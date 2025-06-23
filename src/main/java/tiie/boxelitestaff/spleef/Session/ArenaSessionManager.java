package tiie.boxelitestaff.spleef.Session;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaSessionManager {

    private final Map<UUID, ArenaSetupSession> sessions = new HashMap<>();

    public void startSession(Player player, String name) {
        sessions.put(player.getUniqueId(), new ArenaSetupSession(player, name));
    }

    public ArenaSetupSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void endSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }
}
