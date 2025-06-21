package tiie.boxelitestaff.StaffClasses;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import tiie.boxelitestaff.BoxEliteStaff;

import java.util.*;
import java.util.stream.Collectors;

public class StaffChatManager {

    private final BoxEliteStaff plugin;
    private final Set<UUID> toggledPlayers = new HashSet<>();
    private BukkitTask actionBarTask;

    public StaffChatManager(BoxEliteStaff plugin) {
        this.plugin = plugin;
    }

    /**
     * Load toggled players from config file list "toggled-players"
     */
    public void loadToggledPlayers() {
        FileConfiguration config = plugin.getConfig();
        List<String> uuidStrings = config.getStringList("toggled-players");
        for (String uuidStr : uuidStrings) {
            try {
                toggledPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in toggled-players config: " + uuidStr);
            }
        }
    }


    public void saveToggledPlayers() {
        List<String> uuidStrings = toggledPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        plugin.getConfig().set("toggled-players", uuidStrings);
        plugin.saveConfig();
    }


    public boolean isToggled(Player player) {
        return toggledPlayers.contains(player.getUniqueId());
    }


    public boolean togglePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (toggledPlayers.contains(uuid)) {
            toggledPlayers.remove(uuid);
            return false;
        } else {
            toggledPlayers.add(uuid);
            return true;
        }
    }


    public Collection<Player> getToggledPlayers() {
        return toggledPlayers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }


    public void startActionBarTask() {
        stopActionBarTask(); // Cancel existing if running

        long interval = plugin.getConfig().getLong("actionbar-loop-time", 100L);
        MessagerManager messageManager = ((BoxEliteStaff) plugin).getMessageManager();

        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Component actionBarMsg = messageManager.getActionBarMessage();
            for (Player player : getToggledPlayers()) {
                player.sendActionBar(actionBarMsg);
            }
        }, 0L, interval);
    }


    public void stopActionBarTask() {
        if (actionBarTask != null && !actionBarTask.isCancelled()) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }
}
