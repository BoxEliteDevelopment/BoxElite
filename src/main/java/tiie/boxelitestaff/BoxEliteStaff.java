package tiie.boxelitestaff;

import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tiie.boxelitestaff.JoinEvent.EventCommand;
import tiie.boxelitestaff.JoinEvent.EventListener;
import tiie.boxelitestaff.JoinEvent.EventManager;
import tiie.boxelitestaff.StaffClasses.MessagerManager;
import tiie.boxelitestaff.StaffClasses.StaffChatCommand;
import tiie.boxelitestaff.StaffClasses.StaffChatListener;
import tiie.boxelitestaff.StaffClasses.StaffChatManager;

public final class BoxEliteStaff extends JavaPlugin {

    private String updateVerison = "v1.1";

    private StaffChatManager staffChatManager;
    private MessagerManager messageManager;
    private StaffChatCommand staffChatCommand;


    private EventManager eventManager;
    @Override
    public void onEnable() {

        sendPluginUpdateMessage();



        //TODO make main more clean

        saveDefaultConfig();


        this.messageManager = new MessagerManager(this);
        this.staffChatManager = new StaffChatManager(this);
        this.staffChatManager.loadToggledPlayers();

        // Register command
        this.staffChatCommand = new StaffChatCommand(this, staffChatManager, messageManager);
        getCommand("staffchat").setExecutor(staffChatCommand);


        staffChatManager.startActionBarTask();

        getServer().getPluginManager().registerEvents(
                new StaffChatListener(staffChatManager, messageManager),
                this
        );

        this.eventManager = new EventManager(this);

        getCommand("event").setExecutor(new EventCommand(this, eventManager));
        getCommand("event").setTabCompleter(new EventCommand(this, eventManager));
        Bukkit.getPluginManager().registerEvents(new EventListener(this, eventManager), this);


        getLogger().info("StaffChatPlugin enabled!");


    }

    @Override
    public void onDisable() {
        staffChatManager.saveToggledPlayers();
        staffChatManager.stopActionBarTask();
        eventManager.restoreAllInventories();
        getLogger().info("StaffChatPlugin disabled!");
    }
    public StaffChatManager getStaffChatManager() {
        return staffChatManager;
    }

    public MessagerManager getMessageManager() {
        return messageManager;
    }

    public void sendPluginUpdateMessage(){
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(ChatColor.GOLD + "BoxElite " + ChatColor.GRAY +
                            "plugin running version.. " + ChatColor.GREEN + updateVerison);
                }
            }
        }, 100L); // 100 ticks = 5 seconds
    }
}
