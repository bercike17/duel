package hu.yourname.duel.queue;

import hu.yourname.duel.DuelPlugin;
import hu.yourname.duel.mode.DuelMode;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {
    
    private final DuelPlugin plugin;
    private final Map<String, Map<String, Queue<UUID>>> queues = new HashMap<>();
    private final Map<UUID, QueueEntry> playerQueue = new HashMap<>();
    
    public QueueManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void joinQueue(Player player, String mode, String arena) {
        if (playerQueue.containsKey(player.getUniqueId())) {
            leaveQueue(player);
        }
        
        if (plugin.getDuelManager().isInDuel(player) || plugin.getDuelManager().isInCountdown(player)) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("already-in-duel"));
            return;
        }
        
        queues.putIfAbsent(mode, new HashMap<>());
        queues.get(mode).putIfAbsent(arena, new LinkedList<>());
        
        Queue<UUID> arenaQueue = queues.get(mode).get(arena);
        
        if (!arenaQueue.isEmpty()) {
            UUID opponentId = arenaQueue.poll();
            Player opponent = plugin.getServer().getPlayer(opponentId);
            
            if (opponent != null && opponent.isOnline()) {
                playerQueue.remove(opponentId);
                
                Map<String, String> ph = new HashMap<>();
                ph.put("opponent", opponent.getName());
                player.sendMessage(plugin.getMessageManager().getPrefixed("queue-match-found", ph));
                
                ph.put("opponent", player.getName());
                opponent.sendMessage(plugin.getMessageManager().getPrefixed("queue-match-found", ph));
                
                plugin.getDuelManager().startMatch(opponent, player, mode, arena);
                return;
            }
        }
        
        arenaQueue.add(player.getUniqueId());
        playerQueue.put(player.getUniqueId(), new QueueEntry(mode, arena));
        
        Map<String, String> ph = new HashMap<>();
        ph.put("mode", mode);
        ph.put("arena", arena);
        player.sendMessage(plugin.getMessageManager().getPrefixed("queue-joined", ph));
    }
    
    public void leaveQueue(Player player) {
        UUID uuid = player.getUniqueId();
        QueueEntry entry = playerQueue.remove(uuid);
        if (entry == null) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("not-in-queue"));
            return;
        }
        
        Map<String, Queue<UUID>> arenaQueues = queues.get(entry.mode);
        if (arenaQueues != null) {
            Queue<UUID> queue = arenaQueues.get(entry.arena);
            if (queue != null) {
                queue.remove(uuid);
            }
        }
        
        player.sendMessage(plugin.getMessageManager().getPrefixed("queue-left"));
    }
    
    public boolean isInQueue(Player player) {
        return playerQueue.containsKey(player.getUniqueId());
    }
    
    public int getQueueCount(String mode, String arena) {
        Map<String, Queue<UUID>> modeQueues = queues.get(mode);
        if (modeQueues == null) return 0;
        Queue<UUID> queue = modeQueues.get(arena);
        return queue != null ? queue.size() : 0;
    }
    
    public void removeFromQueue(UUID uuid) {
        QueueEntry entry = playerQueue.remove(uuid);
        if (entry == null) return;
        
        Map<String, Queue<UUID>> arenaQueues = queues.get(entry.mode);
        if (arenaQueues != null) {
            Queue<UUID> queue = arenaQueues.get(entry.arena);
            if (queue != null) {
                queue.remove(uuid);
            }
        }
    }
    
    public void showStatus(Player player) {
        player.sendMessage("§6§l========== Duel Státusz ==========");
        
        for (DuelMode mode : plugin.getModeManager().getModes()) {
            String modeName = mode.getName();
            int active = plugin.getDuelManager().getActiveDuelsByMode(modeName);
            
            player.sendMessage("§e§l" + modeName + "§7: §f" + active + " §7aktív duel");
            
            Map<String, Queue<UUID>> arenaQueues = queues.get(modeName);
            if (arenaQueues != null && !arenaQueues.isEmpty()) {
                for (Map.Entry<String, Queue<UUID>> entry : arenaQueues.entrySet()) {
                    int count = entry.getValue().size();
                    if (count > 0) {
                        player.sendMessage("  §b" + entry.getKey() + "§7: §f" + count + " §7várakozó");
                    }
                }
            }
        }
        
        player.sendMessage("§6§l==================================");
        
        if (playerQueue.containsKey(player.getUniqueId())) {
            QueueEntry entry = playerQueue.get(player.getUniqueId());
            player.sendMessage("§aTe jelenleg: §e" + entry.mode + " - " + entry.arena + " §aqueue-ban");
        }
    }
    
    private static class QueueEntry {
        String mode;
        String arena;
        
        QueueEntry(String mode, String arena) {
            this.mode = mode;
            this.arena = arena;
        }
    }
}