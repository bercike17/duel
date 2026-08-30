package hu.yourname.duel.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    
    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void loadMessages() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("messages")) {
            setDefaults();
        }
        
        messages.clear();
        if (config.contains("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(true)) {
                String msg = config.getString("messages." + key, "&cMissing message: " + key);
                messages.put(key, ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
    }
    
    private void setDefaults() {
        plugin.getConfig().set("messages.prefix", "&6[Duel] &r");
        plugin.getConfig().set("messages.lobby-not-set", "&cA duel lobby nincs beallitva!");
        plugin.getConfig().set("messages.teleported-lobby", "&aTeleportálva a duel lobbyba!");
        plugin.getConfig().set("messages.player-not-found", "&cA jatekos nem elerheto!");
        plugin.getConfig().set("messages.cant-duel-self", "&cMagadat nem tudod kihivni!");
        plugin.getConfig().set("messages.no-permission", "&cNincs jogod!");
        plugin.getConfig().set("messages.lobby-set", "&aDuel lobby beallitva!");
        plugin.getConfig().set("messages.npc-set", "&aNPC helye beallitva!");
        plugin.getConfig().set("messages.config-reloaded", "&aKonfiguracio ujratoltve!");
        plugin.getConfig().set("messages.arena-created", "&aPalya &e%arena% &alétrehozva &e%mode% &amódhoz!");
        plugin.getConfig().set("messages.mode-created", "&aDuel mod letrehozva: &e%mode%");
        plugin.getConfig().set("messages.kit-saved", "&aKit elmentve a(z) &e%mode% &amódhoz!");
        plugin.getConfig().set("messages.duel-request-sent", "&aKihivas elkuldve &e%target% &ajatekosnak!");
        plugin.getConfig().set("messages.duel-request-received", "&e%challenger% &akihivott egy duelre!");
        plugin.getConfig().set("messages.duel-request-info", "&7Mod: &f%mode% &7| Palya: &f%arena%");
        plugin.getConfig().set("messages.duel-accept-command", "&e/duel accept &7vagy &c/duel deny");
        plugin.getConfig().set("messages.duel-expired", "&cA kihivas lejart.");
        plugin.getConfig().set("messages.no-pending-request", "&cNincs fuggo kihivasod!");
        plugin.getConfig().set("messages.challenger-offline", "&cA kihivo jatekos offline!");
        plugin.getConfig().set("messages.arena-not-found", "&cA palya nem talalhato!");
        plugin.getConfig().set("messages.duel-denied", "&cElutasitottad a kihivast.");
        plugin.getConfig().set("messages.duel-denied-target", "&e%target% &celutasitotta a kihivast.");
        plugin.getConfig().set("messages.duel-in-progress", "&cValamelyikotok mar duelben van!");
        plugin.getConfig().set("messages.duel-pending", "&cA jatekosnak mar van fuggo kihivasa!");
        plugin.getConfig().set("messages.duel-started", "&2&lDuel elkezdodott! &7Mod: &f%mode% &7| Palya: &f%arena%");
        plugin.getConfig().set("messages.duel-won", "&6&lGyoztel!");
        plugin.getConfig().set("messages.duel-lost", "&c&lVesztettel!");
        plugin.getConfig().set("messages.countdown-title", "&a&l%seconds%");
        plugin.getConfig().set("messages.countdown-subtitle", "&7A duel hamarosan kezdodik...");
        plugin.getConfig().set("messages.cant-move-countdown", "&cNem mozoghatsz a visszaszamlalas alatt!");
        plugin.getConfig().set("messages.select-mode-first", "&cElobb valassz modot!");
        plugin.getConfig().set("messages.select-mode-arena", "&cValassz modot es palyat!");
        plugin.getConfig().set("messages.mode-selected", "&aMod kivalasztva: &e%mode%");
        plugin.getConfig().set("messages.arena-selected", "&aPalya kivalasztva: &e%arena%");
        plugin.getConfig().set("messages.random-mode", "&aVeletlenszeru mod: &e%mode%");
        plugin.getConfig().set("messages.random-arena", "&aVeletlenszeru palya: &e%arena%");
        plugin.getConfig().set("messages.opponent-quit-countdown", "&cAz ellenfeled kilépett a visszaszámlálás alatt!");
        plugin.getConfig().set("messages.command-blocked", "&cEzt a parancsot nem hasznalhatod duel kozben!");
        plugin.getConfig().set("messages.queue-joined", "&aBeleptel a queue-ba: &e%mode% - %arena%");
        plugin.getConfig().set("messages.queue-left", "&cKileptel a queue-bol.");
        plugin.getConfig().set("messages.not-in-queue", "&cNem vagy queue-ban.");
        plugin.getConfig().set("messages.queue-match-found", "&aEllenfel talalva: &e%opponent%");
        plugin.getConfig().set("messages.already-in-duel", "&cMar duelben vagy queue-ban vagy!");
        plugin.saveConfig();
    }
    
    public String get(String key) {
        return messages.getOrDefault(key, "&cMissing: " + key);
    }
    
    public String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return msg;
    }
    
    public String getPrefixed(String key) {
        return get("prefix") + get(key);
    }
    
    public String getPrefixed(String key, Map<String, String> placeholders) {
        return get("prefix") + get(key, placeholders);
    }
}
