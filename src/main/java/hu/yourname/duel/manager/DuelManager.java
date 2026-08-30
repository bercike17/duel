package hu.yourname.duel.manager;

import hu.yourname.duel.DuelPlugin;
import hu.yourname.duel.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DuelManager {
    
    private final DuelPlugin plugin;
    private final Map<UUID, DuelRequest> pendingRequests = new HashMap<>();
    private final Map<UUID, ActiveDuel> activeDuels = new HashMap<>();
    private final Map<UUID, PlayerInventoryData> savedInventories = new HashMap<>();
    private final Set<UUID> countdownPlayers = new HashSet<>();
    private final Map<UUID, BukkitTask> countdownTasks = new HashMap<>();
    private final Map<UUID, UUID> countdownOpponents = new HashMap<>();
    private final Random random = new Random();
    
    public DuelManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void sendDuelRequest(Player challenger, Player target, String mode, String arena) {
        UUID targetId = target.getUniqueId();
        
        if (pendingRequests.containsKey(targetId)) {
            challenger.sendMessage(plugin.getMessageManager().getPrefixed("duel-pending"));
            return;
        }
        
        if (activeDuels.containsKey(challenger.getUniqueId()) || activeDuels.containsKey(targetId)) {
            challenger.sendMessage(plugin.getMessageManager().getPrefixed("duel-in-progress"));
            return;
        }
        
        pendingRequests.put(targetId, new DuelRequest(challenger.getUniqueId(), mode, arena, System.currentTimeMillis()));
        
        Map<String, String> ph = new HashMap<>();
        ph.put("target", target.getName());
        challenger.sendMessage(plugin.getMessageManager().getPrefixed("duel-request-sent", ph));
        
        ph.clear();
        ph.put("challenger", challenger.getName());
        target.sendMessage(plugin.getMessageManager().getPrefixed("duel-request-received", ph));
        
        ph.put("mode", mode);
        ph.put("arena", arena);
        target.sendMessage(plugin.getMessageManager().getPrefixed("duel-request-info", ph));
        target.sendMessage(plugin.getMessageManager().getPrefixed("duel-accept-command"));
        
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingRequests.containsKey(targetId)) {
                    pendingRequests.remove(targetId);
                    challenger.sendMessage(plugin.getMessageManager().getPrefixed("duel-expired"));
                    if (target.isOnline()) target.sendMessage(plugin.getMessageManager().getPrefixed("duel-expired"));
                }
            }
        }.runTaskLater(plugin, 600L);
    }
    
    public void acceptDuel(Player player) {
        DuelRequest req = pendingRequests.remove(player.getUniqueId());
        if (req == null) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-pending-request"));
            return;
        }
        
        Player challenger = Bukkit.getPlayer(req.challenger);
        if (challenger == null || !challenger.isOnline()) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("challenger-offline"));
            return;
        }
        
        Arena arena = plugin.getArenaManager().getArena(req.arena);
        if (arena == null) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("arena-not-found"));
            return;
        }
        
        startDuel(challenger, player, req.mode, arena);
    }
    
    public void denyDuel(Player player) {
        DuelRequest req = pendingRequests.remove(player.getUniqueId());
        if (req == null) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-pending-request"));
            return;
        }
        
        Player challenger = Bukkit.getPlayer(req.challenger);
        player.sendMessage(plugin.getMessageManager().getPrefixed("duel-denied"));
        if (challenger != null && challenger.isOnline()) {
            Map<String, String> ph = new HashMap<>();
            ph.put("target", player.getName());
            challenger.sendMessage(plugin.getMessageManager().getPrefixed("duel-denied-target", ph));
        }
    }
    
    private void startDuel(Player p1, Player p2, String mode, Arena arena) {
        saveInventory(p1);
        saveInventory(p2);
        
        plugin.getKitManager().applyKit(p1, mode);
        plugin.getKitManager().applyKit(p2, mode);
        
        p1.teleport(arena.getSpawn1());
        p2.teleport(arena.getSpawn2());
        
        startCountdown(p1, p2, mode, arena);
    }
    
    public void startMatch(Player p1, Player p2, String mode, String arenaName) {
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            p1.sendMessage(plugin.getMessageManager().getPrefixed("arena-not-found"));
            p2.sendMessage(plugin.getMessageManager().getPrefixed("arena-not-found"));
            return;
        }
        startDuel(p1, p2, mode, arena);
    }
    
    private void saveInventory(Player player) {
        PlayerInventoryData data = new PlayerInventoryData();
        data.contents = cloneItems(player.getInventory().getContents());
        data.armor = cloneItems(player.getInventory().getArmorContents());
        data.exp = player.getExp();
        data.level = player.getLevel();
        data.health = player.getHealth();
        data.food = player.getFoodLevel();
        data.gamemode = player.getGameMode();
        data.effects = new ArrayList<>(player.getActivePotionEffects());
        savedInventories.put(player.getUniqueId(), data);
        
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setExp(0);
        player.setLevel(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    private void restoreInventory(Player player) {
        PlayerInventoryData data = savedInventories.remove(player.getUniqueId());
        if (data == null) return;
        
        player.getInventory().setContents(data.contents != null ? data.contents : new ItemStack[0]);
        player.getInventory().setArmorContents(data.armor != null ? data.armor : new ItemStack[0]);
        player.setExp(data.exp);
        player.setLevel(data.level);
        player.setHealth(Math.min(data.health, 20));
        player.setFoodLevel(data.food);
        player.setGameMode(data.gamemode);
        for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
            player.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : data.effects) {
            player.addPotionEffect(effect);
        }
        player.updateInventory();
    }
    
    private ItemStack[] cloneItems(ItemStack[] original) {
        if (original == null) return new ItemStack[0];
        ItemStack[] clone = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                clone[i] = original[i].clone();
            }
        }
        return clone;
    }
    
    private void startCountdown(Player p1, Player p2, String mode, Arena arena) {
        UUID u1 = p1.getUniqueId();
        UUID u2 = p2.getUniqueId();

        countdownPlayers.add(u1);
        countdownPlayers.add(u2);
        countdownOpponents.put(u1, u2);
        countdownOpponents.put(u2, u1);

        final int seconds = Math.max(1, plugin.getConfig().getInt("countdown-seconds", 5));

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = seconds;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    countdownPlayers.remove(u1);
                    countdownPlayers.remove(u2);
                    countdownOpponents.remove(u1);
                    countdownOpponents.remove(u2);
                    
                    activeDuels.put(u1, new ActiveDuel(u2, mode, arena.getName()));
                    activeDuels.put(u2, new ActiveDuel(u1, mode, arena.getName()));
                    
                    Map<String, String> ph = new HashMap<>();
                    ph.put("mode", mode);
                    ph.put("arena", arena.getName());
                    String msg = plugin.getMessageManager().get("duel-started", ph);
                    
                    p1.sendTitle("", msg, 10, 70, 20);
                    p2.sendTitle("", msg, 10, 70, 20);
                    p1.sendMessage(plugin.getMessageManager().getPrefixed("duel-started", ph));
                    p2.sendMessage(plugin.getMessageManager().getPrefixed("duel-started", ph));
                    
                    countdownTasks.remove(u1);
                    countdownTasks.remove(u2);
                    cancel();
                    return;
                }
                
                Map<String, String> ph = new HashMap<>();
                ph.put("seconds", String.valueOf(timeLeft));
                String title = plugin.getMessageManager().get("countdown-title", ph);
                String subtitle = plugin.getMessageManager().get("countdown-subtitle");
                
                p1.sendTitle(title, subtitle, 0, 25, 0);
                p2.sendTitle(title, subtitle, 0, 25, 0);
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        countdownTasks.put(u1, task);
        countdownTasks.put(u2, task);
    }
    
    public void endDuel(Player winner, Player loser) {
        activeDuels.remove(winner.getUniqueId());
        activeDuels.remove(loser.getUniqueId());
        
        winner.sendMessage(plugin.getMessageManager().getPrefixed("duel-won"));
        loser.sendMessage(plugin.getMessageManager().getPrefixed("duel-lost"));
        
        restoreInventory(winner);
        restoreInventory(loser);
        
        Location lobby = plugin.getLobbyLocation();
        if (lobby != null) {
            winner.teleport(lobby);
            loser.teleport(lobby);
        }
    }
    
    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (countdownPlayers.contains(uuid)) {
            countdownPlayers.remove(uuid);
            BukkitTask task = countdownTasks.remove(uuid);
            if (task != null) task.cancel();
            
            UUID opponentUuid = countdownOpponents.remove(uuid);
            if (opponentUuid != null) {
                countdownPlayers.remove(opponentUuid);
                countdownTasks.remove(opponentUuid);
                countdownOpponents.remove(opponentUuid);
                
                Player opponent = Bukkit.getPlayer(opponentUuid);
                if (opponent != null && opponent.isOnline()) {
                    opponent.sendMessage(plugin.getMessageManager().getPrefixed("opponent-quit-countdown"));
                    restoreInventory(opponent);
                    Location lobby = plugin.getLobbyLocation();
                    if (lobby != null) opponent.teleport(lobby);
                }
            }
            
            savedInventories.remove(uuid);
            return;
        }
        
        ActiveDuel duel = activeDuels.get(uuid);
        if (duel == null) return;
        
        Player opponent = Bukkit.getPlayer(duel.opponent);
        activeDuels.remove(uuid);
        activeDuels.remove(duel.opponent);
        
        if (opponent != null && opponent.isOnline()) {
            opponent.sendMessage(plugin.getMessageManager().getPrefixed("duel-won"));
            restoreInventory(opponent);
            Location lobby = plugin.getLobbyLocation();
            if (lobby != null) opponent.teleport(lobby);
        }
        
        savedInventories.remove(uuid);
    }
    
    public int getActiveDuelsByMode(String mode) {
        return (int) activeDuels.values().stream()
            .filter(d -> d.mode.equalsIgnoreCase(mode))
            .count() / 2;
    }
    
    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId());
    }
    
    public boolean isInCountdown(Player player) {
        return countdownPlayers.contains(player.getUniqueId());
    }
    
    private static class DuelRequest {
        UUID challenger;
        String mode;
        String arena;
        long timestamp;
        
        DuelRequest(UUID challenger, String mode, String arena, long timestamp) {
            this.challenger = challenger;
            this.mode = mode;
            this.arena = arena;
            this.timestamp = timestamp;
        }
    }
    
    private static class ActiveDuel {
        UUID opponent;
        String mode;
        String arena;
        
        ActiveDuel(UUID opponent, String mode, String arena) {
            this.opponent = opponent;
            this.mode = mode;
            this.arena = arena;
        }
    }
    
    private static class PlayerInventoryData {
        ItemStack[] contents;
        ItemStack[] armor;
        float exp;
        int level;
        double health;
        int food;
        GameMode gamemode;
        List<PotionEffect> effects;
    }
}
