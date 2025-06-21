package tiie.boxelitestaff.StaffClasses;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class StaffChatListener implements Listener {

    private final StaffChatManager staffChatManager;
    private final MessagerManager messageManager;

    public StaffChatListener(StaffChatManager staffChatManager, MessagerManager messageManager) {
        this.staffChatManager = staffChatManager;
        this.messageManager = messageManager;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (staffChatManager.isToggled(player) && player.hasPermission("staffchat.use")) {
            event.setCancelled(true);  // Cancel normal chat


            Component staffMessage = messageManager.formatStaffChatMessage(player.getName(), event.getMessage());

            player.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("staffchat.view"))
                    .forEach(p -> p.sendMessage(staffMessage));
        }
    }
}
