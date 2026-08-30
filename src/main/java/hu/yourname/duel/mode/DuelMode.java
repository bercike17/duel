package hu.yourname.duel.mode;

import org.bukkit.Material;

public class DuelMode {
    
    private final String name;
    private final Material icon;
    
    public DuelMode(String name, Material icon) {
        this.name = name;
        this.icon = icon;
    }
    
    public String getName() { return name; }
    public Material getIcon() { return icon; }
}