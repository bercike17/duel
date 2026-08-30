package hu.yourname.duel.listener;

import hu.yourname.duel.DuelPlugin;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class NpcListener implements Listener {
    
    private final DuelPlugin plugin;
    private Villager npc;
    
    public NpcListener(DuelPlugin plugin) {
        this.plugin = plugin;
        spawnNpc();
    }
    
    private void spawnNpc() {
        if (!plugin.getConfig().contains("npc-location")) return;
        
        Location loc = (Location) plugin.getConfig().get("npc-location");
        if (loc == null || loc.getWorld() == null) return;
        
        loc.getWorld().getEntities().stream()
            .filter(e -> e instanceof Villager)
            .filter(e -> e.getCustomName() != null && e.getCustomName().equals("§6§lDuel Mester"))
            .forEach(e -> e.remove());
        
        npc = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        npc.setCustomName("§6§lDuel Mester");
        npc.setCustomNameVisible(true);
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setSilent(true);
        npc.setProfession(Villager.Profession.NITWIT);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Villager villager)) return;
        if (!villager.equals(npc)) return;
        
        e.setCancelled(true);
        Player player = e.getPlayer();
        plugin.getGuiManager().openNpcMenu(player);
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (npc != null && !npc.isValid()) {
            spawnNpc();
        }
    }
}