package tiie.boxelitestaff.spleef.player;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private String playerName;
    private int wins, losses, gamesPlayed, blocksBroken;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getGamesPlayed() { return gamesPlayed; }
    public int getBlocksBroken() { return blocksBroken; }

    public void addWin() { wins++; gamesPlayed++; }
    public void addLoss() { losses++; gamesPlayed++; }
    public void addBlocksBroken(int amt) { blocksBroken += amt; }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    // --- Persistence ---
    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;
        wins = section.getInt("wins", 0);
        losses = section.getInt("losses", 0);
        gamesPlayed = section.getInt("gamesPlayed", 0);
        blocksBroken = section.getInt("blocksBroken", 0);
        this.playerName = section.getString("playerName", "Unknown");
    }

    public void saveToConfig(ConfigurationSection section) {
        if (section == null) return;
        section.set("wins", wins);
        section.set("losses", losses);
        section.set("gamesPlayed", gamesPlayed);
        section.set("blocksBroken", blocksBroken);
        section.set("playerName", playerName);
    }
}
