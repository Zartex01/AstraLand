package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.EconomyManager;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class IslandGeneratorGUI implements InventoryHolder {

    private static final String[] TIER_NAMES = {
        "&7Niveau 0 &8- &fCobblestone pur",
        "&aNiveau 1 &8- &fPierre + Cobblestone",
        "&aNiveau 2 &8- &fVariantes de pierre",
        "&6Niveau 3 &8- &fMinerai de Charbon",
        "&6Niveau 4 &8- &fMinerai de Fer",
        "&bNiveau 5 &8- &fMinerai d'Or",
        "&dNiveau 6 &8- &fMinerai de Diamant",
        "&5Niveau 7 &8- &fMinerai d'Émeraude"
    };
    private static final Material[] TIER_ICONS = {
        Material.COBBLESTONE, Material.STONE, Material.ANDESITE,
        Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
        Material.DIAMOND_ORE, Material.EMERALD_ORE
    };
    private static final int[] COSTS = { 0, 1000, 3000, 7000, 15000, 30000, 60000, 100000 };
    private static final int[] SLOTS = { 10, 11, 12, 13, 14, 15, 16, -1 }; // -1 unused

    private final Inventory inv;
    private final Island island;
    private final Skyblock plugin;

    public IslandGeneratorGUI(Island island, Skyblock plugin) {
        this.island = island;
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 36, c("&8⚙ &a&lGénérateur de l'île"));
        build();
    }

    private void build() {
        inv.clear();
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 36; i++) inv.setItem(i, border);

        int currentLevel = island.getGeneratorLevel();
        EconomyManager eco = plugin.getEconomyManager();

        int[] displaySlots = {10, 11, 12, 13, 14, 15, 16, 7};
        for (int tier = 0; tier < 8; tier++) {
            boolean active   = tier == currentLevel;
            boolean unlocked = tier <= currentLevel;
            boolean canBuy   = tier == currentLevel + 1;
            int cost = tier < COSTS.length ? COSTS[tier] : 0;

            Material mat = active ? TIER_ICONS[tier] : (unlocked ? TIER_ICONS[tier] : Material.GRAY_STAINED_GLASS_PANE);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(c(TIER_NAMES[Math.min(tier, TIER_NAMES.length - 1)]));
                List<String> lore = new ArrayList<>();
                if (active)   lore.add(c("&a▶ Niveau actuel"));
                if (unlocked && !active) lore.add(c("&7✔ Débloqué"));
                if (!unlocked && !canBuy) lore.add(c("&8🔒 Débloqué après le niveau " + (tier - 1)));
                if (canBuy) {
                    int bal = eco.getBalance(island.getOwner());
                    lore.add(c("&7Coût d'amélioration : &6" + cost + " $"));
                    lore.add(c("&7Ton solde : &e" + bal + " $"));
                    lore.add(bal >= cost ? c("&a▶ Cliquer pour améliorer !") : c("&c✗ Fonds insuffisants"));
                }
                addDrops(lore, tier);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            int slot = displaySlots[tier];
            if (slot >= 0 && slot < 36) inv.setItem(slot, item);
        }

        // Info panel
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta im = info.getItemMeta();
        if (im != null) {
            im.setDisplayName(c("&e&lTon Générateur"));
            im.setLore(List.of(
                c("&7Niveau actuel : &a" + currentLevel),
                c("&7Le générateur remplace le cobblestone"),
                c("&7par des blocs plus précieux !"),
                c(""),
                c("&7Améliore en cliquant sur le niveau suivant.")
            ));
            info.setItemMeta(im);
        }
        inv.setItem(4, info);

        // Retour
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) { bm.setDisplayName(c("&c← Fermer")); back.setItemMeta(bm); }
        inv.setItem(31, back);
    }

    private void addDrops(List<String> lore, int tier) {
        lore.add(c("&7─────────────────"));
        switch (tier) {
            case 0 -> lore.add(c("  &f100% &7Cobblestone"));
            case 1 -> { lore.add(c("  &f70% &7Cobblestone")); lore.add(c("  &f30% &7Pierre")); }
            case 2 -> { lore.add(c("  &f50% &7Cobblestone")); lore.add(c("  &f30% &7Pierre")); lore.add(c("  &f20% &7Andésite/Granit/Diorite")); }
            case 3 -> { lore.add(c("  &f45% &7Cobblestone")); lore.add(c("  &f35% &7Pierre")); lore.add(c("  &810% &8Minerai de Charbon")); lore.add(c("  &f10% &7Autres")); }
            case 4 -> { lore.add(c("  &f40% &7Cobblestone")); lore.add(c("  &f30% &7Pierre")); lore.add(c("  &715% &7Minerai de Fer")); lore.add(c("  &815% &8Autres")); }
            case 5 -> { lore.add(c("  &f35% &7Cobblestone")); lore.add(c("  &f25% &7Pierre")); lore.add(c("  &715% &7Minerai de Fer")); lore.add(c("  &610% &6Minerai d'Or")); lore.add(c("  &f15% &7Autres")); }
            case 6 -> { lore.add(c("  &f30% &7Cobblestone")); lore.add(c("  &f25% &7Pierre")); lore.add(c("  &715% &7Minerai de Fer")); lore.add(c("  &610% &6Minerai d'Or")); lore.add(c("  &b5% &bMinerai de Diamant")); lore.add(c("  &f15% &7Autres")); }
            case 7 -> { lore.add(c("  &f25% &7Cobblestone")); lore.add(c("  &f20% &7Pierre")); lore.add(c("  &715% &7Minerai de Fer")); lore.add(c("  &610% &6Or")); lore.add(c("  &b8% &bDiamant")); lore.add(c("  &a2% &aÉmeraude")); lore.add(c("  &f20% &7Autres")); }
        }
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 31) { player.closeInventory(); return; }

        if (!island.isOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire peut améliorer le générateur."));
            return;
        }

        int[] displaySlots = {10, 11, 12, 13, 14, 15, 16, 7};
        for (int tier = 0; tier < 8; tier++) {
            if (displaySlots[tier] == slot) {
                int nextLevel = island.getGeneratorLevel() + 1;
                if (tier != nextLevel) return;
                int cost = COSTS[tier];
                EconomyManager eco = plugin.getEconomyManager();
                if (eco.getBalance(island.getOwner()) < cost) {
                    player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + cost + " $"));
                    return;
                }
                eco.removeBalance(island.getOwner(), cost);
                island.setGeneratorLevel(nextLevel);
                plugin.getIslandManager().saveAll();
                player.sendMessage(c("&a✔ Générateur amélioré au niveau &e" + nextLevel + " &a! Coût : &e" + cost + " $"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.0f);
                build();
                return;
            }
        }
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
