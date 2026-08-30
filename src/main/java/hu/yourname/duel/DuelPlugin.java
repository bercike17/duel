package hu.yourname.duel;

import hu.yourname.duel.arena.ArenaManager;
import hu.yourname.duel.command.DuelCommand;
import hu.yourname.duel.gui.GuiManager;
import hu.yourname.duel.kit.KitManager;
import hu.yourname.duel.listener.DuelListener;
import hu.yourname.duel.listener.NpcListener;
import hu.yourname.duel.manager.DuelManager;
import hu.yourname.duel.message.MessageManager;
import hu.yourname.duel.mode.DuelModeManager;
import hu.yourname.duel.queue.QueueManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelPlugin extends JavaPlugin {
    
    private static DuelPlugin instance;
    private ArenaManager arenaManager;
    private DuelModeManager modeManager;
    private DuelManager duelManager;
    private GuiManager guiManager;
    private KitManager kitManager;
    private MessageManager messageManager;
    private Location lobbyLocation;
    private QueueManager queueManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        this.messageManager = new MessageManager(this);
        this.arenaManager = new ArenaManager(this);
        this.modeManager = new DuelModeManager(this);
        this.kitManager = new KitManager(this);
        this.duelManager = new DuelManager(this);
        this.guiManager = new GuiManager(this);
        this.queueManager = new QueueManager(this);

        loadLobbyLocation();
        
        getCommand("duel").setExecutor(new DuelCommand(this));
        getServer().getPluginManager().registerEvents(new NpcListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        
        getLogger().info("DuelPlugin sikeresen betoltve! (v1.0.0 - Kit + Countdown + Messages)");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("DuelPlugin kikapcsolva!");
    }
    
    private void loadLobbyLocation() {
        if (getConfig().contains("lobby-location")) {
            lobbyLocation = (Location) getConfig().get("lobby-location");
        }
    }
    
    public void setLobbyLocation(Location loc) {
        this.lobbyLocation = loc;
        getConfig().set("lobby-location", loc);
        saveConfig();
    }
    
    public Location getLobbyLocation() {
        return lobbyLocation;
    }
    
    public static DuelPlugin getInstance() {
        return instance;
    }
    
    public ArenaManager getArenaManager() { return arenaManager; }
    public DuelModeManager getModeManager() { return modeManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public KitManager getKitManager() { return kitManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public QueueManager getQueueManager() { return queueManager; }
}