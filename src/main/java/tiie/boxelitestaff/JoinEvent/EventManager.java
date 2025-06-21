package tiie.boxelitestaff.JoinEvent;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tiie.boxelitestaff.BoxEliteStaff;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EventManager {
    private final BoxEliteStaff plugin;
    private final Map<UUID, SavedInventory> savedInventories = new HashMap<>();
    private final Set<UUID> inEvent = new HashSet<>();

    private final File inventoryDir;

    public EventManager(BoxEliteStaff plugin) {
        this.plugin = plugin;
        this.inventoryDir = new File(plugin.getDataFolder(), "inventories");
        if (!inventoryDir.exists()) inventoryDir.mkdirs();
    }

    public boolean isInEvent(Player player) {
        return inEvent.contains(player.getUniqueId());
    }


    public void joinEvent(Player player) {
        UUID uuid = player.getUniqueId();
        if (isInEvent(player)) return;

        // Save to file
        SavedInventory inv = new SavedInventory(
                player.getInventory().getContents(),
                player.getInventory().getArmorContents(),
                player.getInventory().getExtraContents()
        );
        try {
            SavedInventory.saveToFile(SavedInventory.getFile(inventoryDir, uuid), inv);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save inventory for " + player.getName());
            return;
        }

        inEvent.add(uuid);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setExtraContents(null);



        Location eventLoc = getEventLocation();
        if (eventLoc != null) {
            player.teleport(eventLoc);
        }
        }


    public void leaveEvent(Player player) {
        UUID uuid = player.getUniqueId();

        if (!isInEvent(player)) return;

        File file = SavedInventory.getFile(inventoryDir, uuid);
        if (file.exists()) {
            SavedInventory inv = SavedInventory.loadFromFile(file);
            if (inv != null) {
                player.getInventory().setContents(inv.getContents());
                player.getInventory().setArmorContents(inv.getArmor());
                player.getInventory().setExtraContents(inv.getExtra());
            }
            file.delete();
        }

        inEvent.remove(uuid);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + player.getName());
    }

    public void restoreAllInventories() {
        File[] files = inventoryDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String name = file.getName().replace(".yml", "");
            try {
                UUID uuid = UUID.fromString(name);
                Player player = Bukkit.getPlayer(uuid);

                if (player != null && player.isOnline()) {
                    SavedInventory inv = SavedInventory.loadFromFile(file);
                    if (inv != null) {
                        player.getInventory().setContents(inv.getContents());
                        player.getInventory().setArmorContents(inv.getArmor());
                        player.getInventory().setExtraContents(inv.getExtra());

                        player.sendMessage("§a⚠ Your inventory was restored after a server crash.");
                    }

                    file.delete(); // Clean up
                    inEvent.remove(uuid); // optional cleanup
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to restore inventory for file: " + file.getName());
            }
        }
    }

    public Location getEventLocation() {
        if (!plugin.getConfig().isConfigurationSection("event-location")) return null;

        double x = plugin.getConfig().getDouble("event-location.x");
        double y = plugin.getConfig().getDouble("event-location.y");
        double z = plugin.getConfig().getDouble("event-location.z");
        float yaw = (float) plugin.getConfig().getDouble("event-location.yaw");
        float pitch = (float) plugin.getConfig().getDouble("event-location.pitch");
        String world = plugin.getConfig().getString("event-location.world");

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }


    private ItemStack createEventShovel() {
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        ItemMeta meta = shovel.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.EFFICIENCY, 5, true); // Efficiency V
            meta.setUnbreakable(true);
            meta.displayName(Component.text("§bEvent Shovel"));
            shovel.setItemMeta(meta);
        }
        return shovel;
    }
}
