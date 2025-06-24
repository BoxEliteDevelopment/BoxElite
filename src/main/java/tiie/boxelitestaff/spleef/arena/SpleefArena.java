package tiie.boxelitestaff.spleef.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpleefArena {


    private final String name;
    private final World world;
    private final Location lobbySpawn;
    private final List<Location> gameSpawns;
    private final int minPlayers;
    private final int maxPlayers;
    private final Region region;

    private Map<Location, Material> originalFloor = new HashMap<>();

    private Location spectatorSpawn;

    public SpleefArena(String name, World world, Location lobbySpawn, List<Location> gameSpawns,
                       int minPlayers, int maxPlayers, Region region) {
        this.name = name;
        this.world = world;
        this.lobbySpawn = lobbySpawn;
        this.gameSpawns = gameSpawns;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.region = region;
    }

    public String getName() {
        return name;
    }



    public World getWorld() {
        return world;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public List<Location> getGameSpawns() {
        return gameSpawns;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Region getRegion() {
        return region;
    }

    public enum ArenaState {
        IDLE,
        IN_GAME,
        RESETTING
    }

    private ArenaState state = ArenaState.IDLE;

    public ArenaState getState() {
        return state;
    }


    public Location getSpectatorSpawn() {
        return spectatorSpawn;
    }

    // Add a setter or load from config:
    public void setSpectatorSpawn(Location loc) {
        this.spectatorSpawn = loc;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public boolean isAvailable() {
        return state == ArenaState.IDLE;
    }

    public void captureOriginalFloor() {
        originalFloor.clear();
        Region region = this.region;
        World world = region.getPos1().getWorld();

        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    Location loc = new Location(world, x, y, z);
                    originalFloor.put(loc, world.getBlockAt(loc).getType());
                }
            }
        }
    }

    public void restoreOriginalFloor() {
        for (Map.Entry<Location, Material> entry : originalFloor.entrySet()) {
            entry.getKey().getBlock().setType(entry.getValue(), false);
        }
    }
}


