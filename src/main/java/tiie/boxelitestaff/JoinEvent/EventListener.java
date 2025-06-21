package tiie.boxelitestaff.JoinEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import tiie.boxelitestaff.BoxEliteStaff;

public class EventListener implements Listener {

    private final BoxEliteStaff plugin;
    private final EventManager eventManager;

    public EventListener(BoxEliteStaff plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (eventManager.isInEvent(player)) {
            eventManager.leaveEvent(player);
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (eventManager.isInEvent(player)) {
            eventManager.leaveEvent(player);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (eventManager.isInEvent(player)) {
            // If player is teleporting OUT of the event world, remove them
            if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
                eventManager.leaveEvent(player);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (eventManager.isInEvent(player)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                eventManager.leaveEvent(player);
            });
        }
    }
}
