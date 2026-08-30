package hu.yourname.duel.kit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitManager {
    private final JavaPlugin plugin;
    private final File kitFile;
    private FileConfiguration kitConfig;
    private final Map<String, Kit> kits = new HashMap<>();
    
    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.kitFile = new File(plugin.getDataFolder(), "kits.yml");
        loadKits();
    }
    
    public void loadKits() {
        if (!kitFile.exists()) {
            try {
                kitFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kitConfig = YamlConfiguration.loadConfiguration(kitFile);
        kits.clear();
        
        if (kitConfig.contains("kits")) {
            for (String mode : kitConfig.getConfigurationSection("kits").getKeys(false)) {
                String path = "kits." + mode;
                List<?> itemsList = kitConfig.getList(path + ".items", new ArrayList<>());
                List<?> armorList = kitConfig.getList(path + ".armor", new ArrayList<>());
                
                ItemStack[] items = itemsList.stream()
                    .filter(o -> o instanceof ItemStack)
                    .map(o -> (ItemStack) o)
                    .toArray(ItemStack[]::new);
                ItemStack[] armor = armorList.stream()
                    .filter(o -> o instanceof ItemStack)
                    .map(o -> (ItemStack) o)
                    .toArray(ItemStack[]::new);
                    
                kits.put(mode.toLowerCase(), new Kit(cloneItems(items), cloneItems(armor)));
            }
        }
    }
    
    public void saveKit(String mode, Player player) {
        ItemStack[] items = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        
        String path = "kits." + mode.toLowerCase();
        kitConfig.set(path + ".items", items != null ? Arrays.asList(items) : new ArrayList<>());
        kitConfig.set(path + ".armor", armor != null ? Arrays.asList(armor) : new ArrayList<>());
        save();
        
        kits.put(mode.toLowerCase(), new Kit(cloneItems(items), cloneItems(armor)));
    }
    
    public void applyKit(Player player, String mode) {
        Kit kit = kits.get(mode.toLowerCase());
        if (kit == null) return;
        
        player.getInventory().setContents(kit.getItems());
        player.getInventory().setArmorContents(kit.getArmor());
        player.updateInventory();
    }
    
    public boolean hasKit(String mode) {
        return kits.containsKey(mode.toLowerCase());
    }
    
    private void save() {
        try {
            kitConfig.save(kitFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    
    public static class Kit {
        private final ItemStack[] items;
        private final ItemStack[] armor;
        
        public Kit(ItemStack[] items, ItemStack[] armor) {
            this.items = items != null ? items : new ItemStack[0];
            this.armor = armor != null ? armor : new ItemStack[0];
        }
        
        public ItemStack[] getItems() { return items; }
        public ItemStack[] getArmor() { return armor; }
    }
}