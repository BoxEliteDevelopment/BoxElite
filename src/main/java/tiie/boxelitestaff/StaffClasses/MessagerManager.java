package tiie.boxelitestaff.StaffClasses;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import tiie.boxelitestaff.BoxEliteStaff;

public class MessagerManager {

    private final BoxEliteStaff plugin;
    private final MiniMessage miniMessage;

    public MessagerManager(BoxEliteStaff plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }


    public Component getActionBarMessage() {

        // Default gradient: blue (#4b6cb7) to purple (#8a2be2)
        String rawMsg = plugin.getConfig().getString("actionbar-message",
                "<gradient:#4b6cb7:#8a2be2>Staff Chat Mode: <color:#90ee90>ON");
        return miniMessage.deserialize(rawMsg);
    }


    public Component formatStaffChatMessage(String senderName, String message) {

        String raw = "<gradient:#4b6cb7:#8a2be2>[BoxElite SC] <dark_purple>" + senderName + ": <white>" + message;
        return miniMessage.deserialize(raw);
    }


    public Component formatToggleMessage(boolean enabled) {
        if (enabled) {
            return miniMessage.deserialize("<gradient:#4b6cb7:#8a2be2><bold>StaffChat:</bold></gradient> <gray>You have <green>enabled</green> StaffChat mode.");
        } else {
            return miniMessage.deserialize("<gradient:#4b6cb7:#8a2be2><bold>StaffChat:</bold></gradient> <gray>You have <red>disabled</red> StaffChat mode.");
        }
    }

    public Component getNoPermissionMessage() {
        // Red error message
        String rawMsg = "<gradient:#ff0000:#8b0000><bold>BoxElite</bold></gradient> <gray>|</gray> <red>You don’t have permission.";
        return miniMessage.deserialize(rawMsg);
    }
}
