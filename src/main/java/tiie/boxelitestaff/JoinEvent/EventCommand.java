package tiie.boxelitestaff.JoinEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tiie.boxelitestaff.BoxEliteStaff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand implements CommandExecutor, TabCompleter {


    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final BoxEliteStaff plugin;
    private final EventManager eventManager;

    public EventCommand(BoxEliteStaff plugin, EventManager eventManager) {
        this.plugin = plugin;
        this.eventManager = eventManager;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }



        if (args.length == 1 && args[0].equalsIgnoreCase("setlocation")) {
            if (player.hasPermission("boxelite.event.setlocation")) {


                plugin.getConfig().set("event-location.world", player.getWorld().getName());
                plugin.getConfig().set("event-location.x", player.getLocation().getX());
                plugin.getConfig().set("event-location.y", player.getLocation().getY());
                plugin.getConfig().set("event-location.z", player.getLocation().getZ());
                plugin.getConfig().set("event-location.yaw", player.getLocation().getYaw());
                plugin.getConfig().set("event-location.pitch", player.getLocation().getPitch());
                plugin.saveConfig();
                player.sendMessage("§aEvent location set.");
                return true;
            }else {
                player.sendMessage(getNoPermissionMessage());
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("restoreall")) {
            if (player.hasPermission("boxelite.event.restoreall")) {
                eventManager.restoreAllInventories();
                player.sendMessage(getRestoreAllMessage());
            } else {
                player.sendMessage(getNoPermissionMessage());
            }
            return true;
        }

        if (eventManager.isInEvent(player)) {
            player.sendMessage(getAlreadyInEventMessage());
            return true;
        }

        if (player.hasPermission("boxelite.event.join")) {
            eventManager.joinEvent(player);
            player.sendMessage(getJoinMessage());
            return true;
        } else {
            player.sendMessage(getNoPermissionMessage());
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            // First argument tab complete
            //make more advanced
            List<String> options = Arrays.asList("setlocation", "restoreall");
            List<String> result = new ArrayList<>();
            String arg = args[0].toLowerCase();

            for (String option : options) {
                if (option.startsWith(arg)) {
                    result.add(option);
                }
            }
            return result;
        }

        // No further tab completions for now
        return new ArrayList<>();
    }


    //TODO don't make dup methods.


    public Component getJoinMessage() {
        // Gold-ish gradient for joined message
        String rawMsg = "<gradient:#ffd700:#ffa500><bold>BoxElite</bold></gradient> <gray>|</gray> <gold>You have joined the event!";
        return miniMessage.deserialize(rawMsg);
    }

    public Component getAlreadyInEventMessage() {
        // Red gradient for error message
        String rawMsg = "<gradient:#ff0000:#8b0000><bold>BoxElite</bold></gradient> <gray>|</gray> <red>You are already in the event.";
        return miniMessage.deserialize(rawMsg);
    }

    public Component getRestoreAllMessage() {
        // Green gradient success message
        String rawMsg = "<gradient:#00ff00:#008000><bold>BoxElite</bold></gradient> <gray>|</gray> <green>Restored all saved inventories.";
        return miniMessage.deserialize(rawMsg);
    }

    public Component getNoPermissionMessage() {
        // Red error message
        String rawMsg = "<gradient:#ff0000:#8b0000><bold>BoxElite</bold></gradient> <gray>|</gray> <red>You don’t have permission.";
        return miniMessage.deserialize(rawMsg);
    }

}
