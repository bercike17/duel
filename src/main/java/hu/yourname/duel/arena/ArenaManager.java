package hu.yourname.duel.arena;

import hu.yourname.duel.DuelPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArenaManager {
    
    private final DuelPlugin plugin;
    private final File arenaFile;
    private FileConfiguration arenaConfig;
    private final List<Arena> arenas = new ArrayList<>();
    
    public ArenaManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        loadArenas();
    }
    
    public void loadArenas() {
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
        arenas.clear();
        
        if (arenaConfig.contains("arenas")) {
            for (String key : arenaConfig.getConfigurationSection("arenas").getKeys(false)) {
                String path = "arenas." + key;
                String mode = arenaConfig.getString(path + ".mode");
                Location spawn1 = arenaConfig.getLocation(path + ".spawn1");
                Location spawn2 = arenaConfig.getLocation(path + ".spawn2");
                arenas.add(new Arena(key, mode, spawn1, spawn2));
            }
        }
    }
    
    public void createArena(String name, String mode, Location spawn1, Location spawn2) {
        Arena arena = new Arena(name, mode, spawn1, spawn2);
        arenas.add(arena);
        saveArena(arena);
    }
    
    public void createArena(String name, String mode, Location spawn) {
        // Alapértelmezetten a spawn1 és spawn2 ugyanaz, később állítható
        createArena(name, mode, spawn, spawn.clone().add(5, 0, 0));
    }
    
    private void saveArena(Arena arena) {
        String path = "arenas." + arena.getName();
        arenaConfig.set(path + ".mode", arena.getMode());
        arenaConfig.set(path + ".spawn1", arena.getSpawn1());
        arenaConfig.set(path + ".spawn2", arena.getSpawn2());
        save();
    }
    
    public Arena getArena(String name) {
        return arenas.stream()
            .filter(a -> a.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public List<Arena> getArenasByMode(String mode) {
        return arenas.stream()
            .filter(a -> a.getMode().equalsIgnoreCase(mode))
            .toList();
    }
    
    public List<Arena> getAllArenas() {
        return new ArrayList<>(arenas);
    }
    
    private void save() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}