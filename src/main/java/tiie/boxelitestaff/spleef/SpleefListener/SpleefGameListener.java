package tiie.boxelitestaff.spleef.SpleefListener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.spleef.Arena.SpleefArena;
import tiie.boxelitestaff.spleef.Session.GameSession;

import java.net.http.WebSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpleefGameListener implements Listener {

    //cleaner

    private final Set<UUID> spleefPlayers = new HashSet<>();

    public Set<UUID> getSpectators() {
        return spectators;
    }



    private final Set<UUID> spectators = new HashSet<>();

    public void addPlayer(Player player) {
        spleefPlayers.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        spleefPlayers.remove(player.getUniqueId());
    }

    public void clearAll() {
        spleefPlayers.clear();
    }

    public void clearSpectators() {
        spectators.clear();
    }

    public boolean isInSpleefGame(Player player) {
        return spleefPlayers.contains(player.getUniqueId());
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInSpleefGame(player)) return;

        // Only allow breaking snow blocks
        if (event.getBlock().getType() != Material.SNOW_BLOCK) {
            event.setCancelled(true);
            return;
        }

        // Prevent drops
        event.setDropItems(false);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isInSpleefGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (isInSpleefGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isInSpleefGame(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (isInSpleefGame(player)) {
            event.setKeepInventory(true);
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isInSpleefGame(player) && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isInSpleefGame(player)) {
            event.setCancelled(true);
        }
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreakSpec(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isSpectator(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreakCreative(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Only allow active spleef players to break inside arenas
        if (!spleefPlayers.contains(player.getUniqueId()) && player.getGameMode() != GameMode.SPECTATOR) {
            for (SpleefArena arena : BoxEliteStaff.getInstance().getArenaManager().getAllArenas()) {
                if (arena.getRegion().contains(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakStat(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isInSpleefGame(player)) return;
        if (event.getBlock().getType() != Material.SNOW_BLOCK) {
            event.setCancelled(true);
            return;
        }

        BoxEliteStaff.getInstance()
                .getStatsManager()
                .getStats(player.getUniqueId())
                .addBlocksBroken(1);

        event.setDropItems(false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Remove from game players
        if (spleefPlayers.contains(uuid)) {
            spleefPlayers.remove(uuid);
            GameSession session = BoxEliteStaff.getInstance().getSetupManager().getSessionByPlayer(uuid);
            if (session != null) {
                session.forceEliminate(uuid); // You’d need to implement this
            }
        }

        // Remove from spectators
        if (spectators.contains(uuid)) {
            spectators.remove(uuid);
        }
    }


}
