package tiie.boxelitestaff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import tiie.boxelitestaff.JoinEvent.EventCommand;
import tiie.boxelitestaff.JoinEvent.EventListener;
import tiie.boxelitestaff.JoinEvent.EventManager;
import tiie.boxelitestaff.StaffClasses.MessagerManager;
import tiie.boxelitestaff.StaffClasses.StaffChatCommand;
import tiie.boxelitestaff.StaffClasses.StaffChatListener;
import tiie.boxelitestaff.StaffClasses.StaffChatManager;
import tiie.boxelitestaff.spleef.Arena.ArenaManager;
import tiie.boxelitestaff.spleef.Session.ArenaSessionManager;
import tiie.boxelitestaff.spleef.SpleefCommand;
import tiie.boxelitestaff.spleef.SpleefListener.SpleefGameListener;

import java.io.File;

public final class BoxEliteStaff extends JavaPlugin {

    private static final String VERSION = "v1.1";

    public static BoxEliteStaff instance;

    private StaffChatManager staffChatManager;
    private MessagerManager messageManager;
    private EventManager eventManager;
    private ArenaManager arenaManager;
    private ArenaSessionManager setupSessionArena;

    private SpleefGameListener spleefGameListener;

    @Override
    public void onEnable() {

        arenaManager = new ArenaManager();

        FileConfiguration arenasConfig = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "arenas.yml")
        );



        arenaManager.loadArenas(arenasConfig);
        this.setupSessionArena = new ArenaSessionManager();

        this.spleefGameListener = new SpleefGameListener();

        instance = this;


        saveDefaultConfig();
        initManagers();
        registerCommands();
        registerListeners();
        scheduleStartupMessage();

        getLogger().info("BoxEliteStaff enabled, version: " + VERSION);




    }

    @Override
    public void onDisable() {
        staffChatManager.saveToggledPlayers();
        staffChatManager.stopActionBarTask();
        eventManager.restoreAllInventories();

        getLogger().info("BoxEliteStaff disabled.");
    }

    private void initManagers() {
        this.messageManager = new MessagerManager(this);
        this.staffChatManager = new StaffChatManager(this);
        this.eventManager = new EventManager(this);

        this.staffChatManager.loadToggledPlayers();
        this.staffChatManager.startActionBarTask();


    }

    public static BoxEliteStaff getInstance() {
        return instance;
    }

    private void registerCommands() {
        getCommand("staffchat").setExecutor(new StaffChatCommand(staffChatManager, messageManager));

        EventCommand eventCommand = new EventCommand(this, eventManager);
        getCommand("event").setExecutor(eventCommand);
        getCommand("event").setTabCompleter(eventCommand);

        getCommand("spleef").setExecutor(new SpleefCommand(this));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new StaffChatListener(staffChatManager, messageManager), this);
        pm.registerEvents(new EventListener(this, eventManager), this);



        if (spleefGameListener != null) {
            pm.registerEvents(spleefGameListener, this);
        } else {
            getLogger().warning("spleefGameListener is null and was not registered!");
        }
    }

    private void scheduleStartupMessage() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(ChatColor.GOLD + "BoxElite " + ChatColor.GRAY +
                            "plugin running version.. " + ChatColor.GREEN + VERSION);
                }
            }
        }, 100L);
    }

    public SpleefGameListener getSpleefGameListener() {
        return spleefGameListener;
    }

    public ArenaManager getArenaManager(){
        return arenaManager;
    }

    public ArenaSessionManager getSetupManager() {
        return setupSessionArena;
    }

    public StaffChatManager getStaffChatManager() {
        return staffChatManager;
    }

    public MessagerManager getMessageManager() {
        return messageManager;
    }
}
