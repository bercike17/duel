package hu.yourname.duel.gui;

import hu.yourname.duel.DuelPlugin;
import hu.yourname.duel.arena.Arena;
import hu.yourname.duel.mode.DuelMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GuiManager implements Listener {
    
    private final DuelPlugin plugin;
    private final Map<UUID, Player> duelRequests = new HashMap<>();
    private final Map<UUID, DuelSelection> selections = new HashMap<>();
    
    public GuiManager(DuelPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openDuelRequestGui(Player challenger, Player target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lDuel Kihivas: §c" + target.getName());
        
        ItemStack info = createItem(Material.PAPER, "§eKihivo: §f" + challenger.getName(), 
            Arrays.asList("§7Valassz modot es palyat!"));
        inv.setItem(4, info);
        
        ItemStack randomMode = createItem(Material.COMMAND_BLOCK, "§6§lRandom Mod", 
            Arrays.asList("§7Veletlenszeru mod valasztasa"));
        inv.setItem(11, randomMode);
        
        int slot = 12;
        for (DuelMode mode : plugin.getModeManager().getModes()) {
            ItemStack item = createItem(mode.getIcon(), "§a" + mode.getName(), 
                Arrays.asList("§7Kattints a kivalasztashoz!"));
            inv.setItem(slot++, item);
            if (slot > 16) break;
        }
        
        ItemStack randomArena = createItem(Material.ENDER_PEARL, "§6§lRandom Palya", 
            Arrays.asList("§7Veletlenszeru palya valasztasa"));
        inv.setItem(20, randomArena);
        
        updateArenaItems(inv, plugin.getModeManager().getModes().isEmpty() ? null : plugin.getModeManager().getModes().get(0));
        
        ItemStack send = createItem(Material.EMERALD_BLOCK, "§2§lKihivas Kuldes", 
            Arrays.asList("§7Kattints a kihivas elkuldeshez!"));
        inv.setItem(26, send);
        
        duelRequests.put(challenger.getUniqueId(), target);
        selections.put(challenger.getUniqueId(), new DuelSelection());
        challenger.openInventory(inv);
    }
    
    public void openNpcMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8§lDuel Modok");
        
        int slot = 10;
        for (DuelMode mode : plugin.getModeManager().getModes()) {
            int active = plugin.getDuelManager().getActiveDuelsByMode(mode.getName());
            int queueCount = 0;
            for (Arena arena : plugin.getArenaManager().getArenasByMode(mode.getName())) {
                queueCount += plugin.getQueueManager().getQueueCount(mode.getName(), arena.getName());
            }

            ItemStack item = createItem(mode.getIcon(), "§a" + mode.getName(), Arrays.asList(
                "§7Aktív duel: §f" + active,
                "§7Várakozó: §f" + queueCount,
                "§7Kattints a queue-hoz!"
            ));
            
            inv.setItem(slot++, item);
            if (slot == 17) slot = 19;
            if (slot == 26) break;
        }
        
        ItemStack stats = createItem(Material.BOOK, "§eStatisztikaim", 
            Arrays.asList("§7Hamarosan..."));
        inv.setItem(31, stats);
        
        player.openInventory(inv);
    }
    
    private void updateArenaItems(Inventory inv, DuelMode mode) {
        for (int i = 21; i <= 25; i++) {
            inv.setItem(i, null);
        }
        
        if (mode == null) return;
        
        int slot = 21;
        List<Arena> arenas = plugin.getArenaManager().getArenasByMode(mode.getName());
        for (Arena arena : arenas) {
            ItemStack item = createItem(Material.GRASS_BLOCK, "§b" + arena.getName(), 
                Arrays.asList("§7Mod: §f" + mode.getName(), "§7Kattints a kivalasztashoz!"));
            inv.setItem(slot++, item);
            if (slot > 25) break;
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        
        String title = e.getView().getTitle();
        if (!title.startsWith("§8§lDuel")) return;
        
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        
        String name = item.getItemMeta().getDisplayName();
        UUID uuid = player.getUniqueId();
        
        if (title.contains("Duel Kihivas")) {
            handleDuelRequestGui(player, name, e.getSlot(), uuid);
        } else if (title.contains("Duel Modok")) {
            handleNpcMenu(player, name);
        }
    }
    
    private void handleDuelRequestGui(Player player, String name, int slot, UUID uuid) {
        Player target = duelRequests.get(uuid);
        if (target == null) {
            player.closeInventory();
            return;
        }
        
        DuelSelection sel = selections.get(uuid);
        
        if (name.equals("§6§lRandom Mod")) {
            List<DuelMode> modes = plugin.getModeManager().getModes();
            if (!modes.isEmpty()) {
                sel.mode = modes.get(new Random().nextInt(modes.size())).getName();
                Map<String, String> ph = new HashMap<>();
                ph.put("mode", sel.mode);
                player.sendMessage(plugin.getMessageManager().getPrefixed("random-mode", ph));
            }
        } else if (name.equals("§6§lRandom Palya")) {
            if (sel.mode != null) {
                List<Arena> arenas = plugin.getArenaManager().getArenasByMode(sel.mode);
                if (!arenas.isEmpty()) {
                    sel.arena = arenas.get(new Random().nextInt(arenas.size())).getName();
                    Map<String, String> ph = new HashMap<>();
                    ph.put("arena", sel.arena);
                    player.sendMessage(plugin.getMessageManager().getPrefixed("random-arena", ph));
                }
            } else {
                player.sendMessage(plugin.getMessageManager().getPrefixed("select-mode-first"));
            }
        } else if (name.equals("§2§lKihivas Kuldes")) {
            if (sel.mode == null || sel.arena == null) {
                player.sendMessage(plugin.getMessageManager().getPrefixed("select-mode-arena"));
                return;
            }
            player.closeInventory();
            plugin.getDuelManager().sendDuelRequest(player, target, sel.mode, sel.arena);
            duelRequests.remove(uuid);
            selections.remove(uuid);
        } else if (slot >= 12 && slot <= 16) {
            sel.mode = name.replace("§a", "");
            Map<String, String> ph = new HashMap<>();
            ph.put("mode", sel.mode);
            player.sendMessage(plugin.getMessageManager().getPrefixed("mode-selected", ph));
            
            Inventory inv = player.getOpenInventory().getTopInventory();
            DuelMode mode = plugin.getModeManager().getMode(sel.mode);
            updateArenaItems(inv, mode);
        } else if (slot >= 21 && slot <= 25) {
            sel.arena = name.replace("§b", "");
            Map<String, String> ph = new HashMap<>();
            ph.put("arena", sel.arena);
            player.sendMessage(plugin.getMessageManager().getPrefixed("arena-selected", ph));
        }
    }
    
    private void handleNpcMenu(Player player, String name) {
        if (name.equals("§eStatisztikaim")) {
            player.sendMessage("§eStatisztikak hamarosan...");
            player.closeInventory();
            return;
        }
        
        String modeName = name.replace("§a", "");
        DuelMode mode = plugin.getModeManager().getMode(modeName);
        if (mode != null) {
            player.closeInventory();
            List<Arena> arenas = plugin.getArenaManager().getArenasByMode(modeName);
            if (!arenas.isEmpty()) {
                // Automatikusan az első arénához lépteti be a sorba
                plugin.getQueueManager().joinQueue(player, modeName, arenas.get(0).getName());
            } else {
                player.sendMessage(plugin.getMessageManager().getPrefixed("arena-not-found"));
            }
        }
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static class DuelSelection {
        String mode;
        String arena;
    }
}