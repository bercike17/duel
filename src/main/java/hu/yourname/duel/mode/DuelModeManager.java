package hu.yourname.duel.mode;

import hu.yourname.duel.DuelPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DuelModeManager {
    
    private final DuelPlugin plugin;
    private final File modeFile;
    private FileConfiguration modeConfig;
    private final List<DuelMode> modes = new ArrayList<>();
    
    public DuelModeManager(DuelPlugin plugin) {
        this.plugin = plugin;
        this.modeFile = new File(plugin.getDataFolder(), "modes.yml");
        loadModes();
    }
    
    public void loadModes() {
        if (!modeFile.exists()) {
            try {
                modeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        modeConfig = YamlConfiguration.loadConfiguration(modeFile);
        modes.clear();
        
        // Alapértelmezett módok
        if (!modeConfig.contains("modes")) {
            createDefaultModes();
        }
        
        for (String key : modeConfig.getConfigurationSection("modes").getKeys(false)) {
            String path = "modes." + key;
            Material icon = Material.valueOf(modeConfig.getString(path + ".icon", "DIAMOND_SWORD"));
            modes.add(new DuelMode(key, icon));
        }
    }
    
    private void createDefaultModes() {
        modeConfig.set("modes.Classic.icon", "DIAMOND_SWORD");
        modeConfig.set("modes.Sumo.icon", "SLIME_BALL");
        modeConfig.set("modes.Bow.icon", "BOW");
        modeConfig.set("modes.BuildUHC.icon", "GOLDEN_APPLE");
        modeConfig.set("modes.Nodebuff.icon", "POTION");
        save();
    }
    
    public void createMode(String name) {
        DuelMode mode = new DuelMode(name, Material.DIAMOND_SWORD);
        modes.add(mode);
        modeConfig.set("modes." + name + ".icon", "DIAMOND_SWORD");
        save();
    }
    
    public DuelMode getMode(String name) {
        return modes.stream()
            .filter(m -> m.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }
    
    public List<DuelMode> getModes() {
        return new ArrayList<>(modes);
    }
    
    private void save() {
        try {
            modeConfig.save(modeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}