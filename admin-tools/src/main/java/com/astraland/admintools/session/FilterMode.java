package com.astraland.admintools.session;

import org.bukkit.Material;

public enum FilterMode {

    CONTAINS("Contient", "&7Joueurs dont le pseudo &acontient &7le texte", Material.HOPPER),
    STARTS_WITH("Commence par", "&7Joueurs dont le pseudo &acommence par &7le texte", Material.ARROW),
    ENDS_WITH("Termine par", "&7Joueurs dont le pseudo &atermine par &7le texte", Material.SPECTRAL_ARROW),
    EXACT("Exact", "&7Joueur avec &ace pseudo exact", Material.TARGET);

    private final String displayName;
    private final String description;
    private final Material material;

    FilterMode(String displayName, String description, Material material) {
        this.displayName = displayName;
        this.description = description;
        this.material = material;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Material getMaterial()  { return material; }

    public FilterMode next() {
        FilterMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public boolean matches(String playerName, String filterText) {
        if (filterText == null || filterText.isEmpty()) return true;
        String name = playerName.toLowerCase();
        String text = filterText.toLowerCase();
        return switch (this) {
            case CONTAINS    -> name.contains(text);
            case STARTS_WITH -> name.startsWith(text);
            case ENDS_WITH   -> name.endsWith(text);
            case EXACT       -> name.equals(text);
        };
    }
}
