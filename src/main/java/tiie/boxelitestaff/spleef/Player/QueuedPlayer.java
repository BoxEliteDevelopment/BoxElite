package tiie.boxelitestaff.spleef.Player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QueuedPlayer {

    private final UUID uuid;

    public QueuedPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
