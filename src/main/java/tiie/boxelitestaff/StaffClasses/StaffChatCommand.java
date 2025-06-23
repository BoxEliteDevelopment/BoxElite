package tiie.boxelitestaff.StaffClasses;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tiie.boxelitestaff.BoxEliteStaff;
import tiie.boxelitestaff.utils.PlayerCommand;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class StaffChatCommand extends PlayerCommand {
    private final StaffChatManager staffChatManager;
    private final MessagerManager messageManager;

    public StaffChatCommand( StaffChatManager staffChatManager, MessagerManager messageManager) {

        this.staffChatManager = staffChatManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean execute(Player player, Command command, String label, String[] args) {
        if (!player.hasPermission("boxelite.staffchat")){
            messageManager.getNoPermissionMessage();
            return false;
        }
        if (args.length == 0) {
            toggleStaffChatMode(player);
        } else {
            sendOneTimeStaffChatMessage(player, args);
        }
            return true;
    }
    private void sendStaffChatMessage(Player sender, String message) {
        Component formatted = messageManager.formatStaffChatMessage(sender.getName(), message);
        Set<Player> recipients = staffChatManager.getToggledPlayers().stream()
                .filter(p -> p.hasPermission("boxelite.staffchat.view"))
                .collect(Collectors.toSet());
        if (!staffChatManager.isToggled(sender) && sender.hasPermission("boxelite.staffchat.view")) {
            recipients.add(sender);
        }
        recipients.forEach(p -> p.sendMessage(formatted));
    }
    private void toggleStaffChatMode(Player player) {
        boolean enabled = staffChatManager.togglePlayer(player);
        player.sendMessage(messageManager.formatToggleMessage(enabled));
        staffChatManager.saveToggledPlayers();
    }
    private void sendOneTimeStaffChatMessage(Player sender, String[] args) {
        String message = String.join(" ", args);
        sendStaffChatMessage(sender, message);
    }
}
