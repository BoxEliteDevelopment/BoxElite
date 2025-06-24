package tiie.boxelitestaff.spleef;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.spleef.arena.SpleefArena;
import tiie.boxelitestaff.spleef.player.PlayerStats;
import tiie.boxelitestaff.spleef.player.StatsManager;
import tiie.boxelitestaff.spleef.session.ArenaSessionManager;
import tiie.boxelitestaff.spleef.session.ArenaSetupSession;
import tiie.boxelitestaff.spleef.session.GameSession;
import tiie.boxelitestaff.utils.PlayerCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpleefCommand extends PlayerCommand {

    private BoxEliteStaff plugin;

    public SpleefCommand(BoxEliteStaff plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean execute(Player player, Command cmd, String label, String[] args) {


        ArenaSessionManager setupManager = plugin.getSetupManager();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /spleef <create|pos1|pos2|setlobby|addspawn|save>");
            return true;
        }


        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /spleef create <name>");
                return true;
            }

            if (player.hasPermission("boxelite.spleef.event")) {
                String name = args[1].toLowerCase();
                setupManager.startSetupSession(player, name);
                player.sendMessage("§aStarted arena setup for: " + name);
            } else {
                player.sendMessage("§cYou don't have permission to create arenas.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            Player target = player;

            if (args.length >= 2) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found or not online.");
                    return true;
                }
            }

            PlayerStats stats = plugin.getStatsManager().getStats(target.getUniqueId());

            player.sendMessage("§8§m------------------------------");
            player.sendMessage("§eSpleef Stats for §a" + target.getName());
            player.sendMessage("§7• §fGames Played: §b" + stats.getGamesPlayed());
            player.sendMessage("§7• §fWins: §a" + stats.getWins());
            player.sendMessage("§7• §fLosses: §c" + stats.getLosses());
            player.sendMessage("§7• §fBlocks Broken: §6" + stats.getBlocksBroken());
            player.sendMessage("§8§m------------------------------");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("top")) {
            StatsManager statsManager = BoxEliteStaff.getInstance().getStatsManager();

            // Get top 10 players by wins (adjust to your stat)
            List<Map.Entry<String, Integer>> topPlayers = statsManager.getTopPlayersByWins(10);

            player.sendMessage("§6§lSpleef Leaderboard - Top 10 Wins");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topPlayers) {
                player.sendMessage("§e#" + rank + " §a" + entry.getKey() + " §7- §f" + entry.getValue() + " wins");
                rank++;
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("resetstats")) {
            if (!player.hasPermission("spleef.admin")) {
                player.sendMessage("§cYou don't have permission to do that.");
                return true;
            }

            String targetName = args[1];
            StatsManager statsManager = BoxEliteStaff.getInstance().getStatsManager();

            if (statsManager.resetPlayerStats(targetName)) {
                player.sendMessage("§aStats reset for player §e" + targetName);
            } else {
                player.sendMessage("§cPlayer §e" + targetName + " §cnot found.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("spectate")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /spleef spectate <arena>");
                return true;
            }

            String arenaName = args[1].toLowerCase();
            GameSession session = plugin.getSetupManager().getGameSession(arenaName);

            if (session == null) {
                player.sendMessage("§cThat arena is not running a game.");
                return true;
            }

            if (plugin.getSpleefGameListener().isSpectator(player)) {
                player.sendMessage("§cYou are already spectating.");
                return true;
            }

            // Spectate
            plugin.getSpleefGameListener().addSpectator(player);
            player.getInventory().clear();
            player.setGameMode(org.bukkit.GameMode.SPECTATOR);
            player.teleport(session.getArena().getSpectatorSpawn());
            player.sendMessage("§bYou are now spectating the game in §e" + arenaName);
            return true;
        }

        if (args[0].equalsIgnoreCase("join")) {
            plugin.getQueueManager().addPlayer(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("leave")) {
            plugin.getQueueManager().removePlayer(player);

            // Then check if they're in an active game
            GameSession session = plugin.getSetupManager().getSessionByPlayer(player.getUniqueId());
            if (session != null) {
                session.forceEliminate(player.getUniqueId());
                player.teleport(session.getArena().getLobbySpawn());
                player.sendMessage("§cYou have left the Spleef game.");
            }

            if (plugin.getSpleefGameListener().isSpectator(player)) {
                plugin.getSpleefGameListener().removeSpectator(player);
                if (session != null){
                    player.teleport(session.getArena().getLobbySpawn());
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage("§eYou have stopped spectating.");
                    return true;
                }else {
                    plugin.getLogger();
                }

            }
            return true;
        }


        if (args[0].equalsIgnoreCase("start")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /spleef start <arena>");
                return true;
            }

            String arenaName = args[1].toLowerCase();
            SpleefArena arena = plugin.getArenaManager().getArena(arenaName);

            if (arena == null) {
                player.sendMessage("§cArena not found.");
                return true;
            }

            if (!arena.isAvailable()) {
                player.sendMessage("§cThat arena is currently in use.");
                return true;
            }

            // You can replace this with actual player gathering logic
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers()).stream()
                    .filter(p -> p.getWorld().equals(arena.getWorld()))
                    .limit(arena.getMaxPlayers()) // optional cap
                    .collect(Collectors.toList());

            if (players.size() < arena.getMinPlayers()) {
                player.sendMessage("§cNot enough players to start the game.");
                return true;
            }

            GameSession session = new GameSession(arena, players, plugin.getArenaManager());
            plugin.getArenaManager().registerSession(arenaName, session);
            session.start();
            return true;
        }

// Everything below this point requires an active session
        ArenaSetupSession session = setupManager.getSetupSession(player);
        if (session == null) {
            player.sendMessage("§cYou're not in an arena setup session. Use /spleef create <name> first.");
            return true;
        }

        Location loc = player.getLocation();

        switch (args[0].toLowerCase()) {
            case "pos1":
                session.setPos1(loc);
                player.sendMessage("§aPosition 1 set at your location.");
                break;

            case "pos2":
                session.setPos2(loc);
                player.sendMessage("§aPosition 2 set at your location.");
                break;

            case "setlobby":
                session.setLobby(loc);
                player.sendMessage("§aLobby spawn set.");
                break;

            case "addspawn":
                session.getSpawnPoints().add(loc);
                player.sendMessage("§aAdded spawn point #" + session.getSpawnPoints().size());
                break;

            case "save":
                if (session.getPos1() == null || session.getPos2() == null || session.getLobby() == null || session.getSpawnPoints().isEmpty()) {
                    player.sendMessage("§cYou must set pos1, pos2, lobby, and at least 1 spawn point.");
                    return true;
                }

                File file = new File(plugin.getDataFolder(), "arenas.yml");
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

                String base = "arenas." + session.getArenaName();
                cfg.set(base + ".world", session.getPos1().getWorld().getName());
                cfg.set(base + ".lobbySpawn", serializeLocation(session.getLobby()));

                List<Map<String, Object>> spawnList = session.getSpawnPoints().stream()
                        .map(this::serializeLocation)
                        .collect(Collectors.toList());
                cfg.set(base + ".gameSpawns", spawnList);
                cfg.set(base + ".region.pos1", serializeLocation(session.getPos1()));
                cfg.set(base + ".region.pos2", serializeLocation(session.getPos2()));
                cfg.set(base + ".minPlayers", 2);
                cfg.set(base + ".maxPlayers", 8);

                try {
                    cfg.save(file);
                    setupManager.endSetupSession(player);
                    player.sendMessage("§aArena saved and setup complete.");
                } catch (IOException e) {
                    e.printStackTrace();
                    player.sendMessage("§cFailed to save arena!");
                }
                break;

            default:
                player.sendMessage("§cUnknown subcommand.");
        }

        return true;
    }

    private Map<String, Object> serializeLocation(Location loc) {
        Map<String, Object> map = new HashMap<>();
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", loc.getYaw());
        map.put("pitch", loc.getPitch());
        return map;
    }
}
