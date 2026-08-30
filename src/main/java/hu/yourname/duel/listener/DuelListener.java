package hu.yourname.duel.listener;

import hu.yourname.duel.DuelPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class DuelListener implements Listener {
    
    private final DuelPlugin plugin;
    
    public DuelListener(DuelPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (plugin.getDuelManager().isInCountdown(player)) {
            if (e.getFrom().getX() != e.getTo().getX() || 
                e.getFrom().getY() != e.getTo().getY() || 
                e.getFrom().getZ() != e.getTo().getZ()) {
                e.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().getPrefixed("cant-move-countdown"));
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getDuelManager().isInDuel(player) && !plugin.getDuelManager().isInCountdown(player)) {
            return;
        }
        
        String fullCmd = e.getMessage().toLowerCase();
        String baseCmd = fullCmd.split(" ")[0];
        
        // Duel parancsok mindig engedélyezettek
        if (baseCmd.equals("/duel") || baseCmd.equals("/d")) {
            return;
        }
        
        List<String> whitelist = plugin.getConfig().getStringList("command-whitelist");
        for (String allowed : whitelist) {
            if (baseCmd.equals("/" + allowed.toLowerCase())) {
                return;
            }
        }
        
        e.setCancelled(true);
        player.sendMessage(plugin.getMessageManager().getPrefixed("command-blocked"));
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        
        // Queue-ból kilépés
        if (plugin.getQueueManager().isInQueue(player)) {
            plugin.getQueueManager().removeFromQueue(player.getUniqueId());
        }
        
        // Duel vagy countdown kezelése
        if (plugin.getDuelManager().isInDuel(player) || plugin.getDuelManager().isInCountdown(player)) {
            plugin.getDuelManager().handleQuit(player);
        }
    }
}