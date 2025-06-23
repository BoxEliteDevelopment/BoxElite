package tiie.boxelitestaff.spleef.Session;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaSetupSession {



    private final Player player;
    private final String arenaName;
    private Location pos1, pos2;
    private Location lobby;
    private final List<Location> spawnPoints = new ArrayList<>();

    public ArenaSetupSession(Player player, String arenaName) {
        this.player = player;
        this.arenaName = arenaName;
    }

    public Player getPlayer() {
        return player;
    }

    public String getArenaName() {
        return arenaName;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getLobby() {
        return lobby;
    }

    public void setLobby(Location lobby) {
        this.lobby = lobby;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }
}
