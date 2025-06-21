package tiie.boxelitestaff;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tiie.boxelitestaff.JoinEvent.EventCommand;
import tiie.boxelitestaff.JoinEvent.EventListener;
import tiie.boxelitestaff.JoinEvent.EventManager;
import tiie.boxelitestaff.StaffClasses.MessagerManager;
import tiie.boxelitestaff.StaffClasses.StaffChatCommand;
import tiie.boxelitestaff.StaffClasses.StaffChatListener;
import tiie.boxelitestaff.StaffClasses.StaffChatManager;

public final class BoxEliteStaff extends JavaPlugin {

    private StaffChatManager staffChatManager;
    private MessagerManager messageManager;
    private StaffChatCommand staffChatCommand;


    private EventManager eventManager;
    @Override
    public void onEnable() {

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
}
