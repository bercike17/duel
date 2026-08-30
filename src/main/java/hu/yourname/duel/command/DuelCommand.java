package hu.yourname.duel.command;

import hu.yourname.duel.DuelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelCommand implements CommandExecutor {
    
    private final DuelPlugin plugin;
    
    public DuelCommand(DuelPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Csak jatekos hasznalhatja!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join" -> handleJoin(player);
            case "setlobby" -> handleSetLobby(player);
            case "setnpc" -> handleSetNpc(player);
            case "createarena" -> handleCreateArena(player, args);
            case "createmode" -> handleCreateMode(player, args);
            case "setkit" -> handleSetKit(player, args);
            case "reload" -> handleReload(player);
            case "accept" -> plugin.getDuelManager().acceptDuel(player);
            case "deny" -> plugin.getDuelManager().denyDuel(player);
            case "queue" -> {
                if (args.length < 3) {
                    player.sendMessage("§cHasználat: /duel queue <mod> <palya>  vagy  /duel leavequeue");
                    return true;
                }
                plugin.getQueueManager().joinQueue(player, args[1], args[2]);
            }
            case "leavequeue" -> plugin.getQueueManager().leaveQueue(player);
            case "status" -> plugin.getQueueManager().showStatus(player);
            default -> {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.getMessageManager().getPrefixed("player-not-found"));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(plugin.getMessageManager().getPrefixed("cant-duel-self"));
                    return true;
                }
                plugin.getGuiManager().openDuelRequestGui(player, target);
            }
        }
        return true;
    }
    
    private void handleJoin(Player player) {
        if (plugin.getLobbyLocation() == null) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("lobby-not-set"));
            return;
        }
        player.teleport(plugin.getLobbyLocation());
        player.sendMessage(plugin.getMessageManager().getPrefixed("teleported-lobby"));
    }
    
    private void handleSetLobby(Player player) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        plugin.setLobbyLocation(player.getLocation());
        player.sendMessage(plugin.getMessageManager().getPrefixed("lobby-set"));
    }
    
    private void handleSetNpc(Player player) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        plugin.getConfig().set("npc-location", player.getLocation());
        plugin.saveConfig();
        player.sendMessage(plugin.getMessageManager().getPrefixed("npc-set"));
    }
    
    private void handleCreateArena(Player player, String[] args) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        if (args.length < 3) {
            player.sendMessage("§cHasznalat: /duel createarena <nev> <mod>");
            return;
        }
        String arenaName = args[1];
        String modeName = args[2];
        plugin.getArenaManager().createArena(arenaName, modeName, player.getLocation());
        
        Map<String, String> ph = new HashMap<>();
        ph.put("arena", arenaName);
        ph.put("mode", modeName);
        player.sendMessage(plugin.getMessageManager().getPrefixed("arena-created", ph));
    }
    
    private void handleCreateMode(Player player, String[] args) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cHasznalat: /duel createmode <nev>");
            return;
        }
        plugin.getModeManager().createMode(args[1]);
        Map<String, String> ph = new HashMap<>();
        ph.put("mode", args[1]);
        player.sendMessage(plugin.getMessageManager().getPrefixed("mode-created", ph));
    }
    
    private void handleSetKit(Player player, String[] args) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cHasznalat: /duel setkit <mod>");
            return;
        }
        String mode = args[1];
        plugin.getKitManager().saveKit(mode, player);
        
        Map<String, String> ph = new HashMap<>();
        ph.put("mode", mode);
        player.sendMessage(plugin.getMessageManager().getPrefixed("kit-saved", ph));
    }
    
    private void handleReload(Player player) {
        if (!player.hasPermission("duel.admin")) {
            player.sendMessage(plugin.getMessageManager().getPrefixed("no-permission"));
            return;
        }
        plugin.reloadConfig();
        plugin.getMessageManager().loadMessages();
        plugin.getArenaManager().loadArenas();
        plugin.getModeManager().loadModes();
        plugin.getKitManager().loadKits();
        player.sendMessage(plugin.getMessageManager().getPrefixed("config-reloaded"));
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6§lDuel Plugin - Parancsok:");
        player.sendMessage("§e/duel join §7- Belepes a duel lobbyba");
        player.sendMessage("§e/duel <jatekos> §7- Jatekos kihivasa");
        player.sendMessage("§e/duel accept §7- Kihivas elfogadasa");
        player.sendMessage("§e/duel deny §7- Kihivas elutasitasa");
        player.sendMessage("§e/duel queue <mod> <palya> §7- Queue-ba allas");
        player.sendMessage("§e/duel leavequeue §7- Queue elhagyasa");
        player.sendMessage("§e/duel status §7- Státusz megtekintése");
        if (player.hasPermission("duel.admin")) {
            player.sendMessage("§e/duel setlobby §7- Lobby hely beallitasa");
            player.sendMessage("§e/duel setnpc §7- NPC hely beallitasa");
            player.sendMessage("§e/duel createarena <nev> <mod> §7- Palya letrehozasa");
            player.sendMessage("§e/duel createmode <nev> §7- Mod letrehozasa");
            player.sendMessage("§e/duel setkit <mod> §7- Kit mentese modhoz");
            player.sendMessage("§e/duel reload §7- Konfig ujratoltese");
        }
    }
}