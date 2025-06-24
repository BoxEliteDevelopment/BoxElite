package tiie.boxelitestaff.spleef.Arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import tiie.boxelitestaff.spleef.Player.QueuedPlayer;
import tiie.boxelitestaff.spleef.Session.GameSession;

import java.util.*;
import java.util.stream.Collectors;

public class ArenaManager {

    private final Map<String, SpleefArena> arenas = new HashMap<>();

    private final Map<String, GameSession> runningGames = new HashMap<>();
    private final Queue<QueuedPlayer> joinQueue = new LinkedList<>();

    public void loadArenas(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            ConfigurationSection arenaSec = section.getConfigurationSection(name);
            if (arenaSec == null) continue;

            World world = Bukkit.getWorld(arenaSec.getString("world"));
            if (world == null) continue;

            Location lobby = parseLocation(world, arenaSec.getConfigurationSection("lobbySpawn"));

            List<Location> spawns = new ArrayList<>();
            for (Map<?, ?> locMap : arenaSec.getMapList("gameSpawns")) {
                spawns.add(parseLocation(world, locMap));
            }

            Location pos1 = parseLocation(world, arenaSec.getConfigurationSection("region.pos1"));
            Location pos2 = parseLocation(world, arenaSec.getConfigurationSection("region.pos2"));

            SpleefArena arena = new SpleefArena(
                    name,
                    world,
                    lobby,
                    spawns,
                    arenaSec.getInt("minPlayers"),
                    arenaSec.getInt("maxPlayers"),
                    new Region(pos1, pos2)
            );

            arenas.put(name.toLowerCase(), arena);
        }
    }


    //changed to INTEGER IN MAP

    private Location parseLocation(World world, Map<?, ?> map) {
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = map.containsKey("yaw") ? ((Number) map.get("yaw")).floatValue() : 0f;
        float pitch = map.containsKey("pitch") ? ((Number) map.get("pitch")).floatValue() : 0f;
        return new Location(world, x, y, z, yaw, pitch);
    }

    private Location parseLocation(World world, ConfigurationSection section) {
        return new Location(
                world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw", 0),
                (float) section.getDouble("pitch", 0)
        );
    }



    public Collection<SpleefArena> getAllArenas() {
        return arenas.values();
    }

    public void registerArena(SpleefArena arena) {
        arenas.put(arena.getName().toLowerCase(), arena);
    }

    public SpleefArena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public List<SpleefArena> getAvailableArenas() {
        return arenas.values().stream()
                .filter(SpleefArena::isAvailable)
                .collect(Collectors.toList());
    }

    private List<Player> dequeuePlayers(int count) {
        List<Player> players = new ArrayList<>();
        while (!joinQueue.isEmpty() && players.size() < count) {
            QueuedPlayer qp = joinQueue.poll();
            if (qp.getPlayer().isOnline()) {
                players.add(qp.getPlayer());
            }
        }
        return players;
    }

    public boolean isInGame(String arenaName) {
        return runningGames.containsKey(arenaName.toLowerCase());
    }

    public void endGame(String arenaName) {
        runningGames.remove(arenaName.toLowerCase());
        SpleefArena arena = arenas.get(arenaName.toLowerCase());
        if (arena != null) {
            arena.setState(SpleefArena.ArenaState.RESETTING);
            // Call arena.reset(); once implemented
            arena.setState(SpleefArena.ArenaState.IDLE);
        }
    }

    public void registerSession(String arenaName, GameSession session) {
        runningGames.put(arenaName.toLowerCase(), session);
    }


}
