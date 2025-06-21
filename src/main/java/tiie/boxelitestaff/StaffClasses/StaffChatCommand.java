package tiie.boxelitestaff.StaffClasses;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tiie.boxelitestaff.BoxEliteStaff;

import java.util.Collection;

public class StaffChatCommand implements CommandExecutor {

    private final BoxEliteStaff plugin;
    private final StaffChatManager staffChatManager;
    private final MessagerManager messageManager;

    public StaffChatCommand(BoxEliteStaff plugin, StaffChatManager staffChatManager, MessagerManager messageManager) {
        this.plugin = plugin;
        this.staffChatManager = staffChatManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command."));
            return true;
        }

        if (!player.hasPermission("boxelite.staffchat")){
            messageManager.getNoPermissionMessage();
            return false;
        }


            if (args.length == 0) {
                // Toggle staff chat mode
                boolean enabled = staffChatManager.togglePlayer(player);
                player.sendMessage(messageManager.formatToggleMessage(enabled));
                staffChatManager.saveToggledPlayers();
            } else {
                // Send a one-time message to staff chat
                String message = String.join(" ", args);
                sendStaffChatMessage(player, message);
            }
            return true;



    }


    private void sendStaffChatMessage(Player sender, String message) {
        Component formattedMessage = messageManager.formatStaffChatMessage(sender.getName(), message);

        Collection<Player> recipients = staffChatManager.getToggledPlayers();

        // Also send to sender if they are not toggled
        if (!staffChatManager.isToggled(sender)) {
            recipients = new java.util.ArrayList<>(recipients);
            ((java.util.List<Player>) recipients).add(sender);
        }

        for (Player p : recipients) {
            if (p.hasPermission("boxelite.staffchat.view")) {
                p.sendMessage(formattedMessage);
            }
        }
    }


}
