package tiie.boxelitestaff.spleef.player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tiie.boxelitestaff.BoxEliteStaff;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {
    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();
    private final File statsFile;
    private FileConfiguration config;

    public StatsManager() {
        statsFile = new File(BoxEliteStaff.getInstance().getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(statsFile);
        loadStats();
    }

    public PlayerStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, id -> {
            PlayerStats stats = new PlayerStats(id);
            ConfigurationSection sec = config.getConfigurationSection(id.toString());
            if (sec != null) stats.loadFromConfig(sec);

            // ✅ Set name if online
            Player player = Bukkit.getPlayer(id);
            if (player != null) stats.setPlayerName(player.getName());

            return stats;
        });
    }

    public void resetStats(UUID uuid) {
        statsMap.put(uuid, new PlayerStats(uuid));
    }

    public Collection<PlayerStats> getAllStats() {
        return statsMap.values();
    }

    public void saveStats() {
        for (PlayerStats stats : statsMap.values()) {
            ConfigurationSection sec = config.createSection(stats.getUuid().toString());
            stats.saveToConfig(sec);
        }

        try {
            config.save(statsFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save Spleef stats.");
            e.printStackTrace();
        }
    }

    public void loadStats() {
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                PlayerStats stats = new PlayerStats(uuid);
                stats.loadFromConfig(config.getConfigurationSection(key));
                statsMap.put(uuid, stats);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public List<Map.Entry<String, Integer>> getTopPlayersByWins(int limit) {
        return statsMap.values().stream()
                .sorted(Comparator.comparingInt(PlayerStats::getWins).reversed())
                .limit(limit)
                .map(stat -> Map.entry(stat.getPlayerName(), stat.getWins()))
                .collect(Collectors.toList());
    }

    public boolean resetPlayerStats(String playerName) {
        UUID targetUUID = null;

        for (PlayerStats stat : statsMap.values()) {
            if (stat.getPlayerName().equalsIgnoreCase(playerName)) {
                targetUUID = stat.getUuid();
                break;
            }
        }

        if (targetUUID == null) {
            return false;
        }

        statsMap.remove(targetUUID);
        saveStats();
        return true;
    }

}
